package au.com.ionata.redmap.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.net.Uri;
import au.com.ionata.redmap.api.models.RegionListPrim;
import au.com.ionata.redmap.api.models.RegionPrim;
import au.com.ionata.redmap.api.models.UserSightingListPrim;
import au.com.ionata.redmap.api.models.UserSightingPrim;
import au.com.ionata.redmap.utils.Callback;

public class UserSightingApiService extends BaseListService<UserSightingPrim, UserSightingListPrim> {
	
	private Context context;
	private String token;
	
	public UserSightingApiService(Context context, String token) {
		super();
		this.context = context;
		this.token = token;
		url = "user/sightings/";
    }
	
	@Override
	protected void executeRequest(){
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", String.format("Token %s", token)));
        Header[] headersArray = new Header[headers.size()];
        headers.toArray(headersArray);
		
        client.get(context, getUrl(), headersArray, requestParams, responseHandler);
	}
	
	@Override
	protected void processResponse(String response){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    	UserSightingListPrim data = gson.fromJson(response, UserSightingListPrim.class);
    	count = data.count;
    	
    	for (UserSightingPrim prim : data.results) {
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
	public void AddOnFinishedListener(Callback<BaseListService<UserSightingPrim, UserSightingListPrim>> callback){
		onFinishedListeners.add(callback);
	}
}
