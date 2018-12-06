package au.com.ionata.redmap.data;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public interface IGenericRepository<T> {
	public List<T> getAll() throws SQLException;
	public T getById(int id) throws SQLException;
	public void save(T entity) throws SQLException;
	public void insert(T entity) throws SQLException;
	public void update(T entity) throws SQLException;
	public void delete(int id) throws SQLException;
	public void deleteAll() throws SQLException;
	public long count() throws SQLException;
	public List<T> getByFieldEquals(String fieldName, Object value)throws SQLException;
	public QueryBuilder<T, Integer> getQueryBuilder();
	public List<T> getQueryResults(PreparedQuery<T> preparedQuery) throws SQLException;
}
