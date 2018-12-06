package au.com.ionata.redmap.api.models;

import java.util.Date;
import java.util.List;

public class SpeciesPrim {
	public int id;
	public String url;
	public String species_name;
	public String common_name;
	public Date update_time;
	public String short_description;
	public String description;
	public String notes;
	public String image_credit;
	public String picture_url;
	public String sightings_url;
	public String distribution_url;
	public List<Integer> category_id_list;
	public List<Integer> region_id_list;
}
