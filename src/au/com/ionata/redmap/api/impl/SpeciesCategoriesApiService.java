package au.com.ionata.redmap.api.impl;

import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

import android.net.Uri;
import au.com.ionata.redmap.api.models.SpeciesCategoriesListPrim;
import au.com.ionata.redmap.api.models.SpeciesCategoryPrim;
import au.com.ionata.redmap.utils.Callback;

public class SpeciesCategoriesApiService extends BaseListService<SpeciesCategoryPrim, SpeciesCategoriesListPrim> {

	public SpeciesCategoriesApiService() {
		super();
		url = "category/";
    }

	@Override
	protected void processResponse(String response){
    	Gson gson = new Gson();
    	SpeciesCategoriesListPrim data = gson.fromJson(response, SpeciesCategoriesListPrim.class);
    	count = data.count;
    	
    	for (SpeciesCategoryPrim prim : data.results) {
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
	public void AddOnFinishedListener(Callback<BaseListService<SpeciesCategoryPrim, SpeciesCategoriesListPrim>> callback){
		onFinishedListeners.add(callback);
	}
}
