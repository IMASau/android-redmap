package au.com.ionata.redmap.overlay;

import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.Provider;

public class LoadingOverlayActivityBase extends BaseActivity implements ILoadingOverlayActivity {

	protected OverlayManager overlayManager;
	protected Provider provider;
	
	
	public LoadingOverlayActivityBase() {
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
}
