package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;
import java.util.List;

import au.com.ionata.redmap.data.IGenericRepository;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.User;

public interface ISightingRepository extends IGenericRepository<Sighting> {
	public Sighting getByPk(int pk);
	public List<Sighting> getSightingsByUser(User user) throws SQLException;
	public List<Sighting> getPendingSightingsByUser(User user) throws SQLException;
	public List<Sighting> getPendingSightings() throws SQLException;
}
