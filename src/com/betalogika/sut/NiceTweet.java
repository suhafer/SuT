package com.betalogika.sut;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class NiceTweet extends Activity implements OnClickListener {

		/**shared preferences for user twitter details*/
	private SharedPreferences tweetPrefs; 
	    /**twitter object**/
	private Twitter tweetTwitter; 
	  
	    /**twitter key*/
	public final static String TWIT_KEY = "Rkx3NH2IFVeGoBZUenl1sDb3a"; 
	    /**twitter secret*/
	public final static String TWIT_SECRET = "Pu3tkMkPrBqerAxjfs0Hv0yF59xiX5UCIyUj7y0YTsjjb2VfHu"; 
	  
	    /**the update ID for this tweet if it is a reply*/
	private long tweetID = 0; 
	    /**the username for the tweet if it is a reply*/
	private String tweetName = "";
	
	/* 
	 * onCreate called when activity is created 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) { 
	    super.onCreate(savedInstanceState); 
	        //set tweet layout 
	    setContentView(R.layout.tweet);   
	}
	
	/* 
	 * Call setup method when this activity starts 
	 */
	@Override
	public void onResume() { 
	    super.onResume(); 
	        //call helper method 
	    setupTweet(); 
	}
	
	/** 
	 * Method called whenever this Activity starts 
	 * - get ready to tweet 
	 * Sets up twitter and onClick listeners 
	 * - also sets up for replies 
	 */
	private void setupTweet() { 
	    //prepare to tweet 
			//get preferences for user twitter details 
		tweetPrefs = getSharedPreferences("TwitNicePrefs", 0); 
		          
		        //get user token and secret for authentication 
		String userToken = tweetPrefs.getString("user_token", null); 
		String userSecret = tweetPrefs.getString("user_secret", null); 
		  
		    //create a new twitter configuration usign user details 
		Configuration twitConf = new ConfigurationBuilder() 
			.setDebugEnabled(true)
			.setOAuthConsumerKey(TWIT_KEY) 
		    .setOAuthConsumerSecret(TWIT_SECRET) 
		    .setOAuthAccessToken(userToken) 
		    .setOAuthAccessTokenSecret(userSecret) 
		    .build(); 
		  
		    //create a twitter instance 
		tweetTwitter = new TwitterFactory(twitConf).getInstance();
			//get any data passed to this intent for a reply 
		Bundle extras = getIntent().getExtras();
		if(extras !=null) 
		{ 
		        //get the ID of the tweet we are replying to 
		    tweetID = extras.getLong("tweetID"); 
		        //get the user screen name for the tweet we are replying to 
		    tweetName = extras.getString("tweetUser"); 
		  
		        //use the passed information
		    	//get a reference to the text field for tweeting 
		    EditText theReply = (EditText)findViewById(R.id.tweettext); 
		        //start the tweet text for the reply @username 
		    theReply.setText("@"+tweetName+" "); 
		        //set the cursor to the end of the text for entry 
		    theReply.setSelection(theReply.getText().length());
		  
		}
		else 
		{ 
		        EditText theReply = (EditText)findViewById(R.id.tweettext); 
		        theReply.setText(""); 
		}
		
		//set up listener for choosing home button to go to timeline
		ImageView tweetClicker = (ImageView) findViewById(R.id.homebtn); 
		tweetClicker.setOnClickListener(this);
		          
		    //set up listener for send tweet button 
		Button tweetButton = (Button)findViewById(R.id.dotweet); 
		tweetButton.setOnClickListener(this);
	  
	}
	
	/** 
	 * Listener method for button clicks 
	 * - for home button and send tweet button 
	 */
	public void onClick(View v) { 
	    //handle home and send button clicks 
		EditText tweetTxt = (EditText)findViewById(R.id.tweettext);
			//find out which view has been clicked 
		switch(v.getId()) { 
		case R.id.dotweet: 
		    //send tweet 
			String toTweet = tweetTxt.getText().toString(); 
			new Tweet().execute(toTweet);
			   //reset the edit text 
		    tweetTxt.setText("");
		    break; 
		case R.id.homebtn: 
		    //go to the home timeline 
			tweetTxt.setText("");
		  
		    break; 
		default: 
		    break; 
		}
		finish();
	  
	}
	
	private class Tweet extends AsyncTask<String, String,String> {
		
		protected String doInBackground(String... urls) {
			String toTweet=urls[0];
			try { 
			        //handle replies 
			    if(tweetName.length()>0) 
			        tweetTwitter.updateStatus(new StatusUpdate(toTweet).inReplyToStatusId(tweetID)); 
			          
			        //handle normal tweets 
			    else 
			        tweetTwitter.updateStatus(toTweet); 
			} 
			catch(TwitterException te) { Log.e("NiceTweet", te.getMessage()); }
				return null;
		    }
		
		@Override
	    protected void onPostExecute(String result) {
			
	    }

	 }
}
