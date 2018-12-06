package au.com.ionata.redmap.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri.Builder;
import android.util.Log;
import au.com.ionata.redmap.RedmapContext;

public abstract class ApiBase {
	
	protected String url = "";
	
	protected String format = "json";

	protected String protocol = RedmapContext.getInstance().GetProtocol();
	protected String hostname = RedmapContext.getInstance().GetHostname();
	protected String apiPath = RedmapContext.getInstance().GetApiPath();
	
	protected RequestParams requestParams;

	public ApiBase(){
		ResetRequestParams();
	}
	
	protected void ResetRequestParams()
	{
		requestParams = new RequestParams();
		requestParams.put("format", "json");
	}
	
	protected String getUrl(){
		if (url.isEmpty()){
			throw new RuntimeException("Request URL was empty");
		}
		
		Builder builder = new Builder();
		String uri = builder
			.scheme(protocol)
			.encodedAuthority(hostname)
			.encodedPath(apiPath)
			.appendEncodedPath(url)
			.toString();
		//Log.d("APICall", uri);
		return uri;
	}
}
