package au.com.ionata.redmap.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import au.com.ionata.redmap.RedmapContext;

public class Files
{
    public static String GetNewFilePath()
    {
    	UUID uuid = UUID.randomUUID();
    	
    	Context ctx = RedmapContext.getInstance().GetContext();
    	String filesPath = String.format("%s%s%s", ctx.getFilesDir(), File.separator, uuid.toString());
    	
    	File file = new File(filesPath);
    	while (file.exists())
    	{
    		uuid = UUID.randomUUID();
    		filesPath = String.format("%s%s%s", ctx.getFilesDir(), File.separator, uuid.toString());
    		file = new File(filesPath);
    	}

    	return uuid.toString();
    }
    
    public static boolean FileExists(String path)
    {
    	if (path == null) return false;
    	Context ctx = RedmapContext.getInstance().GetContext();
    	String filesPath = String.format("%s%s%s", ctx.getFilesDir(), File.separator, path);
    	return new File(filesPath).exists();
    }
    
    public static boolean DeleteFile(String path)
    {
    	if (path == null) return false;
    	Context ctx = RedmapContext.getInstance().GetContext();
    	String filesPath = String.format("%s%s%s", ctx.getFilesDir(), File.separator, path);
    	File file = new File(filesPath);
    	return file.delete();
    }
    
    public static Bitmap GetBitmapFromBytes(byte[] imageData){
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inPurgeable = true;
	    options.inInputShareable = true;
    	return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
    }
    
    public static byte[] GetByteArrayByPath(String path) throws IOException
    {
    	Context ctx = RedmapContext.getInstance().GetContext();
        FileInputStream file = ctx.openFileInput(path);
        return IOUtils.toByteArray(file);
    }
}
