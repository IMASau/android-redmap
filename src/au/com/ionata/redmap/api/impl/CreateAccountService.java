package au.com.ionata.redmap.api.impl;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.sql.SQLException;
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
import au.com.ionata.redmap.api.interfaces.ICreateAccountServiceAttemptComplete;
import au.com.ionata.redmap.models.User;

public class CreateAccountService extends ApiBase {

	protected Collection<ICreateAccountServiceAttemptComplete> onFinishedListeners = null;
	
    private boolean success = false;
    private String message = "";
	
	private User user;
	private Context context;
	
	private static AsyncHttpClient client = new AsyncHttpClient();
	
	public CreateAccountService(Context context, User user){
		this.onFinishedListeners = new ArrayList<ICreateAccountServiceAttemptComplete>();
		this.context = context;
		this.user = user;
		url = "user/register/";
	}
	
	public void AddOnFinishedListener(ICreateAccountServiceAttemptComplete callback){
		onFinishedListeners.add(callback);
	}
	
	private void finished(){
		if (onFinishedListeners != null){
			for (ICreateAccountServiceAttemptComplete listener: onFinishedListeners){
				listener.Complete(success, message);
			}
		}
	}
	
	public void Execute(){
		submitCreateAccount();
	}
	
	private void submitCreateAccount() {
        try {
        	String json = user.toJson();
        	StringEntity entity = new StringEntity(json);
        	client.setTimeout(15000);
	        client.post(context, getUrl(), entity, "application/json", responseHandler);
	        return;
        } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	        setGenericFailure();
	        finished();
        }
	}

    private AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler(){
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
            this.success = true;
            return;
        }

        // Failures, server side
        if (!success && error != null && error instanceof HttpResponseException){
            try 
            {
                jo = new JSONObject(response);
                if (jo.has("email")) {
                    JSONArray errors = jo.optJSONArray("email");
                    setGenericFailure(errors.getString(0));
                    return;
                }
                
                if (jo.has("username")) {
                    JSONArray errors = jo.optJSONArray("username");
                    setGenericFailure(errors.getString(0));
                    return;
                }
                
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
