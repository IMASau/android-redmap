package au.com.ionata.redmap.data;

import java.sql.SQLException;

import au.com.ionata.redmap.data.repositories.IRegionRepository;
import au.com.ionata.redmap.data.repositories.ISettingsRepository;
import au.com.ionata.redmap.data.repositories.ISightingRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesCategoryRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesCategorySpeciesRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesRegionRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesRepository;
import au.com.ionata.redmap.data.repositories.IUserRepository;
import au.com.ionata.redmap.data.repositories.RegionRepository;
import au.com.ionata.redmap.data.repositories.SpeciesCategoryRepository;
import au.com.ionata.redmap.data.repositories.SpeciesCategorySpeciesRepository;
import au.com.ionata.redmap.data.repositories.SpeciesRepository;
import au.com.ionata.redmap.data.repositories.SettingsRepository;
import au.com.ionata.redmap.data.repositories.SpeciesRegionRepository;
import au.com.ionata.redmap.data.repositories.SightingRepository;
import au.com.ionata.redmap.data.repositories.UserRepository;
import au.com.ionata.redmap.models.SpeciesRegion;
import au.com.ionata.redmap.models.User;

import com.j256.ormlite.support.ConnectionSource;

public class AppRepository implements IAppRepository {
	private ConnectionSource mConnectionSource;
	private IRegionRepository mRegionRepository;
	private ISettingsRepository mSettingsRepository;
	private ISpeciesCategoryRepository mSpeciesCategoryRepository;
	private ISpeciesRepository mSpeciesRepository;
	private ISightingRepository mSightingRepository;
	private IUserRepository mUserRepository;
	
	private ISpeciesCategorySpeciesRepository mSpeciesCategorySpeciesRepository;
	private ISpeciesRegionRepository mSpeciesRegionRepository;
	
	
	public AppRepository()
	{
	}

	public AppRepository(ConnectionSource connectionSource){
		mConnectionSource = connectionSource;
	}

	public void initialize(IRegionRepository regionRepository) {
		mRegionRepository = regionRepository;
	}

	public IRegionRepository RegionRepository() throws SQLException {
		if(mRegionRepository == null){
			mRegionRepository = new RegionRepository(mConnectionSource);
		}
		
		return mRegionRepository;
	}

	@Override
	public ISpeciesCategoryRepository SpeciesCategoryRepository() throws SQLException {
		if(mSpeciesCategoryRepository == null){
			mSpeciesCategoryRepository = new SpeciesCategoryRepository(mConnectionSource);
		}
		
		return mSpeciesCategoryRepository;
	}

	@Override
	public ISpeciesCategorySpeciesRepository SpeciesCategorySpeciesRepository() throws SQLException {
		if(mSpeciesCategorySpeciesRepository == null){
			mSpeciesCategorySpeciesRepository = new SpeciesCategorySpeciesRepository(mConnectionSource);
		}
		
		return mSpeciesCategorySpeciesRepository;
	}
	
	@Override
	public ISpeciesRegionRepository SpeciesRegionRepository() throws SQLException {
		if(mSpeciesRegionRepository == null){
			mSpeciesRegionRepository = new SpeciesRegionRepository(mConnectionSource);
		}
		
		return mSpeciesRegionRepository;
	}
	
	@Override
	public ISpeciesRepository SpeciesRepository() throws SQLException {
		if(mSpeciesRepository == null){
			mSpeciesRepository = new SpeciesRepository(mConnectionSource);
		}

		return mSpeciesRepository;
	}	
	
	@Override
	public ISettingsRepository SettingsRepository() throws SQLException {
		if(mSettingsRepository == null){
			mSettingsRepository = new SettingsRepository(mConnectionSource);
		}

		return mSettingsRepository;
	}
	
	@Override
	public ISightingRepository SightingRepository() throws SQLException {
		if(mSightingRepository == null){
			mSightingRepository = new SightingRepository(mConnectionSource);
		}

		return mSightingRepository;
	}
	
	
	@Override
	public IUserRepository UserRepository() throws SQLException {
		if(mUserRepository == null){
			mUserRepository = new UserRepository(mConnectionSource);
		}

		return mUserRepository;
	}
}
