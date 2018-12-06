package au.com.ionata.redmap.screens;

import java.io.File;
import java.io.InputStream;

import com.google.analytics.tracking.android.EasyTracker;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.R.layout;
import au.com.ionata.redmap.R.menu;
import au.com.ionata.redmap.overlay.BaseActivity;

public class HowYouHelpActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_how_you_help);
		// Show the Up button in the action bar.
		setupActionBar();
		
		WebView webView = (WebView)findViewById(R.id.webView);
		webView.loadUrl("file:///android_asset/helping.html");
		webView.setWebViewClient(webViewClient);
	}
	
	protected WebViewClient webViewClient = new WebViewClient()
    {
        // Override URL
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
        	if (url.equals("redmap://spot"))
        	{
    			Intent intent = new Intent(getApplicationContext(), SpeciesCategoriesActivity.class);
                startActivity(intent);
                return true;
        	}
        	else if (url.equals("redmap://log"))
        	{
    			Intent intent = new Intent(getApplicationContext(), LogSightingActivity.class);
                startActivity(intent);
                return true;
        	}
        	return false;
        }
    };

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
