package au.com.ionata.redmap.models;

import au.com.ionata.redmap.api.models.RegionPrim;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "regions")
public class Region {
	
	@DatabaseField(generatedId=true, allowGeneratedIdInsert=true)
	public int Id;
	
	@DatabaseField
	public String Url;

	@DatabaseField
	public String Slug;

	@DatabaseField
	public String Description;

	@DatabaseField
	public String SightingsUrl;

    public Region(){
    	super();
    }
    
    public Region(int id, String url, String slug, String description, String sightingsUrl){
    	super();
    	this.Id = id;
        this.Url = url;
        this.Slug = slug;
        this.Description = description;
        this.SightingsUrl = sightingsUrl;
    }
    
    public Region(RegionPrim regionPrim){
    	this(regionPrim.id, regionPrim.url, regionPrim.slug, regionPrim.description, regionPrim.sightings_url);
    }
    
    @Override
    public boolean equals(Object object) {
    	if (object.getClass() == Region.class){
    		return ((Region)object).Id == Id;
    	}

        return false;
    }
    
    @Override
    public int hashCode() {
        return Id;
    }
}
