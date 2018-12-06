package au.com.ionata.redmap.data;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;


public class AppDao<T> extends BaseDaoImpl<T, Integer> implements IAppDao<T> {
	
	public AppDao() throws SQLException{
		super(null);
	}
	
	public AppDao(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}
	
}
