package au.com.ionata.redmap.api.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.util.Log;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.api.interfaces.ISubmitSightingComplete;
import au.com.ionata.redmap.api.models.UserSightingPrim;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.utils.Callback;

public class SubmitSightingService extends ApiBase implements ISubmitSightingComplete
{

	protected Collection<Callback<ISubmitSightingComplete>> onFinishedListeners = null;

	private List<Sighting> sightings;
	
	private List<Sighting> completedSightings;

	private Iterator<Sighting> submissionIterator;

	private Header[] headersArray;
	
	private Sighting currentSighting;

	private AsyncHttpClient client = new AsyncHttpClient();

	public SubmitSightingService(List<Sighting> sightings)
	{
		this.onFinishedListeners = new ArrayList<Callback<ISubmitSightingComplete>>();
		this.sightings = sightings;
		url = "sighting/create/";
	}

	public SubmitSightingService(Sighting sighting)
	{
		this.onFinishedListeners = new ArrayList<Callback<ISubmitSightingComplete>>();
		url = "sighting/create/";

		sightings = new ArrayList<Sighting>();
		sightings.add(sighting);
	}

	public void AddOnFinishedListener(Callback<ISubmitSightingComplete> callback)
	{
		onFinishedListeners.add(callback);
	}

	private void finished()
	{
		if (onFinishedListeners != null)
		{
			for (Callback<ISubmitSightingComplete> listener : onFinishedListeners)
			{
				listener.Complete(this);
			}
		}
	}

	public void Execute()
	{
		setHeadersArray();
		
		client = new AsyncHttpClient();

		submissionIterator = sightings.iterator();
		completedSightings = new ArrayList<Sighting>();
		submitSighting();
	}

	private void setHeadersArray()
	{
		List<Header> headers = new ArrayList<Header>();
		String token = RedmapContext.getInstance().Provider.getCurrentUser().AuthToken;

		headers.add(new BasicHeader("Authorization", String.format("Token %s", token)));
		headersArray = new Header[headers.size()];
		headers.toArray(headersArray);
	}

	private void submitSighting()
	{
		if (!submissionIterator.hasNext())
		{
			finished();
			return;
		}

		currentSighting = submissionIterator.next();
		try
		{
			String postData = currentSighting.toJson();
			client.post(RedmapContext.getInstance().GetContext(), getUrl(), headersArray, new StringEntity(postData), "application/json", responseHandler);
			return;
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			finished();
		}
	}

	private AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler()
	{
		@Override
		public void onSuccess(String response)
		{
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
			UserSightingPrim userSightingPrim = gson.fromJson(response, UserSightingPrim.class);
			currentSighting.UpdateAttributes(userSightingPrim);
			completedSightings.add(currentSighting);
		}

		public void onFailure(Throwable e, String response)
		{
			Log.d("API Error respnose", response);
			e.printStackTrace();
		};

		public void onFinish()
		{
			// continue submission
			submitSighting();
		};
	};

	@Override
	public List<Sighting> GetResult()
	{
		return completedSightings;
	}
}
