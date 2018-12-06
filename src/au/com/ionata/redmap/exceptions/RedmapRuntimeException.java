package au.com.ionata.redmap.exceptions;

import android.util.Log;

public class RedmapRuntimeException extends RuntimeException 
{
    private static final long serialVersionUID = 1L;

    public RedmapRuntimeException(Throwable ex){
    	Log.e("Unhandled exception", ex.getMessage());
    	ex.printStackTrace();
    }
    
    public RedmapRuntimeException(){
    	Log.e("Unhandled exception", "No message given");
    }
    
    public RedmapRuntimeException(String msg){
    	Log.e("Unhandled exception", msg);
    }
}
