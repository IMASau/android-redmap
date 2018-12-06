package au.com.ionata.redmap.data.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.os.AsyncTask;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.http.interfaces.GetSpeciesCompleteInterface;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;


public final class GetSpeciesAsyncTask extends AsyncTask <String, Void, Collection<Species>> {

	private Collection<GetSpeciesCompleteInterface> listeners;
	
	private Provider provider;
	private Region region = null;
	private SpeciesCategory speciesCategory = null;
	
	public GetSpeciesAsyncTask(GetSpeciesCompleteInterface listener, Context context, Region region){
		super();
		this.listeners = new ArrayList<GetSpeciesCompleteInterface>();
		this.provider = RedmapContext.getInstance().Provider;
		this.region = region;
		addListener(listener);
	}
	
	public GetSpeciesAsyncTask(GetSpeciesCompleteInterface listener, Context context, Region region,  int speciesCategoryId){
		this(listener, context, region);
		try {
	        this.speciesCategory = provider.AppRepositories.SpeciesCategoryRepository().getById(speciesCategoryId);
        } catch (SQLException e) {
	        e.printStackTrace();
        }
	}
	
	public void addListener(GetSpeciesCompleteInterface listener){
		this.listeners.add(listener);
	}

	@Override
    protected Collection<Species> doInBackground(String... urls) {
		try {
	        return provider.AppRepositories.SpeciesRepository().getAll(region, speciesCategory);
        } catch (SQLException e) {
	        e.printStackTrace();
        }
		return new ArrayList<Species>();
	}

	@Override
	protected void onPostExecute(Collection<Species> species) {
		for (GetSpeciesCompleteInterface listener : listeners){
			listener.onGetSpeciesComplete(species);
		}
	}
}
