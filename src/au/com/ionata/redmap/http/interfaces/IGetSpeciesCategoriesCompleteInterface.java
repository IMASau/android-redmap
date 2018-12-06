package au.com.ionata.redmap.http.interfaces;

import java.util.List;

import au.com.ionata.redmap.models.SpeciesCategory;

public interface IGetSpeciesCategoriesCompleteInterface {
    public void onGetSpeciesCategoriesComplete(List<SpeciesCategory> speciesCategories);
}
