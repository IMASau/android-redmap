package au.com.ionata.redmap.api.impl;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.api.interfaces.ILoginServiceAttemptComplete;
import au.com.ionata.redmap.api.interfaces.ISubmitSightingComplete;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.User;
import au.com.ionata.redmap.utils.Callback;

public class UserLoginService extends ApiBase {

	protected Collection<ILoginServiceAttemptComplete> onEventListeners = null;
	
	private RequestParams params = new RequestParams();
	
	private String username;
	private String password;
	private Context context;
	
	private static AsyncHttpClient client = new AsyncHttpClient();
	
	private boolean success = false;
	private String message = "";
	private String token = "";
	

	public UserLoginService(Context context, String username, String password){
		this.onEventListeners = new ArrayList<ILoginServiceAttemptComplete>();
		this.context = context;
		this.username = username;
		this.password = password;
		url = "user/api-token-auth/";
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
        String url = getUrl();
        Log.d("API login call", url);
		RequestParams postParams = new RequestParams();
		postParams.put("username", username);
		postParams.put("password", password);
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
				if (jo.has("token")) {
					this.success = true;
					this.token = jo.getString("token");
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
