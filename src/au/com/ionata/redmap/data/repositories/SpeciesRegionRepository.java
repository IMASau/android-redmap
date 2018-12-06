package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;

import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesCategorySpecies;
import au.com.ionata.redmap.models.SpeciesRegion;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class SpeciesRegionRepository extends GenericRepository<SpeciesRegion> implements ISpeciesRegionRepository{

	public SpeciesRegionRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, SpeciesRegion.class);
		// TODO Auto-generated constructor stub
	}
	
	private QueryBuilder createSpeciesIdQueryBuilder(Species species) throws SQLException{
		QueryBuilder<Species, Integer> queryBuider = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository().getQueryBuilder();
		queryBuider.where().eq("Id", species.Id);
		return queryBuider;
	}

	private QueryBuilder createSpeciesRegionIdQueryBuilder(Region region) throws SQLException{
		QueryBuilder<Region, Integer> queryBuider = RedmapContext.getInstance().Provider.AppRepositories.RegionRepository().getQueryBuilder();
		queryBuider.where().eq("Id", region.Id);
		return queryBuider;
	}
	
	@Override
    public boolean SpeciesInRegion(Species species, Region region) throws SQLException
    {
        QueryBuilder<SpeciesRegion, Integer> queryBuilder = getQueryBuilder();
        queryBuilder.leftJoin(createSpeciesIdQueryBuilder(species));
        queryBuilder.leftJoin(createSpeciesRegionIdQueryBuilder(region));
		return queryBuilder.query().size() > 0 ? true : false;
    }
	
}
