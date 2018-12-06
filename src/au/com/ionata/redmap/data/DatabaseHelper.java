package au.com.ionata.redmap.data;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Settings;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesCategorySpecies;
import au.com.ionata.redmap.models.SpeciesRegion;
import au.com.ionata.redmap.models.User;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "redmap.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 34;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		Create();
	}

	public void Create(){
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			// Create entity tables
			TableUtils.createTable(connectionSource, Region.class);
			TableUtils.createTable(connectionSource, Species.class);
			TableUtils.createTable(connectionSource, SpeciesCategory.class);
			TableUtils.createTable(connectionSource, Settings.class);
			TableUtils.createTable(connectionSource, Sighting.class);
			TableUtils.createTable(connectionSource, User.class);
			
			// Create relationship tables
			TableUtils.createTable(connectionSource, SpeciesCategorySpecies.class);
			TableUtils.createTable(connectionSource, SpeciesRegion.class);
			
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
		Log.i(DatabaseHelper.class.getName(), "finished onCreate");
	}
	
	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		Drop();
		Create();
	}
	
	public void Drop(){
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			// Drop relation tables first
			TableUtils.dropTable(connectionSource, SpeciesCategorySpecies.class, true);
			TableUtils.dropTable(connectionSource, SpeciesRegion.class, true);
			
			// Drop entities
			TableUtils.dropTable(connectionSource, Region.class, true);
			TableUtils.dropTable(connectionSource, Species.class, true);
			TableUtils.dropTable(connectionSource, SpeciesCategory.class, true);
			TableUtils.dropTable(connectionSource, Settings.class, true);
			TableUtils.dropTable(connectionSource, Sighting.class, true);
			TableUtils.dropTable(connectionSource, User.class, true);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
	}
}