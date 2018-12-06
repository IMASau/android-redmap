package au.com.ionata.redmap.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import au.com.ionata.redmap.api.models.UserDetailPrim;
import au.com.ionata.redmap.utils.Callback;

public class UserDetailService extends BaseService<UserDetailPrim> {
	
    private String token;
    private Context context;
    
    private static AsyncHttpClient client = new AsyncHttpClient();
    
	public UserDetailService(Context context, String token) {
		super();
		this.context = context;
		this.token = token;
		url = "user/detail/";
    }

    protected void executeRequest(){
        final BaseService<UserDetailPrim> service = this;

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", String.format("Token %s", token)));
        Header[] headersArray = new Header[headers.size()];
        headers.toArray(headersArray);
        
        RequestParams params = new RequestParams();
        params.put("format", "json");

        client.get(context, getUrl(), headersArray, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int code, String response) {
                service.processResponse(response);
            }
            
            @Override
            public void onStart() {
                super.onStart();
            }
        });
    }
    
	public void AddOnFinishedListener(Callback<UserDetailPrim> callback){
		onFinishedListeners.add(callback);
	}

    @Override
    protected void processResponse(String response) {
        Gson gson = new Gson();
        UserDetailPrim data = gson.fromJson(response, UserDetailPrim.class);
        result = data;
        finished();
    }

}
