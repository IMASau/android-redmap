package au.com.ionata.redmap.screens;

import com.google.analytics.tracking.android.EasyTracker;

import au.com.ionata.redmap.R;
import au.com.ionata.redmap.R.id;
import au.com.ionata.redmap.R.layout;
import au.com.ionata.redmap.utils.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MemoryMonitorActivity extends Activity
{
	private TextView mMaxMem;
	private TextView mCurMem;
	private TextView mAvilMem;
	private MemoryInfo mi = new MemoryInfo();
	private ActivityManager activityManager;
	
	private long maxMem = 0;
	private long curMem = 0;
	private long avilMem = 0;
	
	private Handler m_handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memory_monitor);
		
		
		activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		mMaxMem = (TextView)findViewById(R.id.mem_max);
		mCurMem = (TextView)findViewById(R.id.mem_cur);
		
		m_handler = new Handler();
		updateTask.run();
	}
	
	Runnable updateTask = new Runnable()
	{
	     @Override 
	     public void run() {
	    	 updateMi(); //this function can change value of m_interval.
	          m_handler.postDelayed(updateTask, 500);
	     }
	};
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateMi(){
		activityManager.getMemoryInfo(mi);
		curMem = mi.totalMem;
		avilMem = mi.availMem;
		
		if (curMem > maxMem) maxMem = curMem;
		
		updateDisplay();
	}
	
	private void updateDisplay(){
		mMaxMem.setText(String.valueOf(maxMem / 1024) + "B");
		mCurMem.setText(String.valueOf(curMem / 1024) + "B");
	}
	
	@Override
	protected void onStart()
	{
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onStop()
	{
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);
	}
}
