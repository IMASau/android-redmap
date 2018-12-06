package au.com.ionata.redmap;

import com.testflightapp.lib.TestFlight;

import android.app.Application;
import android.content.Intent;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.models.Region;

public class RedmapApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
        TestFlight.takeOff(this, "5b580521-b416-4728-ac42-6cd36bd0c029");
		RedmapContext.getInstance().SetContext(getApplicationContext());
		RedmapContext.getInstance().StartUp();
		RedmapContext.getInstance().SetupFacebook();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}