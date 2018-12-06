package au.com.ionata.redmap.overlay;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import au.com.ionata.redmap.R;

public class OverlayManager {

	private int rId = R.layout.activity_loading;
	
	private Activity activity;
	
	private View overlayView;
	
	public OverlayManager(Activity activity){
		this.activity = activity;
	}
	
	public OverlayManager(Activity activity, int rId){
		this.rId = rId;
		this.activity = activity;
	}
	
	public void CreateOverlay(){
		CreateOverlay(rId);
	}
	
	public void CreateOverlay(int rId){

		LayoutInflater inflater = activity.getLayoutInflater();
		overlayView = inflater.inflate(rId, null);
		overlayView.setVisibility(View.GONE);

		activity.getWindow().addContentView(overlayView,
                new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
	}
	
	public void DismissLoadingOverlay(){
		overlayView.setVisibility(View.GONE);
	}
	
	public void ShowLoadingOverlay(){
		overlayView.setVisibility(View.VISIBLE);
	}
}
