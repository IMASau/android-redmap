package au.com.ionata.redmap.api.models;

import java.sql.Date;
import java.util.List;

public class UserSightingPrim {
	public int id;
	public String url;
	public String species;
	public int species_id;
	public String other_species;
	public boolean is_published;
	public String region;
	public Integer region_id;
	public Date update_time;
	public List<String> category_list;
	public String latitude;
	public String longitude;
	public int accuracy;
	public Date logging_date;
	public boolean is_valid_sighting;
	public String photo_url;
}
