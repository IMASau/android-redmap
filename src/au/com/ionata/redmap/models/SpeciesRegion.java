package au.com.ionata.redmap.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "species_region")
public class SpeciesRegion {
    
	@DatabaseField(generatedId=true)
	public int Id;
	
	@DatabaseField(foreign=true)
    public Species Species;
	
	@DatabaseField(foreign=true)
    public Region Region;

    public SpeciesRegion() {
        // empty constructor needed by orm
    }

    public SpeciesRegion(Species species, Region region) {
    	this.Species = species;
    	this.Region = region;
    }
}
