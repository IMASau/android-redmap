package au.com.ionata.redmap.data.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.os.AsyncTask;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.http.interfaces.GetSpeciesByIdCompleteInterface;
import au.com.ionata.redmap.models.Species;


public final class GetSpeciesByIdAsyncTask extends AsyncTask <Integer, Void, Species> {

	private Collection<GetSpeciesByIdCompleteInterface> listeners;
	
	private Provider provider;
	
	private int speciesId = 0;
	
	public GetSpeciesByIdAsyncTask(GetSpeciesByIdCompleteInterface listener, Context context){
		listeners = new ArrayList<GetSpeciesByIdCompleteInterface>();
		provider = RedmapContext.getInstance().Provider;
		addListener(listener);
	}
	
	public GetSpeciesByIdAsyncTask(GetSpeciesByIdCompleteInterface listener, Context context, int speciesId){
		this(listener, context);
		this.speciesId = speciesId;
	}
	
	public void addListener(GetSpeciesByIdCompleteInterface listener){
		this.listeners.add(listener);
	}

	@Override
    protected Species doInBackground(Integer... ids) {
		try {
	        return provider.AppRepositories.SpeciesRepository().getById(speciesId);
        } catch (SQLException e) {
	        e.printStackTrace();
        }
		return null;
    }

	@Override
	protected void onPostExecute(Species species) {
		for (GetSpeciesByIdCompleteInterface listener : listeners){
			listener.onGetSpeciesByIdComplete(species);
		}
	}
}
