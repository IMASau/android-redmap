package au.com.ionata.redmap.data.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.http.interfaces.IGetSpeciesCategoriesCompleteInterface;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.SpeciesCategory;


public final class GetSpeciesCategoriesAsyncTask extends AsyncTask <String, Void, List<SpeciesCategory>> {

	private Collection<IGetSpeciesCategoriesCompleteInterface> listeners;
	
	private Provider provider;
	private Context context;
	
	public GetSpeciesCategoriesAsyncTask(IGetSpeciesCategoriesCompleteInterface listener, Context context){
		listeners = new ArrayList<IGetSpeciesCategoriesCompleteInterface>();
		provider = RedmapContext.getInstance().Provider;
		addListener(listener);
	}
	
	public void addListener(IGetSpeciesCategoriesCompleteInterface listener){
		this.listeners.add(listener);
	}

	@Override
    protected List<SpeciesCategory> doInBackground(String... urls) {
		try {
			return provider.AppRepositories.SpeciesCategoryRepository().getAll();

        } catch (SQLException e) {
	        e.printStackTrace();
        }
		return null;
    }

	@Override
	protected void onPostExecute(List<SpeciesCategory> queryContract) {
		for (IGetSpeciesCategoriesCompleteInterface listener : listeners){
			listener.onGetSpeciesCategoriesComplete(queryContract);
		}
	}
}
