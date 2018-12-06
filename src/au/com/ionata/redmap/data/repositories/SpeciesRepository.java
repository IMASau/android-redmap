package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.data.Provider;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesCategorySpecies;
import au.com.ionata.redmap.models.SpeciesRegion;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class SpeciesRepository extends GenericRepository<Species> implements ISpeciesRepository{

	public SpeciesRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Species.class);
	}

	@Override
	public List<Species> getAll() throws SQLException {
		return getQueryBuilder().orderBy("Description", true).query();
	}

	private QueryBuilder createRegionSpeciesFilterQueryBuilder(Region region) throws SQLException{
		QueryBuilder<SpeciesRegion, Integer> regionSpeciesQueryBuider = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRegionRepository().getQueryBuilder();
		regionSpeciesQueryBuider.where().eq("Region_id", region.Id);
		return regionSpeciesQueryBuider;
	}
	
	private QueryBuilder createSpeciesCategorySpeciesFilterQueryBuilder(SpeciesCategory speciesCategory) throws SQLException{
		QueryBuilder<SpeciesCategorySpecies, Integer> speciesCategorySpeciesQueryBuider = RedmapContext.getInstance().Provider.AppRepositories.SpeciesCategorySpeciesRepository().getQueryBuilder();
		speciesCategorySpeciesQueryBuider.where().eq("SpeciesCategory_id", speciesCategory.Id);
		return speciesCategorySpeciesQueryBuider;
	}
	
	private QueryBuilder<Species, Integer> getAllQueryBuilder(Region region, SpeciesCategory speciesCategory) throws SQLException {
		
		QueryBuilder<Species, Integer> speciesQueryBuilder= getQueryBuilder();
		
		if (region != null){
			speciesQueryBuilder.leftJoin(createRegionSpeciesFilterQueryBuilder(region));
		}
		
		if (speciesCategory != null){
			speciesQueryBuilder.leftJoin(createSpeciesCategorySpeciesFilterQueryBuilder(speciesCategory));
		}
		return speciesQueryBuilder;
	}
	
	public List<Species> getAll(Region region, SpeciesCategory speciesCategory) throws SQLException {
		
		QueryBuilder<Species, Integer> speciesQueryBuilder = getAllQueryBuilder(region, speciesCategory);
		return speciesQueryBuilder.query();
	}
	
	public long countAll(Region region, SpeciesCategory speciesCategory) throws SQLException {
		QueryBuilder<Species, Integer> speciesQueryBuilder = getAllQueryBuilder(region, speciesCategory);
		return speciesQueryBuilder.countOf();
	}
	
	@Override
	public void delete(int id) throws SQLException
	{
		Species species = getById(id);
		species.ClearPicture();
		super.delete(id);
	}
}
