package au.com.ionata.redmap.overlay;

import android.app.ListActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;

public class LoadingOverlayListActivityBase extends ListActivity implements ILoadingOverlayActivity{

	private OverlayManager overlayManager;
	protected Provider provider;
	
	public LoadingOverlayListActivityBase() {
	    super();
	    provider = RedmapContext.getInstance().Provider;
	    overlayManager = new OverlayManager(this);
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		overlayManager.CreateOverlay();
	}
	
	public void dismissLoadingOverlay(){
		overlayManager.DismissLoadingOverlay();
	}
	
	public void showLoadingOverlay(){
		overlayManager.ShowLoadingOverlay();
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
