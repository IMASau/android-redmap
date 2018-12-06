package au.com.ionata.redmap.overlay;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;

public class FragmentBaseActivity extends FragmentActivity {

	protected Provider provider;
	
	public FragmentBaseActivity() {
	    super();
	    provider = RedmapContext.getInstance().Provider;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
