package au.com.ionata.redmap.data.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.models.Region;

public class RegionActionbarAdapter extends BaseAdapter {

	protected Context context;
	protected int layoutResourceId;
	
	protected List<Region> data;
	
	protected LayoutInflater inflater;

	public RegionActionbarAdapter(Context context, List<Region> data) {
		super();
		this.data = data;
		this.context = context;
		
		// inflater
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Region region = data.get(position);
		
		View actionBarView = inflater.inflate(R.layout.spinner_regions, null);
		TextView title = (TextView) actionBarView.findViewById(R.id.ab_basemaps_title);
		TextView subtitle = (TextView) actionBarView.findViewById(R.id.ab_basemaps_subtitle);
		
		title.setText(context.getResources().getString(R.string.regions_filter_label));
		subtitle.setText(region.Description);
		
		return actionBarView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		Region region = data.get(position);
		
		View actionBarDropDownView = inflater.inflate(R.layout.spinner_regions_item, null);
		TextView dropDownTitle = (TextView) actionBarDropDownView.findViewById(R.id.ab_basemaps_dropdown_title);

		dropDownTitle.setText(region.Description);
		dropDownTitle.setTag(region);

		return actionBarDropDownView;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Region getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
