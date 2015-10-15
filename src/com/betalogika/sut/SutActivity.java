package com.betalogika.sut;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SutActivity extends Activity implements OnClickListener {

	/**developer account key for this app*/
	public final static String TWIT_KEY = "Rkx3NH2IFVeGoBZUenl1sDb3a"; 
	/**developer secret for the app*/
	public final static String TWIT_SECRET = "Pu3tkMkPrBqerAxjfs0Hv0yF59xiX5UCIyUj7y0YTsjjb2VfHu"; 
	/**app url*/
	public final static String TWIT_URL = "sut-android://betalogika";
	
	/**Twitter instance*/
	private Twitter niceTwitter; 
	/**request token for accessing user account*/
	private RequestToken niceRequestToken; 
	/**shared preferences to store user details*/
	private SharedPreferences nicePrefs; 
	  
	//for error logging 
	private String LOG_TAG = "SutActivity";//alter for your Activity name
	
	
	boolean mark = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sut);
		
		//get the preferences for the app 
		nicePrefs = getSharedPreferences("TwitNicePrefs", 0); 
		  
		//find out if the user preferences are set 
		if(nicePrefs.getString("user_token", null)==null) { 
		      
		    //no user preferences so prompt to sign in 
		    setContentView(R.layout.main); 
		    
		    //get a twitter instance for authentication 
		    niceTwitter = new TwitterFactory().getInstance(); 
		      
		    //pass developer key and secret 
		    niceTwitter.setOAuthConsumer(TWIT_KEY, TWIT_SECRET);
		    
		    new RequestT().execute();
		    
		    //setup button for click listener 
		    Button signIn = (Button)findViewById(R.id.signin); 
		    signIn.setOnClickListener(this);
		  
		} 
		else 
		{ 
		    //user preferences are set - get timeline 
			startActivity(new Intent(this, TweetListActivity.class));
            finish();
		}
		
	}
	
	private class RequestT extends AsyncTask<Void, String,String> {
	     protected String doInBackground(Void... urls) {
	    	 try 
			    { 
			        //get authentication request token 
			        niceRequestToken = niceTwitter.getOAuthRequestToken(TWIT_URL); 
			    } 
			    catch(TwitterException te) 
			    { 
			    	Log.e(LOG_TAG, "TE " + te.getMessage());
			    }
	    	
	    	 return null;
	     }

	 }
	
	private class RequestS extends AsyncTask<Uri, String,AccessToken> {
		SutActivity context;
		
		public RequestS(SutActivity a){
			this.context=a;
		}
		
		protected AccessToken doInBackground(Uri... urls) {
	    	 Uri twitURI = urls[0];
	    	 //is verifcation - get the returned data 
	    	 String oaVerifier = twitURI.getQueryParameter("oauth_verifier");
	    	 try
		        { 
		            //try to get an access token using the returned data from the verification page 
		            AccessToken accToken = niceTwitter.getOAuthAccessToken(niceRequestToken, oaVerifier);
		            
		            return accToken;
		        } 
		        catch (TwitterException te) 
		        {
		        	Log.e(LOG_TAG, "Failed to get access token: " + te.getMessage());
		        	return null;
		        }
	     }
	     
	     @Override
	     protected void onPostExecute(AccessToken accToken) {
	    	 nicePrefs = getSharedPreferences("TwitNicePrefs", 0);
    	 	 //add the token and secret to shared prefs for future reference 
             nicePrefs.edit() 
                .putString("user_token", accToken.getToken()) 
                .putString("user_secret", accToken.getTokenSecret()) 
                .commit();
             startActivity(new Intent(context, TweetListActivity.class));
             context.finish();
             Log.e(LOG_TAG, "Finish");
	     
	     }

	 }
	
	/** 
	 * Click listener handles sign in and tweet button presses 
	 */
	public void onClick(View v) { 
	    //find view 
	    switch(v.getId()) { 
	        //sign in button pressed 
	        case R.id.signin: 
	            //take user to twitter authentication web page to allow app access to their twitter account 
	            String authURL = niceRequestToken.getAuthenticationURL(); 
	            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
	                    Intent.FLAG_ACTIVITY_SINGLE_TOP)); 
	            break;
	    default: 
	        break; 
	    } 
	}
	
	/* 
	 * onNewIntent fires when user returns from Twitter authentication Web page 
	 */
	@Override
	protected void onNewIntent(Intent intent) { 
	    super.onNewIntent(intent);
	    setIntent(intent);
	    
	    //get the retrieved data 
	    Uri twitURI = intent.getData(); 
	    //make sure the url is correct 
	    if(twitURI!=null && twitURI.toString().startsWith(TWIT_URL)) 
	    {
	    	new RequestS(this).execute(twitURI);
	    }
	    else
	    {
	    	Log.e(LOG_TAG, "Sw");
	    }
	}
	
	@Override
	public void onDestroy() {
	    Log.e(LOG_TAG, "onDestroy()");
	    super.onDestroy();
	}
	
}
