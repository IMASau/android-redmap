package au.com.ionata.redmap.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "species_category_species")
public class SpeciesCategorySpecies {
    
	@DatabaseField(generatedId=true)
	public int Id;
	
	@DatabaseField(foreign=true)
    public Species Species;
	
	@DatabaseField(foreign=true)
    public SpeciesCategory SpeciesCategory;

    public SpeciesCategorySpecies() {
        // empty constructor needed by orm
    }

    public SpeciesCategorySpecies(Species species, SpeciesCategory speciesCategory) {
    	this.Species = species;
    	this.SpeciesCategory = speciesCategory;
    }

}
