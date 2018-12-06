package au.com.ionata.redmap.data.repositories;

import java.sql.SQLException;

import au.com.ionata.redmap.data.IGenericRepository;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesCategorySpecies;

public interface ISpeciesCategorySpeciesRepository extends IGenericRepository<SpeciesCategorySpecies> {
	public boolean SpeciesInCategory(Species species, SpeciesCategory speciesCategory) throws SQLException;
}
