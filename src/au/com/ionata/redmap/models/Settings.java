package au.com.ionata.redmap.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.content.Context;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.api.models.SightingOptionsPrim;
import au.com.ionata.redmap.utils.HashMapDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "settings")
public class Settings {

	@DatabaseField(generatedId=true)
	public int Id;

	@DatabaseField
	public Date LastSync;

	@DatabaseField
	public String SightingOptions;

	public Settings() {
		this.LastSync = new Date();
		this.SightingOptions = "";
	}
	
	//private String defaultLogSightingOptions = 	

	public SightingOptionsPrim GetSightingOptions() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Object.class, new HashMapDeserializer());
		Gson gson = gsonBuilder.create();
		
		String json = !SightingOptions.isEmpty() ? SightingOptions : getRawSightingOptions();
		SightingOptionsPrim prim = gson.fromJson(json, SightingOptionsPrim.class);

		// Sorting
		Collections.sort(prim.region, new Comparator<SightingOptionsPrim.Region>() {
			@Override
			public int compare(final SightingOptionsPrim.Region object1, final SightingOptionsPrim.Region object2) {
				return object1.fields.description.compareTo(object2.fields.description);
			}
		});

		return prim;
	}

	public boolean FullUpdateNeeded(){
		return SightingOptions.isEmpty();
	}
	
	public boolean SyncUpdateNeeded(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(LastSync);
		cal.add(Calendar.DATE, 1);
		
		return !SightingOptions.isEmpty() && (cal.getTime().before(new Date()));
	}
	
	private String getRawSightingOptions(){
		InputStream inputStream;
		Context context = RedmapContext.getInstance().GetContext();
        try
        {
        	inputStream = context.getResources().getAssets().open("sighting_options.json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
			while ((line = reader.readLine()) != null) {
		        sb.append(line);
		    }
			inputStream.close();
			return sb.toString();
        } catch (IOException e)
        {
	        e.printStackTrace();
        }
        return "";
	}
	
	public boolean IsValid() {
		if (LastSync == null)
			return false;
		if (SightingOptions == null)
			return false;
		if (SightingOptions == "")
			return false;

		return true;
	}
}
