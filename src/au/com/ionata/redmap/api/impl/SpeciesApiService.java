package au.com.ionata.redmap.api.impl;

import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.net.Uri;
import android.util.Log;
import au.com.ionata.redmap.api.models.SpeciesListPrim;
import au.com.ionata.redmap.api.models.SpeciesPrim;
import au.com.ionata.redmap.utils.Callback;

public class SpeciesApiService extends BaseListService<SpeciesPrim, SpeciesListPrim> {
	
	public SpeciesApiService() {
		super();
		url = "species/";
    }

	@Override
	protected void processResponse(String response){
    	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    	
    	SpeciesListPrim data = null;
   		data = gson.fromJson(response, SpeciesListPrim.class);
    	count = data.count;
    	
    	for (SpeciesPrim prim : data.results) {
    		results.add(prim);
    	}
    	
    	if (data.next != null){
    		Uri uri = Uri.parse(data.next);
    		String page_number = uri.getQueryParameter("page");
    		
    		if (page_number != null){
    			requestParams.put("page", page_number);
    		}
    		executeRequest();
    	}
    	else
    	{
    		Log.d("SpeciesApiService", String.format("Finished async get, returning %s species", results.size()));
    		finished();
    	}
	}
}
