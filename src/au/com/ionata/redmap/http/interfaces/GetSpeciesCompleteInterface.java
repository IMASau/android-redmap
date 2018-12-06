package au.com.ionata.redmap.http.interfaces;

import java.util.Collection;

import au.com.ionata.redmap.models.Species;

public interface GetSpeciesCompleteInterface {
    public void onGetSpeciesComplete(Collection<Species> species);
}
