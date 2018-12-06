package au.com.ionata.redmap.utils.maps;

import org.osmdroid.util.GeoPoint;

/**
 * Point on 2D landscape
 * 
 * @author Roman Kushnarenko (sromku@gmail.com)</br>
 */
public class Point
{
	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Point(GeoPoint geoPoint)
	{
		this.x = geoPoint.getLatitudeE6() / 1000000F;
		this.y = geoPoint.getLongitudeE6() / 1000000F;
	}

	public float x;
	public float y;

	@Override
	public String toString()
	{
		return String.format("(%.2f,%.2f)", x, y);
	}
}