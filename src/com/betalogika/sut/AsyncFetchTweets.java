package com.betalogika.sut;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

public class AsyncFetchTweets extends AsyncTask<Void,String,String> {
	
	/**twitter authentication key*/
	public final static String TWIT_KEY = "Rkx3NH2IFVeGoBZUenl1sDb3a"; 
	    /**twitter secret*/
	public final static String TWIT_SECRET = "Pu3tkMkPrBqerAxjfs0Hv0yF59xiX5UCIyUj7y0YTsjjb2VfHu"; 
	    /**twitter object*/
	private Twitter timelineTwitter;
	
	    /**timeline database*/
	private SQLiteDatabase niceDB;
	
		/**shared preferences for user details*/
	private SharedPreferences nicePrefs;
	
	TweetListActivity context;
	
    //debugging tag 
	private String LOG_TAG = "TimelineService";
	
	public AsyncFetchTweets(TweetListActivity a){
		this.context=a;
			//get prefs 
     	nicePrefs = context.getSharedPreferences("TwitNicePrefs", 0); 
     	
     		//get the database 
     	niceDB = context.timelineDB;
    
    	//get user preferences 
     	String userToken = nicePrefs.getString("user_token", null); 
     	String userSecret = nicePrefs.getString("user_secret", null);
    
     	Log.e(LOG_TAG, "Exception: "+userToken);
     	Log.e(LOG_TAG, "Exception: " + userSecret);
	    Log.e(LOG_TAG, "Exception: " + TWIT_KEY);
	    Log.e(LOG_TAG, "Exception: " + TWIT_SECRET);
	      
	        //create new configuration 
	    Configuration twitConf = new ConfigurationBuilder() 
        	.setDebugEnabled(true)
        	.setOAuthConsumerKey(TWIT_KEY) 
        	.setOAuthConsumerSecret(TWIT_SECRET) 
        	.setOAuthAccessToken(userToken) 
        	.setOAuthAccessTokenSecret(userSecret) 
        	.build(); 
        //instantiate new twitter 
	    timelineTwitter = new TwitterFactory(twitConf).getInstance();
	}
	
	protected String doInBackground(Void... params) {
	      String progress_data = null;
	      String result = "A";
	      
			try { 
			    //fetch timeline
					//retrieve the new home timeline tweets as a list 
				List<twitter4j.Status> homeTimeline = timelineTwitter.getHomeTimeline();
				//iterate through new status updates 
				for (twitter4j.Status statusUpdate : homeTimeline)  
				{ 
				        //call the getValues method of the data helper class, passing the new updates 
				    ContentValues timelineValues = NiceDataHelper.getValues(statusUpdate); 
				        //if the database already contains the updates they will not be inserted 
				    niceDB.insertOrThrow("home", null, timelineValues);
				}
			}  
			catch (Exception te) { Log.e(LOG_TAG, "Exception: " + te); }
		    
	      //while the long job getting done {
	         //update progress_data
	         //update result 
	         publishProgress(progress_data);
	      //}
	         
	      return result;
	}
	protected void onProgressUpdate(String... progress_data) {
        //use progress_data to show progress on the screen
    }
	
	@Override
    protected void onPostExecute(String result) {
        //use result obtained at the end of 
    	Update();
    	Log.d("SuT", "Successkah?");
		context.rendertweet();
    }
	
	void Update() {
		int rowLimit = 13; 
		if(DatabaseUtils.queryNumEntries(niceDB, "home")>rowLimit) { 
		    String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " + 
		        "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " + 
		        "limit "+rowLimit+")";   
		    niceDB.execSQL(deleteQuery); 
		}

	}
}
