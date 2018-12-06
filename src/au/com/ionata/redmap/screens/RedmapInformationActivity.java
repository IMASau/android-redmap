package au.com.ionata.redmap.screens;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

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
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.overlay.BaseActivity;

public class RedmapInformationActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_how_you_help);
		// Show the Up button in the action bar.
		setupActionBar();
		
		WebView webView = (WebView)findViewById(R.id.webView);
		InputStream inputStream;
        try
        {
	        inputStream = getAssets().open("info.html");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
			while ((line = reader.readLine()) != null) {
		        sb.append(line);
		    }
			inputStream.close();
		    String html = sb.toString();
		    html = html.replace("{{REDMAP_URL}}", RedmapContext.getInstance().GetBaseUrl());
		    html = html.replace("{{API_BASE}}", RedmapContext.getInstance().GetApiServerName());
			webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);

        } catch (IOException e)
        {
	        e.printStackTrace();
        }
		
	}

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
