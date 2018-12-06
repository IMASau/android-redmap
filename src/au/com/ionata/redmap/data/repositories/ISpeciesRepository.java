package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;
import java.util.List;

import au.com.ionata.redmap.data.IGenericRepository;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;

public interface ISpeciesRepository extends IGenericRepository<Species> {
	public List<Species> getAll(Region region, SpeciesCategory speciesCategory) throws SQLException;
	public long countAll(Region region, SpeciesCategory speciesCategory) throws SQLException;
}
