package au.com.ionata.redmap.api.impl;

import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

import android.net.Uri;
import au.com.ionata.redmap.api.models.RegionListPrim;
import au.com.ionata.redmap.api.models.RegionPrim;
import au.com.ionata.redmap.utils.Callback;

public class RegionApiService extends BaseListService<RegionPrim, RegionListPrim> {
	
	public RegionApiService() {
		super();
		url = "region/";
    }
	
	@Override
	protected void processResponse(String response){
    	Gson gson = new Gson();
    	RegionListPrim data = gson.fromJson(response, RegionListPrim.class);
    	count = data.count;
    	
    	for (RegionPrim prim : data.results) {
    		results.add(prim);
    	}
    	
    	if (data.next != null){
    		Uri uri = Uri.parse(data.next);
    		String page_number = uri.getQueryParameter("page");
    		
    		if (page_number != null){
    			requestParams .put("page", page_number);
    		}
    		executeRequest();
    	}
    	else
    	{
    		finished();
    	}
	}
	
	@Override
	public void AddOnFinishedListener(Callback<BaseListService<RegionPrim, RegionListPrim>> callback){
		onFinishedListeners.add(callback);
	}
}
