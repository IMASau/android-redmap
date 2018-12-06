package au.com.ionata.redmap.screens;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

import com.google.analytics.tracking.android.EasyTracker;

import android.R.bool;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapApplication;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.data.adapters.RegionActionbarAdapter;
import au.com.ionata.redmap.data.adapters.SpeciesCategoriesListViewAdapter;
import au.com.ionata.redmap.data.repositories.ISpeciesCategoryRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesRepository;
import au.com.ionata.redmap.data.tasks.GetSpeciesCategoriesAsyncTask;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;
import au.com.ionata.redmap.http.interfaces.IGetSpeciesCategoriesCompleteInterface;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.overlay.LoadingOverlayListActivityBase;
import au.com.ionata.redmap.screens.gui.GuiTouchManager;

public class SpeciesCategoriesActivity extends LoadingOverlayListActivityBase implements IGetSpeciesCategoriesCompleteInterface, OnNavigationListener {

	protected GuiTouchManager gtm = new GuiTouchManager();

	private RegionActionbarAdapter actionBarNavigationAdapter;
	private RelativeLayout mOtherSpeciesLayout;
	private boolean isLogSightingSelect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_species_categories);
		
		// Set the region filer by current user.region if filtering is null on init
		RedmapContext rc = RedmapContext.getInstance();
		if (rc.Provider.hasCurrentUser() && rc.Provider.getCurrentUser().Region != null){
			rc.FilterRegion = rc.Provider.getCurrentUser().Region;
		}
		
		View empty = findViewById(R.id.empty);
		empty.setVisibility(View.GONE);

		mOtherSpeciesLayout = (RelativeLayout)findViewById(R.id.otherSpeciesLayout);
		mOtherSpeciesLayout.setVisibility(View.GONE);
		
		// Show the Up button in the action bar.
		setupActionBar();

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("isLogSightingSelect")) {
			isLogSightingSelect = extras.getBoolean("isLogSightingSelect");
			mOtherSpeciesLayout.setVisibility(View.VISIBLE);
		}
	}

	public void confirmOtherSpeciesOnClick(View view){
		EditText field = (EditText)findViewById(R.id.otherSpeciesName);
		String text = field.getText().toString();
		
		if (!text.isEmpty()){
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_OK, resultIntent);
			resultIntent.putExtra("otherSpecies", text);
			finish();
			return;
		}
		else
		{
			field.setError("Required field");
		}
	}

	private void loadSpeciesCategories() {
		// load categories
		showLoadingOverlay();
		GetSpeciesCategoriesAsyncTask at = new GetSpeciesCategoriesAsyncTask(this, getApplicationContext());
		at.execute();
	}

	private void renderSpeciesCategories(List<SpeciesCategory> speciesCategories, Region region) {
		Context ctx = getApplicationContext();
		int layout = R.layout.listitem_species_categories;
		List<String> titleStrings = new ArrayList<String>(speciesCategories.size());

		for (SpeciesCategory speciesCategory : speciesCategories) {
			titleStrings.add(speciesCategory.Description);
		}

		setListAdapter(new SpeciesCategoriesListViewAdapter(ctx, layout, titleStrings.toArray(new String[titleStrings.size()]), speciesCategories));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		SpeciesCategory speciesCategory = (SpeciesCategory) v.getTag();

		Intent intent = new Intent(getApplicationContext(), SpeciesListingActivity.class);
		intent.putExtra("speciesCategoryId", speciesCategory.Id);

		if (isLogSightingSelect) {
			intent.putExtra("isLogSightingSelect", isLogSightingSelect);
			startActivityForResult(intent, LogSightingActivity.AR_SELECT_SPECIES);
			return;
		}

		startActivity(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);
		switch (requestCode) {
			case (LogSightingActivity.AR_SELECT_SPECIES): {
				if (resultCode == Activity.RESULT_OK) {
					setResult(Activity.RESULT_OK, returnedIntent);
					finish();
					return;
				}
				break;
			}
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

		List<Region> regions;
		try {
			regions = provider.AppRepositories.RegionRepository().getAll();
		} catch (SQLException e) {
			regions = new ArrayList<Region>();
			e.printStackTrace();
		}

		// add an empty region at the top
		regions.add(0, new Region() {
			{
				Description = "All regions";
				Id = 0;
			}
		});

		actionBarNavigationAdapter = new RegionActionbarAdapter(getApplicationContext(), regions);

		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		bar.setListNavigationCallbacks(actionBarNavigationAdapter, this);

		// Set the selected item
		Region region = RedmapContext.getInstance().FilterRegion;

		if (region != null && regions.indexOf(region) > 0) {

			bar.setSelectedNavigationItem(regions.indexOf(region));
		}
	}

	/*
	 * public OnItemSelectedListener getOnItemSelectedListener(){
	 * 
	 * OnItemSelectedListener listener = new OnItemSelectedListener() {
	 * 
	 * @Override public void onItemSelected(AdapterView<?> adapter, View view,
	 * int position, long itemId) { loadSpeciesCategories(); }
	 * 
	 * @Override public void onNothingSelected(AdapterView<?> view) { } };
	 * return listener; }
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_general, menu);
		// Configure the search info and add any event listeners
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
	public void onGetSpeciesCategoriesComplete(List<SpeciesCategory> speciesCategories) {
		List<SpeciesCategory> filteredSpeciesCategories = new ArrayList<SpeciesCategory>();
		
		try
		{
			ISpeciesRepository repo = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository();
			for (SpeciesCategory speciesCategory : speciesCategories){
    			if (repo.countAll(RedmapContext.getInstance().FilterRegion, speciesCategory) >= 1)
    			{
    				filteredSpeciesCategories.add(speciesCategory);
    			}
			}
		}
		catch (SQLException ex)
		{
			
		}
		
		renderSpeciesCategories(filteredSpeciesCategories, RedmapContext.getInstance().FilterRegion);
		dismissLoadingOverlay();
	}

	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		Region selectedRegion = actionBarNavigationAdapter.getItem(position);

		if (selectedRegion.Id > 0) {
			RedmapContext.getInstance().FilterRegion = selectedRegion;
		} else {
			RedmapContext.getInstance().FilterRegion = null;
		}
		loadSpeciesCategories();
		return false;
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();

		View empty = findViewById(R.id.empty);
		empty.setVisibility(View.VISIBLE);
		ListView list = getListView();
		list.setEmptyView(empty);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				onBackPressed();
				break;
		}
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