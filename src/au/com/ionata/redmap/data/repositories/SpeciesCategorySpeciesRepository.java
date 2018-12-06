package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;

import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesCategorySpecies;
import au.com.ionata.redmap.models.User;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class SpeciesCategorySpeciesRepository extends GenericRepository<SpeciesCategorySpecies> implements ISpeciesCategorySpeciesRepository{

	public SpeciesCategorySpeciesRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, SpeciesCategorySpecies.class);
		// TODO Auto-generated constructor stub
	}

	private QueryBuilder createSpeciesIdQueryBuilder(Species species) throws SQLException{
		QueryBuilder<Species, Integer> queryBuider = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository().getQueryBuilder();
		queryBuider.where().eq("Id", species.Id);
		return queryBuider;
	}

	private QueryBuilder createSpeciesCategoryIdQueryBuilder(SpeciesCategory speciesCategory) throws SQLException{
		QueryBuilder<SpeciesCategory, Integer> queryBuider = RedmapContext.getInstance().Provider.AppRepositories.SpeciesCategoryRepository().getQueryBuilder();
		queryBuider.where().eq("Id", speciesCategory.Id);
		return queryBuider;
	}
	
	@Override
    public boolean SpeciesInCategory(Species species, SpeciesCategory speciesCategory) throws SQLException
    {
        QueryBuilder<SpeciesCategorySpecies, Integer> queryBuilder = getQueryBuilder();
        queryBuilder.leftJoin(createSpeciesIdQueryBuilder(species));
        queryBuilder.leftJoin(createSpeciesCategoryIdQueryBuilder(speciesCategory));
		return queryBuilder.query().size() > 0 ? true : false;
    }
}
