package au.com.ionata.redmap.data.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.screens.gui.GuiTouchManager;

public class SpeciesListViewAdapter extends ArrayAdapter<Species> implements Filterable {

	protected GuiTouchManager gtm = new GuiTouchManager();

	private LayoutInflater mInflater;

	// private String[] titleStrings;

	protected List<Species> species;
	protected List<Species> originalSpecies;

	private int viewResourceId;

	private SpeciesFilter filter;

	public SpeciesListViewAdapter(Context ctx, int viewResourceId, ArrayList<Species> species) {
		super(ctx, viewResourceId, species);

		this.mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.viewResourceId = viewResourceId;

		this.originalSpecies = species;
		this.species = new ArrayList<Species>(originalSpecies);

		// create filter
		this.filter = new SpeciesFilter();
	}

	/*private String[] updateTitleString()
	{
		String[] rtn = new String[species.size()];
		for (int i = 0; i < rtn.length; ++i){
			rtn[i] = species.get(i).Description;
		}
		return rtn;
	}*/
	
	public void updateDataSet(ArrayList<Species> species) {
		this.species = species;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (species != null){
			return species.size();
		}
		return 0;
	}

	@Override
	public Species getItem(int position) {
		return species.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Species species = this.species.get(position);
		convertView = mInflater.inflate(viewResourceId, null);

		ImageView iv = (ImageView) convertView.findViewById(R.id.option_icon);
		if (species.HasPictureThumbnail()) {
			iv.setImageBitmap(species.GetPictureThumbnailBitmap());
		} else {
			iv.setImageDrawable(convertView.getResources().getDrawable(R.drawable.fish_silhouette));
		}

		TextView tv = (TextView) convertView.findViewById(R.id.option_text);
		tv.setText(species.CommonName);

		TextView description = (TextView) convertView.findViewById(R.id.option_description);
		description.setText(species.SpeciesName);

		gtm.setPressedColours(tv);
		gtm.setPressedColours(description);

		convertView.setTag(species);

		return convertView;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	private class SpeciesFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults rtn = new FilterResults();
			ArrayList<Species> results = new ArrayList<Species>();

			if (originalSpecies == null) {
				return rtn;
			}
			
			// lower case for matching
			constraint = constraint.toString().toLowerCase();

			if (constraint != null) {
				if (originalSpecies != null && originalSpecies.size() > 0) {
					for (Species species : originalSpecies) {
						String speciesName = species.SpeciesName.toLowerCase();
						String commonName = species.CommonName.toLowerCase();
						if (speciesName.contains(constraint) || commonName.contains(constraint)) {
							results.add(species);
						}
					}
				}
				rtn.values = results;
			} else {
				rtn.values = new ArrayList<Species>(originalSpecies);
			}
			return rtn;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			species = (ArrayList<Species>) results.values;
			notifyDataSetChanged();
		}

	}
}