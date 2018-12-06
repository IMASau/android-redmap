package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;

import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.models.Settings;

import com.j256.ormlite.support.ConnectionSource;

public class SettingsRepository extends GenericRepository<Settings> implements ISettingsRepository{

	public SettingsRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Settings.class);
	}
}
