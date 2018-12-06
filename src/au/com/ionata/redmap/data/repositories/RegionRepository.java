package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;
import java.util.List;

import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.SpeciesCategory;

import com.j256.ormlite.support.ConnectionSource;

public class RegionRepository extends GenericRepository<Region> implements IRegionRepository{

	public RegionRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Region.class);
	}

	@Override
	public List<Region> getAll() throws SQLException {
		return getQueryBuilder().orderBy("Description", true).query();
	}
}
