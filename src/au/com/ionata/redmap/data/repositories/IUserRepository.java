package au.com.ionata.redmap.data.repositories;

import com.j256.ormlite.stmt.PreparedQuery;

import au.com.ionata.redmap.data.IGenericRepository;
import au.com.ionata.redmap.models.User;

public interface IUserRepository extends IGenericRepository<User> {
	public User getCurrentUser();
	public void logoutAllUsers();
	public boolean loginUser(User user);
	public User getByPk(int pk);
}
