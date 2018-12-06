package au.com.ionata.redmap.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.util.constants.MapViewConstants;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.adapters.SightingListViewAdapter;
import au.com.ionata.redmap.data.repositories.ISightingRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesRepository;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.User;
import au.com.ionata.redmap.overlay.FragmentBaseActivity;
import au.com.ionata.redmap.utils.maps.Point;
import au.com.ionata.redmap.utils.maps.Polygon;
import au.com.ionata.redmap.utils.maps.ResourceProxyImpl;

public class PersonalMapActivity extends FragmentBaseActivity implements ActionBar.TabListener
{

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	protected List<Sighting> sightings;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_personal_map);
		loadSightings();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
		{
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}
	}

	public MenuItem.OnMenuItemClickListener actionBarRefreshOnClickHandler = new MenuItem.OnMenuItemClickListener()
	{
		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			RefreshDataOnClick();
			return true;
		}
	};
	
	public void RefreshDataOnClick()
	{
		User user = RedmapContext.getInstance().Provider.getCurrentUser();
		RedmapContext.getInstance().Provider.AsyncCurrentUserSightingsSync();
	}

	private void loadSightings()
	{
		if (RedmapContext.getInstance().Provider.hasCurrentUser())
		{
			loadUserSightings();
		} else
		{
			loadOfflineSightings();
		}
	}

	private void loadUserSightings()
	{
		ISightingRepository sightingRepopository;
		ISpeciesRepository speciesRepopository;
		User user;
		try
		{
			sightingRepopository = RedmapContext.getInstance().Provider.AppRepositories.SightingRepository();
			speciesRepopository = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository();

			user = RedmapContext.getInstance().Provider.AppRepositories.UserRepository().getCurrentUser();
			sightings = sightingRepopository.getSightingsByUser(user);
			
			// merge offline sightings
			List<Sighting> offlineSightings = sightingRepopository.getPendingSightings();
			for (Sighting sighting : offlineSightings){
				sightings.add(0, sighting);
			}

			for (Sighting sighting : sightings)
			{
				if (sighting.SpeciesId > 0)
				{
					Species species = speciesRepopository.getById(sighting.SpeciesId);
					if (species != null)
					{
						sighting.Species = species;
					}
				}
			}

		} catch (SQLException e)
		{
			throw new RedmapRuntimeException(e);
		}
	}

	private void loadOfflineSightings()
	{
		ISightingRepository sightingRepopository;
		ISpeciesRepository speciesRepopository;
		try
		{
			sightingRepopository = RedmapContext.getInstance().Provider.AppRepositories.SightingRepository();
			speciesRepopository = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository();

			sightings = sightingRepopository.getPendingSightings();

			for (Sighting sighting : sightings)
			{
				if (sighting.SpeciesId > 0)
				{
					Species species = speciesRepopository.getById(sighting.SpeciesId);
					sighting.Species = species;
				}
			}

		} catch (SQLException e)
		{
			throw new RedmapRuntimeException(e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_map, menu);
		
		MenuItem actionRefreshMenuItem = menu.findItem(R.id.button_redmap_info_menu_item);
		actionRefreshMenuItem.setOnMenuItemClickListener(actionBarRefreshOnClickHandler);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
	{
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
	{
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
	{
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{

		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			Fragment fragment = null;
			Bundle args = new Bundle();
			switch (position)
			{
				case 0:
					fragment = new MapSectionFragment();
					args.putInt(MapSectionFragment.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case 1:
					fragment = new PhotosSectionFragment();
					args.putInt(PhotosSectionFragment.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
			}
			return fragment;
		}

		@Override
		public int getCount()
		{
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			Locale l = Locale.getDefault();
			switch (position)
			{
				case 0:
					return getString(R.string.personal_nav_map).toUpperCase(l);
				case 1:
					return getString(R.string.personal_nav_photos).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class PhotosSectionFragment extends ListFragment
	{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "2";

		private PersonalMapActivity activity;

		private View rootView;

		public PhotosSectionFragment()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			rootView = inflater.inflate(R.layout.fragment_personal_photos, container, false);
			// TextView dummyTextView = (TextView)
			// rootView.findViewById(R.id.section_label);
			// dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
			this.activity = (PersonalMapActivity) getActivity();
			return rootView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
			TextView emptyLabel = (TextView) rootView.findViewById(R.id.empty);
			if (activity.sightings.size() == 0)
			{
				emptyLabel.setVisibility(View.VISIBLE);
			} else
			{
				emptyLabel.setVisibility(View.INVISIBLE);
			}

			// render sightings into list
			renderSightings(activity.sightings);
		}

		private void renderSightings(List<Sighting> sightings)
		{
			Context ctx = getActivity().getApplicationContext();
			int layout = R.layout.listitem_sighting;
			List<String> titleStrings = new ArrayList<String>(sightings.size());

			for (Sighting sighting : sightings)
			{
				titleStrings.add(String.valueOf(sighting.SpeciesId));
			}

			setListAdapter(new SightingListViewAdapter(ctx, layout, titleStrings.toArray(new String[titleStrings.size()]), sightings));
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id)
		{
			super.onListItemClick(l, v, position, id);
			Sighting sighting = (Sighting) v.getTag();
			Intent intent = new Intent(activity.getApplicationContext(), SightingDetailActivity.class);
			intent.putExtra("sightingId", sighting.Id);
			startActivity(intent);

		}

	}

	public static class MapSectionFragment extends Fragment implements MapViewConstants
	{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "1";
		private String regionBoundries = "{\"nsw\":[[149.6510187328861,-39.33284222173442],[156.6447797975237,-39.5413110018877],[159.4980333071544,-27.09125952600292],[141.0655124305001,-29.16733244809674],[141.1320928654111,-34.06243822399932],[149.746172553171,-37.47278600141384],[149.6510187328861,-39.33284222173442]],\"nt\":[[138.1236108545581,-10.2516925663555],[127.9977731065961,-9.0645241337951],[128.9118274580214,-26.14341734492546],[138.0283716212627,-26.03988222620065],[138.1236108545581,-10.2516925663555]],\"qld\":[[138.0345474282234,-26.08713861405066],[141.0001993573809,-26.16533362790543],[141.1049469687183,-29.04820288487291],[153.521736679225,-28.28907377918563],[158.0644917925188,-25.43935957006736],[145.6076768183317,-9.495249291650364],[139.8501220318818,-10.02615982575578],[138.0278356628233,-16.80981979704593],[138.0345474282234,-26.08713861405066]],\"sa\":[[129.6315930938666,-37.23429115008388],[140.93384257679,-39.32729354152659],[140.8098409997071,-26.26951514986825],[129.161587340565,-26.30921632490599],[129.6315930938666,-37.23429115008388]],\"tas\":[[141.9509842435199,-43.48355127390608],[147.0158429295712,-44.88135409569165],[150.7146233887154,-44.24412224317301],[150.9049959105599,-40.48660735682775],[148.9910524123078,-39.30271798355914],[142.3837012007776,-39.35990228822149],[141.9509842435199,-43.48355127390608]],\"vic\":[[141.0534847571628,-34.14430776342346],[141.0544273179301,-39.33226144118233],[148.8616529485754,-39.46564263311937],[149.5687044103001,-39.30967263464959],[149.6841943465396,-37.51725198108272],[141.0534847571628,-34.14430776342346]],\"wa\":[[129.1301644985577,-15.04063693280441],[128.4500714588889,-12.90413227570088],[121.4928303942802,-12.17482067443013],[110.2718695145673,-21.36547558382702],[108.9736667824489,-34.28968215098432],[116.1471228637673,-40.00251657168517],[129.5866527753336,-37.20388013389088],[128.9531480324681,-31.79266577045803],[129.1301644985577,-15.04063693280441]]}";
		private PersonalMapActivity activity;
		private MapView mMapView;
		private MapController mMapController;
		private ItemizedOverlayWithBubble<ExtendedOverlayItem> mMyLocationOverlay;
		private ResourceProxy mResourceProxy;
		private Map<String, Polygon> regionBoundryPolygons;

		public MapSectionFragment()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			this.activity = (PersonalMapActivity) getActivity();
			setupMapView(inflater);
			return mMapView;
			// View rootView = inflater.inflate(R.layout.fragment_peronal_map,
			// container, false);
		}

		private GeoPoint getMapCentre()
		{
			if (RedmapContext.getInstance().Provider.hasCurrentUser())
			{
				User user = RedmapContext.getInstance().Provider.getCurrentUser();
				if (user.Region != null)
				{
					Polygon polygon = RedmapContext.getInstance().GetAustralianGeoService().GetPolygonByState(user.Region.Slug);
					Point centroid = polygon.getCentroid();
					return new GeoPoint(centroid.x, centroid.y);
				}
			}

			return null;
		}

		private void setupMapView(LayoutInflater inflater)
		{
			mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
			mMapView = new MapView(inflater.getContext(), 256, mResourceProxy);
			mMapView.setUseSafeCanvas(true);

			mMapController = mMapView.getController();
			mResourceProxy = new DefaultResourceProxyImpl(activity.getApplicationContext());

			mMapView.setMultiTouchControls(true);
			mMapView.setTileSource(TileSourceFactory.MAPNIK);
			mMapView.setBuiltInZoomControls(true);

			// icon
			final Drawable marker = getResources().getDrawable(R.drawable.ic_marker);
			int markerWidth = marker.getIntrinsicWidth();
			int markerHeight = marker.getIntrinsicHeight();
			marker.setBounds(10, markerHeight, markerWidth, 0);

			final Context ctx = activity.getApplicationContext();

			/* OnTapListener for the Markers, shows a simple Toast. */
			List<ExtendedOverlayItem> items = new ArrayList<ExtendedOverlayItem>();
			// mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
			// marker, new
			// ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
			mMyLocationOverlay = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(ctx, items, mMapView, new RedmapInfoWindow(mMapView, ctx));

			// test markers
			for (final Sighting sighting : activity.sightings)
			{
				Species species = sighting.GetSpecies();
				if (species == null) species = new Species("", "");
				
				mMyLocationOverlay.addItem(new ExtendedOverlayItem(species.CommonName, species.SpeciesName, new GeoPoint(sighting.Latitude, sighting.Longitude), ctx)
				{
					{
						setMarker(marker);
						setRelatedObject(sighting);
						if (sighting.HasPictureThumbnail())
						{
							setImage(new BitmapDrawable(ctx.getResources(), sighting.GetPictureThumbnailBitmap()));
						}
					}
				});
			}

			mMapView.getOverlays().add(mMyLocationOverlay);

			// Move to region or centre on Aus.
			GeoPoint moveToPoint = getMapCentre();
			int moveToZoom = 6;

			// Failed to get a user region, centre on aus, zoom out a little
			if (moveToPoint == null)
			{
				moveToPoint = new GeoPoint(-23.7, 132.8); // default centre of
														  // aus-ish
				moveToZoom = 4;
			}
			mMapController.setZoom(moveToZoom);
			mMapController.setCenter(moveToPoint);

			// Redraw
			mMapView.invalidate();
		}

		public class RedmapInfoWindow extends DefaultInfoWindow
		{

			private Context context;
			private Sighting sighting;

			public RedmapInfoWindow(MapView mapView, Context context)
			{
				super(R.layout.map_redmap_bubble, mapView);
				this.context = context;
				mView.setOnClickListener(bubbleOnClickListener);
				
				ImageView image = (ImageView)(mView.findViewById(R.id.bubble_image));
				image.setOnClickListener(bubbleOnClickListener);
				
				LinearLayout layout = (LinearLayout)(mView.findViewById(R.id.bubble_layout));
				layout.setOnClickListener(bubbleOnClickListener);
			}

			private View.OnClickListener bubbleOnClickListener = new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(context, SightingDetailActivity.class);
					intent.putExtra("sightingId", sighting.Id);
					startActivity(intent);
				}
			};

			@Override
			public void open(ExtendedOverlayItem item, int offsetX, int offsetY)
			{
				super.open(item, offsetX, offsetY);
				sighting = (Sighting) item.getRelatedObject();
				mView.setOnClickListener(bubbleOnClickListener);
			}
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
