package au.com.ionata.redmap.screens;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sromku.simple.fb.SimpleFacebook;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.data.repositories.ISightingRepository;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.User;
import au.com.ionata.redmap.overlay.BaseActivity;
import au.com.ionata.redmap.screens.gui.GuiTouchManager;
import au.com.ionata.redmap.utils.Callback;

public class MainActivity extends BaseActivity {

	protected GuiTouchManager gtm = new GuiTouchManager();
	private int devMode = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = getLayoutInflater();
		View overlayView = inflater.inflate(R.layout.activity_main, null);
		
		// hide dev layout
		RelativeLayout dev_layout = (RelativeLayout)overlayView.findViewById(R.id.main_dev_layout);
		dev_layout.setVisibility(View.GONE);
		setContentView(overlayView);

		setupActionBar();
		addListenerOnButton();
		setPressedColours();
		showMeThings();
		setHelpFont();
	}
	
	private void setHelpFont(){
		Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "AlphaMacAOE.ttf");
		TextView help = (TextView)findViewById(R.id.main_how_you_are_helping_label);
		help.setTypeface(tf, 0);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		setLoginInterfaceVisibility();
	}
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionbar = getActionBar();
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(false);
			
		}
	}
	
	private void showMeThings() {

		// show me things
		try {
	        long speciesCount = Provider.AppRepositories.SpeciesRepository().count();
	        long speciesCategoryCount = Provider.AppRepositories.SpeciesCategoryRepository().count();
	        long speciesCategorySpeciesCount = Provider.AppRepositories.SpeciesCategorySpeciesRepository().count();
	        long speciesRegionCount = Provider.AppRepositories.SpeciesRegionRepository().count();
	        long regionCount = Provider.AppRepositories.RegionRepository().count();
	        long settingsCount = Provider.AppRepositories.SettingsRepository().count();
	        
	        TextView t = (TextView)findViewById(R.id.console);
	        t.setText(String.format(
	        	"speciesCount: %s \n" +
	        	"speciesCategoryCount: %s \n" +
	        	"speciesCategorySpeciesCount: %s \n" +
	        	"speciesRegionCount: %s \n" +
	        	"regionCount: %s \n" +
	        	"settingsCount: %s \n" +
	        	"",
	        	speciesCount, speciesCategoryCount, speciesCategorySpeciesCount, speciesRegionCount, regionCount, settingsCount
	        ));
        } catch (Exception e) {
	        throw new RuntimeException(e);
        }
	}
	
	private void setPressedColours() {

		// Set text hover states
		RelativeLayout spotLayout = (RelativeLayout) findViewById(R.id.main_nav_spot);
		TextView spotTitle = (TextView) spotLayout.findViewById(R.id.title);
		TextView spotDescription = (TextView) spotLayout.findViewById(R.id.description);
		
		RelativeLayout logLayout = (RelativeLayout) findViewById(R.id.main_nav_log);
		TextView logTitle = (TextView) logLayout.findViewById(R.id.title);
		TextView logDescription = (TextView) logLayout.findViewById(R.id.description);
		
		RelativeLayout personalLayout = (RelativeLayout) findViewById(R.id.main_nav_personal);
		TextView personalTitle = (TextView) personalLayout.findViewById(R.id.title);
		TextView personalDescription = (TextView) personalLayout.findViewById(R.id.description);
		
		gtm.setPressedColours(Arrays.asList(
			spotTitle,
			spotDescription,
			logTitle,
			logDescription,
			personalTitle,
			personalDescription
		));
	}

	private OnClickListener createSpotOnClickListener(){
		final Context ctx = this;
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ctx, SpeciesCategoriesActivity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
			}
		};
		return listener;
	}
	
	private OnClickListener createHowYouHelpOnClickListener(){
		final Context ctx = this;
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ctx, HowYouHelpActivity.class);
				startActivity(intent);
			}
		};
		return listener;
	}
	
	private OnClickListener createPersonalOnClickListener(){
		final Context ctx = this;
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View view) {
			    
			    if (RedmapContext.getInstance().Provider.hasCurrentUser()){
			        Intent intent = new Intent(getApplicationContext(), PersonalMapActivity.class);
			        startActivity(intent);
			        return;
			    }
			    
				Intent intent = new Intent(ctx, LoginActivity.class);
				startActivity(intent);
			}
		};
		return listener;
	}
	
	
	private OnClickListener createLogOnClickListener(){
		final Context ctx = this;

		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ctx, LogSightingActivity.class);
				startActivity(intent);
			}
		};
		return listener;
	}
	
	public void addListenerOnButton() {
		final Provider provider = this.Provider;
		final Context ctx = this.getApplicationContext();
		
		// Collect layouts
		RelativeLayout spotLayout = (RelativeLayout) findViewById(R.id.main_nav_spot);
		RelativeLayout logLayout = (RelativeLayout) findViewById(R.id.main_nav_log);
		RelativeLayout personalLayout = (RelativeLayout) findViewById(R.id.main_nav_personal);
		RelativeLayout howYouHelpLayout = (RelativeLayout) findViewById(R.id.main_how_you_are_helping_container);
		
		spotLayout.setOnClickListener(createSpotOnClickListener());
		logLayout.setOnClickListener(createLogOnClickListener());
		personalLayout.setOnClickListener(createPersonalOnClickListener());
		howYouHelpLayout.setOnClickListener(createHowYouHelpOnClickListener());

		Button button = (Button) findViewById(R.id.lastUpdate);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Toast toast = Toast.makeText(ctx, provider.getSettings().LastSync.toString(), Toast.LENGTH_SHORT);
				toast.show();
				showMeThings();
			}
		});
	}

	private class RefreshConsole implements Callback<RefreshConsole> {
		private final MainActivity activity;
		
		RefreshConsole(MainActivity activity){
			this.activity = activity;
		}

		@Override
        public void Complete(RefreshConsole console) {
			activity.showMeThings();
        }
	}
	
	public void onForceUpdateClick(View view){
		final Provider provider = this.Provider;
		final MainActivity ctx = this;

		AsyncTask task = new AsyncTask(){
			@Override
			protected Object doInBackground(Object... params) {
				
				try {
	                provider.Update();
                } catch (SQLException e) {
	                e.printStackTrace();
                }
			    return null;
			}
			
			@Override
			protected void onPostExecute(Object result) {
				ctx.showMeThings();
			}
		};
		task.execute();
	}
	
    public void onForceUpdateSettingsClick(View view){
        final Provider provider = this.Provider;
        final MainActivity ctx = this;

        AsyncTask task = new AsyncTask(){
            @Override
            protected Object doInBackground(Object... params) {
                
                try {
                    provider.UpdateSettings();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void onPostExecute(Object result) {
                ctx.showMeThings();
            }
        };
        task.execute();
    }
	
	public void onClearDatabaseClick(View view){
		final Provider provider = this.Provider;
		final MainActivity ctx = this;

		AsyncTask task = new AsyncTask(){
			@Override
			protected Object doInBackground(Object... params) {
				provider.clearDatabase();
			    return null;
			}
			
			@Override
			protected void onPostExecute(Object result) {
				ctx.showMeThings();
			}
		};
		task.execute();
	}

    public void onLogoutButtonClick(View view){
    	
    	if (RedmapContext.getInstance().Provider.hasCurrentUser()){
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Confirm logout").setMessage("Are you sure you want to log out.")
				.setPositiveButton("Yes", confirmLogoutHandler)
				.setNegativeButton("No", null);
			AlertDialog dialog = builder.create();
			dialog.show();
        } else {
	        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
	        startActivity(intent);
	        return;
        }
    }
    
    public void sendNotification(View view){
    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_launcher)
    	        .setContentTitle("My notification")
    	        .setContentText("Hello World!");
    	// Creates an explicit intent for an Activity in your app
    	Intent resultIntent = new Intent(this, LoginActivity.class);

    	// The stack builder object will contain an artificial back stack for the
    	// started Activity.
    	// This ensures that navigating backward from the Activity leads out of
    	// your application to the Home screen.
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	// Adds the back stack for the Intent (but not the Intent itself)
    	stackBuilder.addParentStack(LoginActivity.class);
    	// Adds the Intent that starts the Activity to the top of the stack
    	stackBuilder.addNextIntent(resultIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(
    	            0,
    	            PendingIntent.FLAG_UPDATE_CURRENT
    	        );
    	mBuilder.setContentIntent(resultPendingIntent);
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	// mId allows you to update the notification later on.
    	mNotificationManager.notify(1, mBuilder.build());
    }

    private void logoutFacebook(){
    	SimpleFacebook mSimpleFacebook = SimpleFacebook.getInstance(this);
    	if (mSimpleFacebook.isLogin()){
    		mSimpleFacebook.logout(null);
    	}
    }
    
	private DialogInterface.OnClickListener confirmLogoutHandler = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int id) {
            try {
                RedmapContext.getInstance().Provider.AppRepositories.UserRepository().logoutAllUsers();
                logoutFacebook();
                setLoginInterfaceVisibility();
            } catch (SQLException e) {
                e.printStackTrace();
            }
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		
		MenuItem websiteMenuItem = menu.findItem(R.id.button_visit_website_menu_item);
		MenuItem infoMenuItem = menu.findItem(R.id.button_redmap_info_menu_item);
		
		LayoutInflater linflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View websiteButtonView = linflater.inflate(R.layout.button_visit_web_layout, null);
		View infoButtonView = linflater.inflate(R.layout.button_redmap_info_layout, null);
		
		final Context ctx = getApplicationContext();

		Button websiteButton = (Button)websiteButtonView.findViewById(R.id.button_visit_website);
		ImageButton infoButton = (ImageButton)infoButtonView.findViewById(R.id.button_redmap_info);
		
		websiteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.redmap.org.au"));
				startActivity(browserIntent);
			}
		});
		
		
		infoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, RedmapInformationActivity.class);
				startActivity(intent);
			}
		});

		websiteMenuItem.setActionView(websiteButtonView);
		infoMenuItem.setActionView(infoButtonView);
		
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
		/*switch (item.getItemId()) {
			case R.id.menu_quit_item:
				this.finish();
				return true;
			default:
				return ;
		}*/
	}

	private void setLoginInterfaceVisibility(){
		ImageView lock = (ImageView)findViewById(R.id.main_sightings_lock);
		TextView footerView = (TextView)findViewById(R.id.footerButton);
		if (RedmapContext.getInstance().Provider.hasCurrentUser()){
			lock.setVisibility(View.INVISIBLE);
			footerView.setText("Log out");
			
		}else{
			lock.setVisibility(View.VISIBLE);
			footerView.setText("Log in or create an account");
		}
	}
	
	public void clearPendingOnClick(View view){
		ISightingRepository repo;
		int i = 0;
        try
        {
	        repo = RedmapContext.getInstance().Provider.AppRepositories.SightingRepository();
    		List<Sighting> sightings = repo.getPendingSightings();
    		i = sightings.size();
    		for (Sighting sighting : sightings){
    			repo.delete(sighting.Id);
    		}
        } catch (SQLException e)
        {
	        e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), String.format("Deleted %s sightings", i), Toast.LENGTH_SHORT).show();
	}
	
	public void imasOnClick(View view){
		devMode += 1;
		if (devMode > 30){
			RelativeLayout layout = (RelativeLayout)findViewById(R.id.main_dev_layout);
			layout.setVisibility(View.VISIBLE);
		}
	}

	public void wipeDbOnClick(View view){
		RedmapContext.getInstance().Provider.DatabaseHelper.Drop();
		RedmapContext.getInstance().Provider.DatabaseHelper.Create();
	}
	public void clearDraftOnClick(View view){
		QueryBuilder<Sighting, Integer> query;
        try
        {
        	
	        query = RedmapContext.getInstance().Provider.AppRepositories.SightingRepository().getQueryBuilder();
	        query.where().eq("Status", Sighting.STATUS_DRAFT);
	        //Sighting s = query.queryForFirst();
	        //repo.delete(s.Id);
	        RedmapContext.getInstance().Provider.AppRepositories.SightingRepository().deleteAll();
        }
        catch (SQLException e)
        {
	        e.printStackTrace();
        }
        catch (IllegalStateException e)
        {
	        e.printStackTrace();
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
