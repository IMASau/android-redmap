package au.com.ionata.redmap.utils.graphics;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;

public class Imaging
{
	public static String guessMimeByte(byte[] image, String notFound)
	{
		try
		{
			MagicMatch match = Magic.getMagicMatch(image);
			return match.getMimeType();
		}
		catch (MagicParseException e)
		{
		}
		catch (MagicMatchNotFoundException e)
		{
		}
		catch (MagicException e)
		{
		}
		return notFound;
	}
	
	public static byte[] bitmapToJPEGByteArray(Bitmap image)
	{
		return bitmapToByteArrayByMimeType(image, CompressFormat.JPEG.toString());
	}
	
	public static byte[] bitmapToByteArrayByMimeType(Bitmap image, String mimeType)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(CompressFormat.valueOf(mimeType), 100, baos); /// HORRENDOUSELYASZSZSD SLOW
		return baos.toByteArray();
	}
	
	public static Bitmap resizeImageBitmap(Bitmap image, int maxWidth, int maxHeight){
		int photoWidth = image.getWidth();
		int photoHeight = image.getHeight();
		
		if (photoWidth < maxWidth && photoHeight < maxHeight) return image; // resize not needed

		try
		{
			float factorH = maxHeight / (float) image.getHeight();
			float factorW = maxWidth / (float) image.getWidth();
			float factorToUse = (factorH > factorW) ? factorW : factorH;
			image = Bitmap.createScaledBitmap(image, (int)(image.getWidth() * factorToUse), (int)(image.getHeight() * factorToUse), true);
			return image;
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
			System.gc();
			throw new RedmapRuntimeException(e);
		}
	}
	
	public static byte[] resizeImageByteArray(byte[] image, int maxWidth, int maxHeight)
	{
		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
		bitmap = resizeImageBitmap(bitmap, maxWidth, maxHeight);
		return bitmapToJPEGByteArray(bitmap);
	}
}
