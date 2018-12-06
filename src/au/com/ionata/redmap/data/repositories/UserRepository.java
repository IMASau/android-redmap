package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;
import java.util.List;

import android.R.bool;
import au.com.ionata.redmap.data.GenericRepository;
import au.com.ionata.redmap.models.Settings;
import au.com.ionata.redmap.models.User;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class UserRepository extends GenericRepository<User> implements IUserRepository{

	public UserRepository(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, User.class);
	}

	@Override
    public User getCurrentUser() {
		try{
			return getQueryBuilder().where().eq("LoggedIn", true).queryForFirst();
		}
		catch (SQLException ex)
		{
		}
		return null;
    }

    @Override
    public void logoutAllUsers() {
        List<User> users;
        try {
            users = getQueryBuilder().where().eq("LoggedIn", true).query();
            for (User user : users){
                user.LoggedIn = false;
                save(user);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean loginUser(User user) {
        user.LoggedIn = true;
        try {
            save(user);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getByPk(int pk) {
        try {
            return getQueryBuilder().where().eq("Pk", pk).queryForFirst();
        } catch (SQLException e) {
        }
        return null;
    }
}
