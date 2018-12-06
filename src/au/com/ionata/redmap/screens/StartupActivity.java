package au.com.ionata.redmap.screens;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.overlay.BaseActivity;

public class StartupActivity extends BaseActivity {

	private boolean left = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		leaveStartUp();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Splash timer
		new Handler().postDelayed(new Runnable() { 
			public void run() {
				leaveStartUp();
			}
		}, 2000);
	}
	
	protected void leaveStartUp(){
		// Move to main screen from loader
		if (left) return;
		
		left = true;
		Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_general, menu);
		return true;
	}

	@Override
	protected void onStart()
	{
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onStop()
	{
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);
	}
}
