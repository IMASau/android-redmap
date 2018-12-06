package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesRegion;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class SpeciesCategoryRepository extends GenericRepository<SpeciesCategory> implements ISpeciesCategoryRepository{

	public SpeciesCategoryRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, SpeciesCategory.class);
	}

	@Override
	public List<SpeciesCategory> getAll() throws SQLException {
		return getQueryBuilder().orderBy("Description", true).query();
	}
}
