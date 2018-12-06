package au.com.ionata.redmap.screens;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import com.google.analytics.tracking.android.EasyTracker;

import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.api.models.SightingOptionsPrim;
import au.com.ionata.redmap.data.tasks.GetSpeciesByIdAsyncTask;
import au.com.ionata.redmap.http.interfaces.GetSpeciesByIdCompleteInterface;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.overlay.LoadingOverlayActivityBase;
import au.com.ionata.redmap.utils.maps.Point;
import au.com.ionata.redmap.utils.maps.Polygon;

public class SightingDetailActivity extends LoadingOverlayActivityBase {

	private int sightingId;
	private Sighting sighting;
	private Species species;
	private MenuItem shareMenuItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_sighting_detail);
		// Show the Up button in the action bar.

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("sightingId")) {
			sightingId = extras.getInt("sightingId");
			try
            {
	            sighting = RedmapContext.getInstance().Provider.AppRepositories.SightingRepository().getById(sightingId);
	            if (sighting.SpeciesId > 0){
	            	species = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository().getById(sighting.SpeciesId);
	            }
            } 
			catch (SQLException e)
            {
            }
			
			updateInterface();
		}
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_sighting_detail, menu);
		this.shareMenuItem = menu.findItem(R.id.menu_item_share);
		
		// share
		if (sighting != null && sighting.IsValid && shareMenuItem != null){
			shareMenuItem.setVisible(true);
			mSightingShareActionProvider = (ShareActionProvider)shareMenuItem.getActionProvider();
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "Share Sighting");
			intent.putExtra(Intent.EXTRA_TEXT, getSightingShareUrl(sighting.Pk));
			setShareIntent(intent);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateInterface() {
		// gather elements
		TextView speciesLabel = (TextView) findViewById(R.id.speciesLabel);
		TextView speciesCommoneName = (TextView) findViewById(R.id.speciesCommoneName);
		ImageView notValidView = (ImageView) findViewById(R.id.notValidLabel);
		
		TextView spottedOnLabel = (TextView) findViewById(R.id.spottedOnLabel);

		if (species != null){
			speciesLabel.setText(species.SpeciesName);
			speciesCommoneName.setText(species.CommonName);
		}
		else if (sighting.OtherSpecies != null && !sighting.OtherSpecies.isEmpty())
		{
			speciesCommoneName.setText(sighting.OtherSpecies);
			speciesLabel.setText("");
		}
		else
		{
			speciesLabel.setText("");
			speciesCommoneName.setText("");
		}
		
		spottedOnLabel.setText("Spotted on: " + sighting.GetStringLoggingDate());
		
		int validVisible = sighting.IsValid ? View.INVISIBLE : View.VISIBLE;
		notValidView.setVisibility(validVisible);

		// Set the sighting image
		ImageView sightingImageView = (ImageView) findViewById(R.id.sightingImageView);
		if (sighting.HasPicture()) {
			sightingImageView.setImageBitmap(sighting.GetPictureBitmap());
		} else {
			sightingImageView.setImageDrawable(getResources().getDrawable(R.drawable.fish_silhouette));
		}
		
		// draw the sighting location on a map of Aus because we can
		setSightingLocationMap();
	}
	
	private void setSightingLocationMap(){
		ImageView sightingDetailsMap = (ImageView)findViewById(R.id.sightingDetailsMap);
		
		// Make a polygon for the coords of the R.drawable.australia_google_map corners
		GeoPoint nw = new GeoPoint(-7.410849283839832, 112.41210912499992);
		GeoPoint ne = new GeoPoint(-7.410849283839832, 154.07226537499992);
		GeoPoint se = new GeoPoint(-44.248667520681586, 154.07226537499992);
		GeoPoint sw = new GeoPoint(-44.248667520681586, 112.41210912499992);
		
		Polygon.Builder builder = Polygon.Builder();
		builder.addVertex(new Point(nw));
		builder.addVertex(new Point(ne));
		builder.addVertex(new Point(se));
		builder.addVertex(new Point(sw));
		builder.close();
		Polygon polygon = builder.build();
		
		GeoPoint sightingLocation = sighting.getGetPoint();
		
		if (!polygon.contains(new Point(sightingLocation))){
			return;
		}

		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.australia_google_map);
		Bitmap bmOverlay = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
		
		// only attempt to draw the point if it is within the map bounding box
		if (polygon.contains(new Point(sightingLocation))){
			
			double top = ((sightingLocation.getLatitudeE6() / 1000000F) - (nw.getLatitudeE6() / 1000000F)) / ((se.getLatitudeE6() / 1000000F) - (nw.getLatitudeE6() / 1000000F));
			double left = ((sightingLocation.getLongitudeE6() / 1000000F) - (nw.getLongitudeE6() / 1000000F)) / ((se.getLongitudeE6() / 1000000F) - (nw.getLongitudeE6() / 1000000F));
			
    		Canvas canvas = new Canvas(bmOverlay);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(10);
            canvas.drawBitmap(bm, new Matrix(), null);
            canvas.drawCircle(Math.abs(Math.round(bm.getWidth() * left)), Math.abs(Math.round(bm.getHeight() * top)), 6, paint);
		}
		
		sightingDetailsMap.setImageBitmap(bmOverlay);
	}
	
	private String getSightingShareUrl(int id){
		String hostname = RedmapContext.getInstance().GetHostname();
		String protocol = RedmapContext.getInstance().GetProtocol();
		return String.format("%s://%s/sightings/%s/", protocol, hostname, id);
	}
	
	private ShareActionProvider mSightingShareActionProvider;
	
	private void setShareIntent(Intent shareIntent) {
	    if (mSightingShareActionProvider != null) {
	    	mSightingShareActionProvider.setShareIntent(shareIntent);
	    }
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
