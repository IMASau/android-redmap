package au.com.ionata.redmap.api.impl;

import com.google.gson.Gson;

import au.com.ionata.redmap.api.models.SightingOptionsListPrim;
import au.com.ionata.redmap.api.models.SightingOptionsPrim;
import au.com.ionata.redmap.utils.Callback;

public class SightingOptionsApiService extends BaseListService<SightingOptionsPrim, SightingOptionsListPrim> {
	
	public SightingOptionsApiService() {
		super();
		url = "sighting/options/";
    }
	
	protected void processResponse(String response){
    	Gson gson = new Gson();
    	SightingOptionsPrim data = gson.fromJson(response, SightingOptionsPrim.class);
    	data.response = response;
   		results.add(data);
    	finished();
	}
	
	@Override
	public void AddOnFinishedListener(Callback<BaseListService<SightingOptionsPrim, SightingOptionsListPrim>> callback){
		onFinishedListeners.add(callback);
	}
}
