package au.com.ionata.redmap.data.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.screens.gui.GuiTouchManager;

public class SightingListViewAdapter extends ArrayAdapter<String>{
	
	protected GuiTouchManager gtm = new GuiTouchManager();
	
	private LayoutInflater mInflater;
	
	private String[] titleStrings;
	private List<Sighting> sightings;
	
	private int viewResourceId;
	
	@Override
	public boolean isEnabled(int position) {
	    return true;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	
	public SightingListViewAdapter(Context ctx, int viewResourceId, String[] titleStrings, List<Sighting> sightings)
	{
		super(ctx, viewResourceId, titleStrings);
		mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.titleStrings = titleStrings;
		this.sightings = sightings;
		this.viewResourceId = viewResourceId;
	}
	
	public void updateDataSet(List<Sighting> sightings){
		this.sightings = sightings;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount(){
		return titleStrings.length;
	}
	
	@Override
	public String getItem(int position){
		return titleStrings[position];
	}
	
	@Override
	public long getItemId(int position){
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		Sighting sighting = sightings.get(position);
		convertView = mInflater.inflate(viewResourceId, null);
		
		ImageView iv = (ImageView)convertView.findViewById(R.id.option_icon);
		if (sighting.HasPictureThumbnail()){
		    iv.setImageBitmap(sighting.GetPictureThumbnailBitmap());
		}else{
		    iv.setImageDrawable(convertView.getResources().getDrawable(R.drawable.fish_silhouette));
		}
		
		TextView sighting_common_name = (TextView)convertView.findViewById(R.id.sighting_common_name);
		TextView sighting_species_name = (TextView)convertView.findViewById(R.id.sighting_species_name);
		TextView sighting_spotted_on = (TextView)convertView.findViewById(R.id.sighting_spotted_on);
		
		if (sighting.Species != null){
    		sighting_common_name.setText(sighting.Species.CommonName);
    		sighting_species_name.setText(sighting.Species.SpeciesName);
		}
		else if (sighting.OtherSpecies != null && !sighting.OtherSpecies.isEmpty())
		{
			sighting_common_name.setText(sighting.OtherSpecies);
			sighting_species_name.setText("");
		}
		else
		{
			sighting_common_name.setText("");
			sighting_species_name.setText("");
		}
		
		if (sighting.LoggingDate != null){
			sighting_spotted_on.setText(String.format("Spotted on: %s", sighting.GetStringLoggingDate()));
		}
		
		gtm.setPressedColours(sighting_common_name);
		gtm.setPressedColours(sighting_species_name);
		gtm.setPressedColours(sighting_spotted_on);
		
		convertView.setTag(sighting);
		
		return convertView;
	}
}