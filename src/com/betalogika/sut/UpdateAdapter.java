package com.betalogika.sut;

import java.io.InputStream;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateAdapter extends SimpleCursorAdapter {

		/**twitter developer key*/
	public final static String TWIT_KEY = "Rkx3NH2IFVeGoBZUenl1sDb3a";//alter 
	    /**twitter developer secret*/
	public final static String TWIT_SECRET = "Pu3tkMkPrBqerAxjfs0Hv0yF59xiX5UCIyUj7y0YTsjjb2VfHu";//alter 
	      
	    /**strings representing database column names to map to views*/
	static final String[] from = { "update_text", "user_screen",  
	    "update_time", "user_img" }; 
	    /**view item IDs for mapping database record values to*/
	static final int[] to = { R.id.updateText, R.id.userScreen,  
	    R.id.updateTime, R.id.userImg }; 
	      
	private String LOG_TAG = "UpdateAdapter";
	
	/** 
	 * constructor sets up adapter, passing 'from' data and 'to' views 
	 * @param context 
	 * @param c 
	 */
	@SuppressWarnings("deprecation")
	public UpdateAdapter(Context context, Cursor c) { 
	    super(context, R.layout.update, c, from, to); 
	}
	
	/* 
	 * Bind the data to the visible views 
	 */
	@Override
	public void bindView(View row, Context context, Cursor cursor) { 
	    super.bindView(row, context, cursor);
	    	//get the image
	    new RequestImage(row,cursor).execute();
	    	//get the update time 
	    long createdAt = cursor.getLong(cursor.getColumnIndex("update_time")); 
	        //get the update time view 
	    TextView textCreatedAt = (TextView)row.findViewById(R.id.updateTime); 
	        //adjust the way the time is displayed to make it human-readable 
	    textCreatedAt.setText(DateUtils.getRelativeTimeSpanString(createdAt)+" ");
	    
	    	//get the status ID 
	    long statusID = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)); 
	        //get the user name 
	    String statusName = cursor.getString(cursor.getColumnIndex("user_screen"));
	    	//create a StatusData object to store these 
	    StatusData tweetData = new StatusData(statusID, statusName);
	    	//set the status data object as tag for both retweet and reply buttons in this view 
	    row.findViewById(R.id.retweet).setTag(tweetData); 
	    row.findViewById(R.id.reply).setTag(tweetData);
	    	//setup onclick listeners for the retweet and reply buttons 
	    row.findViewById(R.id.retweet).setOnClickListener(tweetListener); 
	    row.findViewById(R.id.reply).setOnClickListener(tweetListener);
	    	//setup  onclick for the user screen name within the tweet 
	    row.findViewById(R.id.userScreen).setOnClickListener(tweetListener);
	}
	
	/** 
	 * tweetListener handles clicks of reply and retweet buttons 
	 * - also handles clicking the user name within a tweet 
	 */
	private OnClickListener tweetListener = new OnClickListener() { 
	    //onClick method 
	    public void onClick(View v) { 
	    	//which view was clicked 
	    	switch(v.getId()) { 
	    	        //reply button pressed 
	    	    case R.id.reply: 
	    	        //implement reply 
	    	    	//create an intent for sending a new tweet 
	    	    	Intent replyIntent = new Intent(v.getContext(), NiceTweet.class); 
	    	    	    //get the data from the tag within the button view 
	    	    	StatusData theData = (StatusData)v.getTag(); 
	    	    	    //pass the status ID 
	    	    	replyIntent.putExtra("tweetID", theData.getID()); 
	    	    	    //pass the user name 
	    	    	replyIntent.putExtra("tweetUser", theData.getUser()); 
	    	    	    //go to the tweet screen 
	    	    	v.getContext().startActivity(replyIntent);
	    	  
	    	        break; 
	    	        //retweet button pressed 
	    	    case R.id.retweet: 
	    	        //implement retweet 
	    	    		//get context 
	    	    	Context appCont = v.getContext(); 
	    	    	    //get preferences for user access 
	    	    	SharedPreferences tweetPrefs = appCont.getSharedPreferences("TwitNicePrefs", 0); 
	    	    	String userToken = tweetPrefs.getString("user_token", null); 
	    	    	String userSecret = tweetPrefs.getString("user_secret", null); 
	    	    	  
	    	    	    //create new Twitter configuration 
	    	    	Configuration twitConf = new ConfigurationBuilder() 
	    	    		.setDebugEnabled(true)
	    	    		.setOAuthConsumerKey(TWIT_KEY) 
	    	    	    .setOAuthConsumerSecret(TWIT_SECRET) 
	    	    	    .setOAuthAccessToken(userToken) 
	    	    	    .setOAuthAccessTokenSecret(userSecret) 
	    	    	    .build(); 
	    	    	  
	    	    	    //create Twitter instance for retweeting 
	    	    	Twitter retweetTwitter = new TwitterFactory(twitConf).getInstance();
	    	    		//get tweet data from view tag 
	    	    	StatusData tweetData = (StatusData)v.getTag();
	    	    	new Retweet(appCont,retweetTwitter).execute(tweetData);
	    	    	
	    	        break; 
	    	        //user has pressed tweet user name 
	    	    case R.id.userScreen: 
	    	        //implement visiting user profile 
	    	    		//get the user screen name 
	    	    	TextView tv = (TextView)v.findViewById(R.id.userScreen); 
	    	    	String userScreenName = tv.getText().toString(); 
	    	    	    //open the user's profile page in the browser 
	    	    	Intent browserIntent = new Intent(Intent.ACTION_VIEW,  
	    	    	    Uri.parse("http://twitter.com/"+userScreenName)); 
	    	    	v.getContext().startActivity(browserIntent);
	    	  
	    	        break; 
	    	    default: 
	    	        break; 
	    	}
	    } 
	};
	
	private class RequestImage extends AsyncTask<Void, String,String> {
	    View rows;
	    Cursor cursors;
		RequestImage(View a,Cursor b){
			this.rows=a;
			this.cursors=b;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		protected String doInBackground(Void... urls) {
			try { 
			        //get profile image 
			    URL profileURL = new URL(cursors.getString(cursors.getColumnIndex("user_img"))); 
			  
			        //set the image in the view for the current tweet 
			    ImageView profPic = (ImageView) rows.findViewById(R.id.userImg); 
			    profPic.setImageDrawable(Drawable.createFromStream 
			        ((InputStream)profileURL.getContent(), "")); 
			} 
			catch(Exception te) 
		    { 
				Log.e(LOG_TAG, te.getMessage());
			}
	    	
	    	 return null;
	     }

	 }
	
	private class Retweet extends AsyncTask<StatusData, String,String> {
		Context ct;
	    Twitter rt;
		Retweet(Context a,Twitter b){
			this.ct=a;
			this.rt=b;
		}
		
		protected String doInBackground(StatusData... urls) {
	    	StatusData tweetData=urls[0];
	    	 try 
 	    	{ 
 	    	        //retweet, passing the status ID from the tag 
 	    	    rt.retweetStatus(tweetData.getID()); 
 	    	    
 	    	} 
 	    	catch(TwitterException te) {Log.e(LOG_TAG, te.getMessage());}
	    	
	    	 return null;
	     }
		
		@Override
	    protected void onPostExecute(String result) {
			CharSequence text = "Retweeted!"; 
    	    int duration = Toast.LENGTH_SHORT; 
    	    Toast toast = Toast.makeText(ct, text, duration); 
    	    toast.show(); 
	    }

	 }
}
