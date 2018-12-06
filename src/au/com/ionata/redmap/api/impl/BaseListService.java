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

public abstract class BaseListService<TPrim, TPrimList> extends ApiBase
{

	protected int count = 0;

	protected Collection<TPrim> results = new ArrayList<TPrim>();
	protected Collection<Callback<BaseListService<TPrim, TPrimList>>> onFinishedListeners = null;

	protected AsyncHttpClient client;
	
	public BaseListService(Collection<Callback<BaseListService<TPrim, TPrimList>>> onFinishedListeners)
	{
		super();
		this.onFinishedListeners = onFinishedListeners;
		client = new AsyncHttpClient();
		ResetRequestParams();
	}

	public BaseListService()
	{
		this(new ArrayList<Callback<BaseListService<TPrim, TPrimList>>>());
	}

	protected void finished()
	{
		Log.d("BaseListService", String.format("finished() called, firing %s callbacks", onFinishedListeners.size()));
		if (onFinishedListeners != null)
		{
			for (Callback<BaseListService<TPrim, TPrimList>> listener : onFinishedListeners)
			{
				listener.Complete(this);
			}
		}
	}

	protected void executeRequest()
	{
		client = new AsyncHttpClient();
		String url = getUrl();
		Log.d("BaseListService", String.format("Getting url %s", client.getUrlWithQueryString(url, requestParams)));
		client.get(url, requestParams, responseHandler);
		ResetRequestParams();
	}

	public void Execute()
	{
		executeRequest();
	}

	protected abstract void processResponse(String response);

	protected AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler()
	{
		@Override
		public void onSuccess(int code, String response)
		{
			processResponse(response);
		}

		@Override
		public void onStart()
		{
			super.onStart();
		}
	};

	public Collection<TPrim> GetResults()
	{
		return results;
	}

	public void AddOnFinishedListener(Callback<BaseListService<TPrim, TPrimList>> callback)
	{
		onFinishedListeners.add(callback);
		Log.d("BaseListService", String.format("Finished callback added, currently %s callbacks", onFinishedListeners.size()));
	}
}
