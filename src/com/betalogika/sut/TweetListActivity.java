package com.betalogika.sut;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class TweetListActivity extends Activity implements OnClickListener {

	/**developer account key for this app*/
	public final static String TWIT_KEY = "Rkx3NH2IFVeGoBZUenl1sDb3a"; 
	/**developer secret for the app*/
	public final static String TWIT_SECRET = "Pu3tkMkPrBqerAxjfs0Hv0yF59xiX5UCIyUj7y0YTsjjb2VfHu"; 
	/**app url*/
	public final static String TWIT_URL = "sut-android://betalogika";
	  
	//for error logging 
	private String LOG_TAG = "SutActivity";//alter for your Activity name
	
	/**main view for the home timeline*/
	ListView homeTimeline; 
	    /**database helper for update data*/
	NiceDataHelper timelineHelper; 
	    /**update database*/
	SQLiteDatabase timelineDB; 
	    /**cursor for handling data*/
	private Cursor timelineCursor; 
	    /**adapter for mapping data*/
	private UpdateAdapter timelineAdapter;
	
	/**shared preferences to store user details*/
	private SharedPreferences nicePrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
	    //get the timeline 
			//get reference to the list view 
		homeTimeline = (ListView)findViewById(R.id.homeList);
			//instantiate database helper 
		timelineHelper = new NiceDataHelper(this); 
		    //get the database 
		timelineDB = timelineHelper.getReadableDatabase();
		
		rendertweet();
		
		//setup onclick listener for tweet button 
		ImageView tweetClicker = (ImageView) findViewById(R.id.tweetbtn); 
		tweetClicker.setOnClickListener(this);
		
		
		AsyncFetchTweets update = new AsyncFetchTweets(this);
		update.execute();
		
	}
	
	public void onClick(View v) { 
	    //find view 
	    switch(v.getId()) { 
	    //other listeners here 
	          //user has pressed tweet button 
	        case R.id.tweetbtn: 
	                //launch tweet activity 
	            startActivity(new Intent(this, NiceTweet.class)); 
	            break;
	    default: 
	        break; 
	    } 
	}
	
	@SuppressWarnings("deprecation")
	void rendertweet(){
		//query the database, most recent tweets first 
		timelineCursor = timelineDB.query 
		    ("home", null, null, null, null, null, "update_time DESC"); 
		    //manage the updates using a cursor 
		startManagingCursor(timelineCursor); 
		    //instantiate adapter 
		timelineAdapter = new UpdateAdapter(this, timelineCursor);
		
			//this will make the app populate the new update data in the timeline view 
		homeTimeline.setAdapter(timelineAdapter);
	}
	
	@Override
	public void onDestroy() {
	    Log.e(LOG_TAG, "onDestroy()");
	    super.onDestroy();
	    try {  
	        timelineDB.close(); 
	    } 
	    catch(Exception se) { Log.e(LOG_TAG, "unable to stop Service or receiver"); } 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, TweetListActivity.class));
			finish();
			return true;
		}
		else if (id == R.id.Logout){
			//clear the preferences
			nicePrefs = getSharedPreferences("TwitNicePrefs", 0);
			nicePrefs.edit().clear().commit();
			startActivity(new Intent(this, SutActivity.class));
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
