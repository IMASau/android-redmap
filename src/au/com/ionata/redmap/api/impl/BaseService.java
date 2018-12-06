package au.com.ionata.redmap.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.net.Uri.Builder;
import android.util.Log;
import au.com.ionata.redmap.utils.Callback;

public abstract class BaseService<TPrim> extends ApiBase {
	
	protected int count = 0;

	protected TPrim result;
	protected Collection<Callback<TPrim>> onFinishedListeners = null;

	protected AsyncHttpClient client;
	
	public BaseService(Collection<Callback<TPrim>> onFinishedListeners){
		super();
		this.onFinishedListeners = onFinishedListeners;
		client = new AsyncHttpClient();
		ResetRequestParams();
	}

	public BaseService(){
		this(new ArrayList<Callback<TPrim>>());
	}

	protected void finished(){
		if (onFinishedListeners != null){
			for (Callback<TPrim> listener: onFinishedListeners){
				listener.Complete(result);
			}
		}
	}
	
	protected void executeRequest(){
		client.get(getUrl(), requestParams, responseHandler);
		ResetRequestParams();
	}
	
	protected AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
	    @Override
	    public void onSuccess(int code, String response) {
	    	processResponse(response);
	    }
	    
	    @Override
	    public void onStart() {
	        super.onStart();
	    }
	};
	
	protected abstract void processResponse(String response);
	
	public void Execute(){
		executeRequest();
	}
	
	public TPrim GetResult(){
		return result;
	}
	
	public void AddOnFinishedListener(Callback<TPrim> callback){
		onFinishedListeners.add(callback);
	}
}
