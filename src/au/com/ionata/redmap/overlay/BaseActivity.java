package au.com.ionata.redmap.overlay;

import android.app.Activity;
import android.view.MenuItem;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;

public class BaseActivity extends Activity {

	protected Provider Provider;
	
	public BaseActivity() {
	    super();
	    Provider = RedmapContext.getInstance().Provider;
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
