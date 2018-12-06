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
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.screens.gui.GuiTouchManager;

public class SpeciesCategoriesListViewAdapter extends ArrayAdapter<String>{
	
	protected GuiTouchManager gtm = new GuiTouchManager();
	
	private LayoutInflater mInflater;
	
	private String[] titleStrings;
	private List<SpeciesCategory> speciesCategories;
	
	private int viewResourceId;
	
	@Override
	public boolean isEnabled(int position) {
	    return true;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	
	public SpeciesCategoriesListViewAdapter(Context ctx, int viewResourceId, String[] titleStrings, List<SpeciesCategory> speciesCategories)
	{
		super(ctx, viewResourceId, titleStrings);
		mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.titleStrings = titleStrings;
		this.speciesCategories = speciesCategories;
		this.viewResourceId = viewResourceId;
	}
	
	public void updateDataSet(ArrayList<SpeciesCategory> speciesCategories){
		this.speciesCategories = speciesCategories;
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
	    SpeciesCategory speciesCategory = speciesCategories.get(position);
		convertView = mInflater.inflate(viewResourceId, null);
		
		byte[] img = speciesCategory.Picture;
		ImageView iv = (ImageView)convertView.findViewById(R.id.option_icon);
		if (img != null || img == new byte[0]){
		    iv.setImageBitmap(speciesCategory.getPictureBitmap());
		}else{
		    iv.setImageDrawable(convertView.getResources().getDrawable(R.drawable.fish_silhouette));
		}
		
		TextView tv = (TextView)convertView.findViewById(R.id.option_text);
		tv.setText(speciesCategory.Description);
		
		gtm.setPressedColours(tv);
		
		convertView.setTag(speciesCategory);
		
		return convertView;
	}
}