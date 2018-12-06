package au.com.ionata.redmap.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.data.adapters.RegionActionbarAdapter;
import au.com.ionata.redmap.data.adapters.SpeciesListViewAdapter;
import au.com.ionata.redmap.data.tasks.GetSpeciesAsyncTask;
import au.com.ionata.redmap.data.tasks.GetSpeciesCategoriesAsyncTask;
import au.com.ionata.redmap.http.interfaces.GetSpeciesCompleteInterface;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.overlay.LoadingOverlayListActivityBase;

public class SpeciesListingActivity extends LoadingOverlayListActivityBase implements GetSpeciesCompleteInterface, OnNavigationListener, OnQueryTextListener, MenuItem.OnActionExpandListener {
	
	private int speciesCategoryId = 0;
	
	private RegionActionbarAdapter actionBarNavigationAdapter;
	
	private ArrayAdapter<Species> spiecesAdapter;
	
	private boolean isLogSightingSelect = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_species_listing);
		View empty = findViewById(R.id.empty);
		empty.setVisibility(View.GONE);
		setupActionBar();
		
    	Bundle extras = getIntent().getExtras();
    	if (extras != null && extras.containsKey("isLogSightingSelect")){
    		isLogSightingSelect = extras.getBoolean("isLogSightingSelect");
    	}
	}

    private void loadSpecies(){
        // load categories
    	showLoadingOverlay();
    	
    	Region fitlerRegion = RedmapContext.getInstance().FilterRegion; // nullable for lolz
    	
    	// Category id
    	Bundle extras = getIntent().getExtras();
    	if (extras != null && extras.containsKey("speciesCategoryId")){
    		speciesCategoryId = extras.getInt("speciesCategoryId");
    	}
    	GetSpeciesAsyncTask at = new GetSpeciesAsyncTask(this, getApplicationContext(), fitlerRegion, speciesCategoryId);
        at.execute();
    }
    
	private void renderSpecies(ArrayList<Species> species) {
		Context ctx = getApplicationContext();
		int layout = R.layout.listitem_species;
		List<String> titleStrings = new ArrayList<String>(
		        species.size());

		for (Species speciesEntity: species) {
			titleStrings.add(speciesEntity.SpeciesName);
		}

		spiecesAdapter = new SpeciesListViewAdapter(
			ctx,
			layout,
		    species
		);
		
		setListAdapter(spiecesAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Species species= (Species)v.getTag();
		
		// Return to the log sighting if we're selecting a species
		if (isLogSightingSelect){
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_OK, resultIntent);
			resultIntent.putExtra("speciesId", species.Id);
			finish();
			return;
		}
		
		Intent intent = new Intent(getApplicationContext(), SpeciesDetailActivity.class);
		intent.putExtra("speciesId", species.Id);
		intent.putExtra("speciesCategoryId", speciesCategoryId);
		startActivity(intent);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		List<Region> regions;
        try {
	        regions = provider.AppRepositories.RegionRepository().getAll();
        } catch (SQLException e) {
	        regions = new ArrayList<Region>();
	        e.printStackTrace();
        }
        
        // add an empty region at the top
        regions.add(0, new Region(){{ Description="All regions"; Id = 0; }});
		
		actionBarNavigationAdapter = new RegionActionbarAdapter(getApplicationContext(), regions);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(actionBarNavigationAdapter, this);
	    
	    // Set the selected item
	    Region region = RedmapContext.getInstance().FilterRegion;

	    if (region != null && regions.indexOf(region) > 0){
        	
	    	actionBar.setSelectedNavigationItem(regions.indexOf(region));
        }
	}
	
	@Override
	public void onGetSpeciesComplete(Collection<Species> species) {
		renderSpecies((ArrayList<Species>) species);
		dismissLoadingOverlay();
	}
	
	@Override
    public boolean onNavigationItemSelected(int position, long itemId) {
		Region selectedRegion = actionBarNavigationAdapter.getItem(position);

        if (selectedRegion.Id > 0) {
        	RedmapContext.getInstance().FilterRegion = selectedRegion;
        }else{
        	RedmapContext.getInstance().FilterRegion = null;
        }
        
        loadSpecies();
	    return false;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_species_listing, menu);

	    // Add search listeners
	    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
	    searchView.setOnQueryTextListener(this);
	    
	    // SearchView setOnCloseListener method is broken in android 4.0+, here's the hack using the menuitem
	    MenuItem menuItem = menu.findItem(R.id.action_search);
	    menuItem.setOnActionExpandListener(this);
	    return true;
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		spiecesAdapter.getFilter().filter(query);
		return true;
	}
	
	@Override
	public boolean onQueryTextChange(String query) {
		spiecesAdapter.getFilter().filter(query);
		return true;
	}
	
	@Override
	public void onContentChanged() {
	    super.onContentChanged();

	    View empty = findViewById(R.id.empty);
	    empty.setVisibility(View.VISIBLE);
	    ListView list = getListView();
	    list.setEmptyView(empty);
	}

	
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		return true;
	}
	
	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		spiecesAdapter.getFilter().filter(null);
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
