package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesCategorySpecies;
import au.com.ionata.redmap.models.User;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class SightingRepository extends GenericRepository<Sighting> implements ISightingRepository{

	public SightingRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Sighting.class);
	}

	@Override
	public List<Sighting> getAll() throws SQLException {
		return getQueryBuilder().orderBy("LoggingDate", true).query();
	}
	
    @Override
    public Sighting getByPk(int pk) {
        try {
            return getQueryBuilder().where().eq("Pk", pk).queryForFirst();
        }
        catch (SQLException e) {
        }
        return null;
    }

	private QueryBuilder createUserPkFilterQueryBuilder(User user) throws SQLException{
		QueryBuilder<User, Integer> userQueryBuider = RedmapContext.getInstance().Provider.AppRepositories.UserRepository().getQueryBuilder();
		userQueryBuider.where().eq("Pk", user.Pk);
		return userQueryBuider;
	}

	@Override
    public List<Sighting> getSightingsByUser(User user) throws SQLException
    {
		QueryBuilder<Sighting, Integer> sightingQueryBuilder = getQueryBuilder();
		sightingQueryBuilder.leftJoin(createUserPkFilterQueryBuilder(user));
		return sightingQueryBuilder.query();
    }
	
	public List<Sighting> getPendingSightingsByUser(User user) throws SQLException{
        QueryBuilder<Sighting, Integer> sightingQueryBuilder = getQueryBuilder();
		sightingQueryBuilder.leftJoin(createUserPkFilterQueryBuilder(user));
		sightingQueryBuilder.where().eq("Status", Sighting.STATUS_PENDING);
		return sightingQueryBuilder.query();
	}
	
	public List<Sighting> getPendingSightings() throws SQLException{
		return getQueryBuilder().where().eq("Status", Sighting.STATUS_PENDING).query();
	}
	
	
	@Override
	public void delete(int id) throws SQLException
	{
		Sighting sighting = getById(id);
		sighting.ClearPicture();
		super.delete(id);
	}
}
