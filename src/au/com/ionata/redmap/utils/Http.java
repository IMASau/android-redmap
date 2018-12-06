package au.com.ionata.redmap.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import android.util.Log;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;

public class Http
{
	public static byte[] getByteResponse(String url) {
		InputStream inputStream = null;
		
		if (url == null) throw new RedmapRuntimeException("Http.getByteResponse(String url) null reference");
		
		try {
			inputStream = new java.net.URL(url).openStream();
		} catch (MalformedURLException e) {
			Log.d("Url", url);
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Url", url);
			e.printStackTrace();
			return null;
		}
		byte[] buffer = new byte[8192];
		int bytesRead;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output.toByteArray();
	}
}
