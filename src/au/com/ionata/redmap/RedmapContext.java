package au.com.ionata.redmap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osmdroid.util.GeoPoint;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

import android.content.Context;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.utils.maps.Point;
import au.com.ionata.redmap.utils.maps.Polygon;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.data.repositories.IUserRepository;
import au.com.ionata.redmap.data.repositories.UserRepository;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;

public class RedmapContext
{

	public Region FilterRegion = null;
	public Provider Provider = null;

	private String protocol = "http";
	// private String hostname = "thomas.dev.ionata.com";
	// private String hostname = "redmap.stage.aki.ionata.com";
	// private String hostname = "staging.redmap.org.au";
	private String hostname = "www.redmap.org.au";
	private String apiPath = "api";

	private AustralianGeoService australianGeoService;
	private Context applicationContext;

	// Notification IDs
	public static int NOTIFICATION_SUBMITTED = 1;
	public static int NOTIFICATION_VERIFED = 2;
	public static int PHOTO_MAX_WIDTH_HEIGHT = 2048;

	private SimpleFacebookConfiguration facebookConfiguration;
	
	private Permissions[] permissions = new Permissions[] {
		Permissions.BASIC_INFO,
		Permissions.EMAIL, 
		Permissions.USER_ABOUT_ME, 
		Permissions.USER_BIRTHDAY, 
		Permissions.USER_WEBSITE, 
	};

	private RedmapContext()
	{
	}

	public String GetProtocol()
	{
		return protocol;
	}

	public String GetHostname()
	{
		return hostname;
	}

	public String GetApiPath()
	{
		return apiPath;
	}

	private static class LazyHolder
	{
		private static final RedmapContext INSTANCE = new RedmapContext();
	}

	public static RedmapContext getInstance()
	{
		return LazyHolder.INSTANCE;
	}

	public String GetBaseUrl()
	{
		return String.format("%s://%s", protocol, hostname);
	}

	public String GetApiServerName()
	{
		String env = "development";
		
		if (hostname.contains("www.redmap.org.au"))
		{
			env = "production";
		}
		else if (hostname.contains("stage"))
		{
			env = "staging";
		}
		
		return String.format("%s (%s)", hostname, env);
	}

	public static boolean hasImageCaptureBug()
	{

		// list of known devices that have the bug
		List<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");

		return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);
	}

	public AustralianGeoService GetAustralianGeoService()
	{
		if (australianGeoService == null)
		{
			australianGeoService = new AustralianGeoService();
		}
		return australianGeoService;
	}

	public class AustralianGeoService
	{

		Map<String, Polygon> mStates;
		private String regionBoundries = "{\"nsw\":[[149.6510187328861,-39.33284222173442],[156.6447797975237,-39.5413110018877],[159.4980333071544,-27.09125952600292],[141.0655124305001,-29.16733244809674],[141.1320928654111,-34.06243822399932],[149.746172553171,-37.47278600141384],[149.6510187328861,-39.33284222173442]],\"nt\":[[138.1236108545581,-10.2516925663555],[127.9977731065961,-9.0645241337951],[128.9118274580214,-26.14341734492546],[138.0283716212627,-26.03988222620065],[138.1236108545581,-10.2516925663555]],\"qld\":[[138.0345474282234,-26.08713861405066],[141.0001993573809,-26.16533362790543],[141.1049469687183,-29.04820288487291],[153.521736679225,-28.28907377918563],[158.0644917925188,-25.43935957006736],[145.6076768183317,-9.495249291650364],[139.8501220318818,-10.02615982575578],[138.0278356628233,-16.80981979704593],[138.0345474282234,-26.08713861405066]],\"sa\":[[129.6315930938666,-37.23429115008388],[140.93384257679,-39.32729354152659],[140.8098409997071,-26.26951514986825],[129.161587340565,-26.30921632490599],[129.6315930938666,-37.23429115008388]],\"tas\":[[141.9509842435199,-43.48355127390608],[147.0158429295712,-44.88135409569165],[150.7146233887154,-44.24412224317301],[150.9049959105599,-40.48660735682775],[148.9910524123078,-39.30271798355914],[142.3837012007776,-39.35990228822149],[141.9509842435199,-43.48355127390608]],\"vic\":[[141.0534847571628,-34.14430776342346],[141.0544273179301,-39.33226144118233],[148.8616529485754,-39.46564263311937],[149.5687044103001,-39.30967263464959],[149.6841943465396,-37.51725198108272],[141.0534847571628,-34.14430776342346]],\"wa\":[[129.1301644985577,-15.04063693280441],[128.4500714588889,-12.90413227570088],[121.4928303942802,-12.17482067443013],[110.2718695145673,-21.36547558382702],[108.9736667824489,-34.28968215098432],[116.1471228637673,-40.00251657168517],[129.5866527753336,-37.20388013389088],[128.9531480324681,-31.79266577045803],[129.1301644985577,-15.04063693280441]]}";

		/*
		 * Constructor builds a polygon for each state with a key eg 'tas' and places it in the mStates property
		 */
		public AustralianGeoService()
		{

			// init
			mStates = new HashMap<String, Polygon>();

			// Convert the json to a dictionary of lists with state keys
			Map<String, List<List<Float>>> map = new Gson().fromJson(regionBoundries, new TypeToken<HashMap<String, List<List<Float>>>>()
			{
			}.getType());

			for (String stateKey : map.keySet())
			{
				// Points are parse from:
				// [[149.6510187328861,-39.33284222173442],[156.6447797975237,-39.5413110018877]...]
				List<List<Float>> points = map.get(stateKey);

				Polygon.Builder builder = Polygon.Builder();
				Iterator<List<Float>> it = points.iterator();
				while (it.hasNext())
				{
					List<Float> longlat = it.next();
					builder.addVertex(new Point(longlat.get(1), longlat.get(0)));
				}
				builder.close();
				Polygon polygon = builder.build();

				//
				mStates.put(stateKey, polygon);
			}
		}

		public boolean Contains(GeoPoint point)
		{
			for (String key : mStates.keySet())
			{
				if (mStates.get(key).contains(new Point(point))) return true;
			}
			return false;
		}

		public boolean QLDContains(GeoPoint point)
		{
			return mStates.get("qld").contains(new Point(point));
		}

		public boolean NSWContains(GeoPoint point)
		{
			return mStates.get("nsw").contains(new Point(point));
		}

		public boolean VICContains(GeoPoint point)
		{
			return mStates.get("vic").contains(new Point(point));
		}

		public boolean TASContains(GeoPoint point)
		{
			return mStates.get("tas").contains(new Point(point));
		}

		public boolean SAContains(GeoPoint point)
		{
			return mStates.get("sa").contains(new Point(point));
		}

		public boolean WAContains(GeoPoint point)
		{
			return mStates.get("wa").contains(new Point(point));
		}

		public boolean NTContains(GeoPoint point)
		{
			return mStates.get("nt").contains(new Point(point));
		}

		public Polygon GetPolygonByState(String stateKey)
		{
			return mStates.get(stateKey);
		}
	}

	public void SetContext(Context applicationContext)
	{
		this.applicationContext = applicationContext;
		Provider = new Provider(applicationContext);
	}

	public Context GetContext()
	{
		return applicationContext;
	}

	public void StartUp()
	{
		if (Provider == null) return;

		if (Provider.getSettings().FullUpdateNeeded())
		{
			Provider.AsyncUpdate();
		}
		else if (Provider.getSettings().SyncUpdateNeeded())
		{
			Provider.AsyncUpdate();
			if (Provider.hasCurrentUser())
			{
				Provider.AsyncCurrentUserSightingsSync();
			}
		}
	}
	
	public SimpleFacebookConfiguration GetFacebookConfig(){
		return facebookConfiguration;
	}
	
	public void SetupFacebook(){
		if (applicationContext == null) throw new RedmapRuntimeException("Facebook setup cannot start before Context setup");
		facebookConfiguration = new SimpleFacebookConfiguration.Builder().setAppId(GetContext().getString(R.string.app_id)).setNamespace(GetContext().getString(R.string.app_namespace)).setPermissions(permissions).build();
		SimpleFacebook.setConfiguration(RedmapContext.getInstance().GetFacebookConfig());
	}
}