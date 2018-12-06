package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;

import au.com.ionata.redmap.data.IGenericRepository;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesRegion;

public interface ISpeciesRegionRepository extends IGenericRepository<SpeciesRegion> {
	public boolean SpeciesInRegion(Species species, Region region) throws SQLException;
}
