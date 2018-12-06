package au.com.ionata.redmap.screens;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.util.constants.MapViewConstants;

import com.google.analytics.tracking.android.EasyTracker;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;

public class LogSightingLocationActivity extends Activity implements LocationListener, MapViewConstants {

	private MapView mMapView;
	private MapController mMapController;
	private LocationManager mLocationManager;
	private ItemizedIconOverlay<OverlayItem> mMyLocationOverlay;
	private ResourceProxy mResourceProxy;
	private GeoPoint mCurrentGeoPoint;

	private ProgressDialog findCurrentLocationDialog;
	private int findLocationBackOffCounter = 0;
	
	private Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_sighting_location);
		// Show the Up button in the action bar.
		setupActionBar();
		setupMapView();
	}

	private void setupMapView() {
		mMapView = (MapView) findViewById(R.id.log_sighting_map_view);
		mMapController = mMapView.getController();
		mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

		mMapView.setMultiTouchControls(true);
		mMapView.setTileSource(TileSourceFactory.MAPNIK);
		mMapView.setBuiltInZoomControls(true);

		// icon
		Drawable marker = getResources().getDrawable(R.drawable.ic_marker);
		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(10, markerHeight, markerWidth, 0);

		/* OnTapListener for the Markers, shows a simple Toast. */
		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items, marker, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
			@Override
			public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				return false;
			}

			@Override
			public boolean onItemLongPress(final int index, final OverlayItem item) {
				return false;
			}
		}, mResourceProxy);

		mMapView.getOverlays().add(mMyLocationOverlay);

		// / Touch event handler
		MapEventsOverlay OverlayEventos = new MapEventsOverlay(getBaseContext(), mReceive);
		mMapView.getOverlays().add(OverlayEventos);

		
		// Move to existing point or centre on Aus.
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("latitude") && extras.containsKey("longitude")) {
			Double latitude = extras.getDouble("latitude");
			Double longitude = extras.getDouble("longitude");
			if (latitude != null && longitude != null) {
				mCurrentGeoPoint = new GeoPoint(latitude, longitude);
				setMarker(mCurrentGeoPoint);
				mMapController.setZoom(6);
				mMapController.setCenter(mCurrentGeoPoint);
			}
		} else {
			GeoPoint point = new GeoPoint(-23.7, 132.8); // centre of aus-ish
			mMapController.setZoom(4);
			mMapController.setCenter(point);
		}
		
		// Redraw
		mMapView.invalidate();

	}

	MapEventsReceiver mReceive = new MapEventsReceiver() {

		@Override
		public boolean singleTapUpHelper(IGeoPoint arg0) {
			// Log.d("debug", "Single tap helper");
			// your onSingleTap logic here
			return false;
		}

		@Override
		public boolean longPressHelper(IGeoPoint arg0) {
			// Log.d("debug", "LongPressHelper");
			// your onLongPress logic here
			setMarker(arg0);
			return false;
		}
	};

	protected void setMarker(IGeoPoint point) {
		GeoPoint touchPoint = (GeoPoint) point;
		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		items.add(new OverlayItem("Here", "SampleDescription", touchPoint));

		mMyLocationOverlay.removeAllItems();
		mMyLocationOverlay.addItem(new OverlayItem("Here", "SampleDescription", touchPoint));

		mCurrentGeoPoint = new GeoPoint(touchPoint);

		// Redraw
		mMapView.invalidate();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public MenuItem.OnMenuItemClickListener actionBarMyLocationOnClickHandler = new MenuItem.OnMenuItemClickListener()
	{
		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			ActionBarMyLocationOnClick();
			return true;
		}
	};
	
	public MenuItem.OnMenuItemClickListener actionBarConfirmLocationOnClickHandler = new MenuItem.OnMenuItemClickListener()
	{
		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			ActionBarConfirmOnClick();
			return true;
		}
	};
	
	
	
	public void ActionBarMyLocationOnClick() {
		startLocationUpdates();
	}
	
	private void showBasicAlert(String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("Ok", null);
		builder.create().show();
	}

	private void locationNotSelectedAlert(){
		String title = "Select sighting location";
		String msg = "You haven't selected the sighting location! Press and hold on the map to place the sighting marker.";
		showBasicAlert(title, msg);
	}
	

	private void locationNotInAustraliaAlert(){
		String title = "Location not in Australian waters";
		String msg = "The location you selected is not in Australian waters!";
		showBasicAlert(title, msg);
	}
	
	public void ActionBarConfirmOnClick() {

		if (mCurrentGeoPoint == null) {
			locationNotSelectedAlert();
			return;
		} /*else if (!RedmapContext.getInstance().GetAustralianGeoService().Contains(mCurrentGeoPoint)){
			locationNotInAustraliaAlert();
			return;
		}*/

		Intent resultIntent = new Intent();
		setResult(Activity.RESULT_OK, resultIntent);
		double latitude = mCurrentGeoPoint.getLatitudeE6() / 1000000F;
		double longitude = mCurrentGeoPoint.getLongitudeE6() / 1000000F;
		resultIntent.putExtra("latitude", latitude);
		resultIntent.putExtra("longitude", longitude);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_log_sighting_location, menu);
		
		MenuItem actionMyLocationMenuItem = menu.findItem(R.id.action_target_my_location);
		actionMyLocationMenuItem.setOnMenuItemClickListener(actionBarMyLocationOnClickHandler);
		
		MenuItem actionConfirmMenuItem = menu.findItem(R.id.action_confirm_coordinates);
		actionConfirmMenuItem.setOnMenuItemClickListener(actionBarConfirmLocationOnClickHandler);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void onLocationChanged(Location location) {
		// Only accept an accuracy update when less than 100m
		if (location.getAccuracy() < 100){
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			GeoPoint point = new GeoPoint(lat, lng);
			setMarker(point);
			mMapController.setZoom(6);
			findLocationBackOff();
			if (findCurrentLocationDialog.isShowing()){
				mMapController.setCenter(point);
				findCurrentLocationDialog.dismiss();
			}
			mHandler.removeCallbacksAndMessages(null);
		}
	}

	private void findLocationBackOff(){
		findLocationBackOffCounter++;
		if (findLocationBackOffCounter >= 5){
			stopLocationUpdates();
		}
	}
	
	private void getLocationFailed(Context context){
		Toast.makeText(context, "Could not find your location", Toast.LENGTH_SHORT).show();
	}
	
	private void startLocationUpdates(){
		findLocationBackOffCounter = 0;
		mHandler.postDelayed(getLocationTimeoutRunable(), 31000); //31secs, because update interval of 3000x10 below allows one extra tick
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
		findCurrentLocationDialog = ProgressDialog.show(mMapView.getContext(), "", "Searching for your location", true, true, locationOnCanceledHandler);
	}
	
	private OnCancelListener locationOnCanceledHandler = new OnCancelListener()
	{
		@Override
		public void onCancel(DialogInterface dialog)
		{
			stopLocationUpdates();
		}
	};
	
	private void stopLocationUpdates(){
		mLocationManager.removeUpdates(this);
		if (findCurrentLocationDialog.isShowing()){
			findCurrentLocationDialog.dismiss();
		}
	}
	
	private Runnable getLocationTimeoutRunable(){
		return new Runnable() {
			final Context context = getApplicationContext();
    	    public void run() {
        		stopLocationUpdates();
        		getLocationFailed(context);
    	    }
		};
	};
	

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
