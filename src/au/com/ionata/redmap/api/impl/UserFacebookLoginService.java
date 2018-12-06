package au.com.ionata.redmap.api.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.util.Log;
import au.com.ionata.redmap.api.interfaces.ILoginServiceAttemptComplete;

public class UserFacebookLoginService extends ApiBase {

	protected Collection<ILoginServiceAttemptComplete> onEventListeners = null;
	
	private RequestParams params = new RequestParams();
	
	private String facebookToken;
	private Context context;
	
	private AsyncHttpClient client;
	
	private boolean success = false;
	private String message = "";
	private String token = "";
	

	public UserFacebookLoginService(Context context, String facebookToken){
		this.onEventListeners = new ArrayList<ILoginServiceAttemptComplete>();
		this.context = context;
		this.facebookToken = facebookToken;
		url = "user/register-facebook/";
	}
	
	public void AddOnFinishedListener(ILoginServiceAttemptComplete callback){
		onEventListeners.add(callback);
	}
	
	private void finished(){
		if (onEventListeners != null){
			for (ILoginServiceAttemptComplete listener: onEventListeners){
				listener.Complete(success, message, token);
			}
		}
	}
	
	public void Execute(){
		attemptLogin();
	}
	
	private void attemptLogin(){
		client = new AsyncHttpClient();
        String url = getUrl();
        Log.d("API login call", url);
		RequestParams postParams = new RequestParams();
		postParams.put("access_token", facebookToken);
        client.post(url, postParams, loginResponseHandler);
	}
	
	private AsyncHttpResponseHandler loginResponseHandler = new AsyncHttpResponseHandler(){
		@Override
	    public void onSuccess(String response) {
			processLoginAttempt(response, true, null);
	    }
		
		public void onFailure(Throwable e, String response) {
			processLoginAttempt(response, false, e);
		};
		
        public void onFinish() {
            finished();
        };
	};

	private void setGenericFailure(){
		setGenericFailure("An unknown error occurred, sorry about that");
	}
	
	private void setGenericFailure(String message){
		this.success = false;
		this.message = message;
	}
	
	private void processLoginAttempt(String response, boolean success, Throwable error){
		JSONObject jo;

		// Success
		if (success){
			try
			{
				jo = new JSONObject(response);
				if (jo.has("auth_token")) {
					this.success = true;
					this.token = jo.getString("auth_token");
					return;
				}
			}
			catch (JSONException e) 
			{
				setGenericFailure("An error occurred processing the server response");
				return;
			}
		}

		// Failures, server side
		if (!success && error != null && error instanceof HttpResponseException){
			try 
			{
				jo = new JSONObject(response);
				// Auth error
				if (jo.has("non_field_errors")) {
					JSONArray errors = jo.optJSONArray("non_field_errors");
	                setGenericFailure(errors.getString(0));
	                return;
				}
				
				if (jo.has("email")) {
					JSONArray errors = jo.optJSONArray("email");
	                setGenericFailure(errors.getString(0));
	                return;
				}
			}
			catch (JSONException e) 
			{
			}
		}
		
		// Failures, connection errors
		if (!success && error != null){

		    if (error instanceof UnknownHostException ){
				setGenericFailure("Could not connect, check your Internet connection");
				return;
			}
			
            if (error instanceof HttpHostConnectException){
                setGenericFailure("A error occurred connecting to the server, please try again later");
                return;
            }
		    
			if (error instanceof HttpResponseException){
				setGenericFailure("A server error occurred, please try again later");
				return;
			}
			
			setGenericFailure(error.getMessage());
			return;
		}

		setGenericFailure(); // why are you here? You're not allowed to be here
	}
}
