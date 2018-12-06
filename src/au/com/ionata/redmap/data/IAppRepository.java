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

public interface IAppRepository {
	public IRegionRepository RegionRepository() throws SQLException;
	public ISpeciesRepository SpeciesRepository() throws SQLException;
	public ISpeciesCategoryRepository SpeciesCategoryRepository() throws SQLException;
	public ISpeciesCategorySpeciesRepository SpeciesCategorySpeciesRepository() throws SQLException;
	public ISpeciesRegionRepository SpeciesRegionRepository() throws SQLException;
	public ISettingsRepository SettingsRepository() throws SQLException;
	public ISightingRepository SightingRepository() throws SQLException;
	public IUserRepository UserRepository() throws SQLException;
	
	public void initialize(IRegionRepository regionRepository);
}
