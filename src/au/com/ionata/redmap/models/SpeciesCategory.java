package au.com.ionata.redmap.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import au.com.ionata.redmap.api.models.SpeciesCategoryPrim;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="species_category")
public class SpeciesCategory {
    
	@DatabaseField(generatedId=true, allowGeneratedIdInsert=true)
    public int Id;
	
	@DatabaseField(canBeNull=true)
    public String Url;
	
	@DatabaseField(canBeNull=true)
    public String Description;
    
	@DatabaseField(canBeNull=true)
    public String LongDescription;
    
	@DatabaseField(dataType=DataType.BYTE_ARRAY, canBeNull=true)
    public byte[] Picture;

	@ForeignCollectionField(eager=true)
	public ForeignCollection<SpeciesCategorySpecies> SpeciesCategorySpecies;
	
    public SpeciesCategory() {
		// Empty constructor is needed by orm
    }

    public SpeciesCategory(SpeciesCategoryPrim prim) {
	    Id = prim.id;
	    Url = prim.url;
	    Description = prim.description;
	    LongDescription = prim.long_description;
	    Picture = prim.picture_url_data;
    }

	public Bitmap getPictureBitmap(){
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inPurgeable = true;
	    options.inInputShareable = true;
    	return BitmapFactory.decodeByteArray(Picture, 0, Picture.length, options);
    }
}
