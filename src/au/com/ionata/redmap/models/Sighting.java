package au.com.ionata.redmap.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.api.models.SightingOptionsPrim;
import au.com.ionata.redmap.api.models.UserSightingPrim;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;
import au.com.ionata.redmap.utils.graphics.Imaging;
import au.com.ionata.redmap.utils.io.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "sightings")
public class Sighting
{

	public final static int STATUS_DRAFT = 1;
	public final static int STATUS_PENDING = 2;
	public final static int STATUS_SYNCED = 3;

	@DatabaseField(generatedId = true)
	public int Id;

	@DatabaseField(canBeNull = true)
	public Integer Pk = null;

	@DatabaseField
	public int Status = STATUS_DRAFT;

	@DatabaseField(foreign = true, canBeNull = true)
	public User User;

	@DatabaseField
	public int SpeciesId;

	// Should probably make this a db field one days
	public Species Species;

	@DatabaseField(canBeNull = true)
	public String OtherSpecies = null;

	@DatabaseField
	public int ActivityId;

	@DatabaseField
	public int CountId;

	@DatabaseField
	public int AccuracyId;

	@DatabaseField(canBeNull = true)
	public Integer SexId = null;

	@DatabaseField(canBeNull = true)
	public Integer HabitatId = null;

	@DatabaseField(canBeNull = true)
	public Double Weight = null;

	@DatabaseField(canBeNull = true)
	public Integer WeightMethodId;

	@DatabaseField(canBeNull = true)
	public Integer Size = null;

	@DatabaseField(canBeNull = true)
	public Integer RegionId = null;

	@DatabaseField(canBeNull = true)
	public Integer SizeMethodId;

	@DatabaseField
	public int SightingDateYear;

	@DatabaseField
	public int SightingDateMonth;

	@DatabaseField
	public int SightingDateDay;

	@DatabaseField(canBeNull = true)
	public Integer TimeId;

	@DatabaseField(canBeNull = true)
	public String PictureUrl = null;

	@DatabaseField(canBeNull = true)
	public String PicturePath = null;
	
	@DatabaseField(canBeNull = true)
	public String PictureThumbnailPath = null;

	@DatabaseField(canBeNull = true)
	public Double Latitude = null;

	@DatabaseField(canBeNull = true)
	public Double Longitude = null;

	@DatabaseField(canBeNull = true)
	public Date LoggingDate = null;

	@DatabaseField(canBeNull = true)
	public Date UpdateDate = null;

	@DatabaseField(canBeNull = true)
	public Integer Depth = null;

	@DatabaseField(canBeNull = true)
	public Integer Temperature = null;

	@DatabaseField
	public boolean IsPublished = false;

	@DatabaseField
	public boolean IsValid = false;

	@DatabaseField
	public String AdditionalComments;

	public Sighting()
	{
		super();
		SightingDateYear = Calendar.getInstance().get(Calendar.YEAR);
		SightingDateMonth = Calendar.getInstance().get(Calendar.MONTH);
		SightingDateDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}

	public Sighting(UserSightingPrim userSightingPrim, User user)
	{
		this();
		UpdateAttributes(userSightingPrim, user);
		this.Status = STATUS_SYNCED;
	}

	public void UpdateAttributes(UserSightingPrim userSightingPrim)
	{
		this.Pk = userSightingPrim.id;
		if (userSightingPrim.species_id > 0)
		{
			this.SpeciesId = userSightingPrim.species_id;
		}
		
		
		this.OtherSpecies = userSightingPrim.other_species;
		this.IsPublished = userSightingPrim.is_published;
		this.RegionId = userSightingPrim.region_id;
		this.UpdateDate = userSightingPrim.update_time;
		this.Latitude = Double.valueOf(userSightingPrim.latitude);
		this.Longitude = Double.valueOf(userSightingPrim.longitude);
		this.AccuracyId = userSightingPrim.accuracy;
		this.LoggingDate = userSightingPrim.logging_date;
		this.PictureUrl = userSightingPrim.photo_url;
		this.IsValid = userSightingPrim.is_valid_sighting;
	}
	
	public void UpdateAttributes(UserSightingPrim userSightingPrim, User user)
	{
		UpdateAttributes(userSightingPrim);
		this.User = user;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object.getClass() == Sighting.class)
		{
			return ((Sighting) object).Id == Id;
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return Id;
	}

	public String toJson()
	{
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		String json = gson.toJson(new PostPrimitive(this));
		return json;
	}

	public void UpdateLoggingDate()
	{

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, SightingDateYear);
		cal.set(Calendar.MONTH, SightingDateMonth);
		cal.set(Calendar.DAY_OF_MONTH, SightingDateDay);

		LoggingDate = cal.getTime();
	}

	public String GetStringLoggingDate()
	{
		if (LoggingDate == null)
			return "";

		int hour = -1;
		try
		{
			if (TimeId != null && Status != Sighting.STATUS_SYNCED)
			{
				SightingOptionsPrim.Time time = RedmapContext.getInstance().Provider.getSettings().GetSightingOptions().getTimeById(TimeId);
				if (time != null)
				{
					hour = Integer.parseInt(time.fields.code);
				} else
				{

				}
			}
		} catch (NumberFormatException e)
		{
		}

		SimpleDateFormat df;
		if (hour >= 0 && hour <= 23 || Status == Sighting.STATUS_SYNCED)
		{
			df = new SimpleDateFormat("d MMM y h:mm a");
		} else
		{
			df = new SimpleDateFormat("d MMM y");
		}

		return df.format(LoggingDate);
	}

	public class PostPrimitive
	{
		public int accuracy;
		public int activity;
		public int count;
		public Integer depth;
		public Integer habitat;
		public String latitude;
		public String longitude;
		public String notes;
		public String other_species;
		public String photo_caption;
		public String photo_url;
		public String photo_url_name;
		public Integer sex;
		public Date sighting_date;
		public Integer size;
		public Integer size_method;
		public Integer species;
		public int time;
		public Integer water_temperature;
		public Double weight;
		public Integer weight_method;

		private byte[] getPicture(byte[] pictureData)
		{
			// check size
			int maxWH = RedmapContext.PHOTO_MAX_WIDTH_HEIGHT;
			
			Bitmap tempPhoto = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
			if (tempPhoto.getWidth() > maxWH || tempPhoto.getHeight() > maxWH)
			{
				tempPhoto = Imaging.resizeImageBitmap(tempPhoto, maxWH, maxWH);
				return Imaging.bitmapToJPEGByteArray(tempPhoto);
			}
			return pictureData;
		}
		
		public PostPrimitive(Sighting sighting)
		{
			this.accuracy = sighting.AccuracyId;
			this.activity = sighting.ActivityId;
			this.count = sighting.CountId;
			this.depth = sighting.Depth;
			this.habitat = sighting.HabitatId;

			// Redmap db has shorter decimal max lengths on lat/long columns
			// than the type allows, leading to explosions.
			DecimalFormat format = new DecimalFormat("###.############");
			this.latitude = String.valueOf(format.format(sighting.Latitude));
			this.longitude = String.valueOf(format.format(sighting.Longitude));

			this.notes = sighting.AdditionalComments;
			this.other_species = sighting.OtherSpecies;
			// this.photo_caption = sighting.;
			this.photo_url = Base64.encodeToString(getPicture(sighting.GetPicture()), Base64.DEFAULT);
			this.photo_url_name = "android_api_photo";
			this.sex = sighting.SexId;

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, sighting.SightingDateYear);
			cal.set(Calendar.MONTH, sighting.SightingDateMonth);
			cal.set(Calendar.DAY_OF_MONTH, sighting.SightingDateDay);
			this.sighting_date = cal.getTime();

			if (sighting.Size == null)
			{
				this.size = null;
				this.size_method = null;
			} else
			{
				this.size = sighting.Size;
				this.size_method = sighting.SizeMethodId;
			}

			if (sighting.SpeciesId <= 0)
			{
				this.species = null; // other species, api reqs a null post
			} else
			{
				this.species = sighting.SpeciesId;
			}

			this.time = sighting.TimeId;
			this.water_temperature = sighting.Temperature;

			if (sighting.Weight == null)
			{
				this.weight = null;
				this.weight_method = null;
			} else
			{
				this.weight = sighting.Weight;
				this.weight_method = sighting.WeightMethodId;
			}
		}
	}

	public boolean HasPicture()
	{
		if (PicturePath == null) return false;
		return Files.FileExists(PicturePath);
	}
	
	public boolean HasPictureThumbnail()
	{
		if (PictureThumbnailPath == null) return false;
		return Files.FileExists(PictureThumbnailPath);
	}
	
	public String GetPicturePath()
	{
		if (PicturePath == null)
		{
			PicturePath = Files.GetNewFilePath();
		}
		
		return PicturePath;
	}

	public String GetPictureThumbnailPath()
	{
		if (PictureThumbnailPath == null)
		{
			PictureThumbnailPath = Files.GetNewFilePath();
		}
		
		return PictureThumbnailPath;
	}
	
	public void SetPicture(Bitmap picture)
	{
		SetPicture(Imaging.bitmapToJPEGByteArray(picture));
	}
	
	public void SetPicture(byte[] picture)
	{
		Context ctx = RedmapContext.getInstance().GetContext();
		String path = GetPicturePath();

		// create directory if not exists
		ctx.getDir(path, Context.MODE_PRIVATE);

		try
		{
			// write file to internal storage
			FileOutputStream fos = ctx.openFileOutput(path, Context.MODE_PRIVATE);
			fos.write(picture);
			fos.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void ClearPicture()
	{
		if (!HasPicture()) return;
		Files.DeleteFile(PicturePath);
		PicturePath = null;
		ClearPictureThumbnail();
	}
	
	public void ClearPictureThumbnail()
	{
		if (!HasPictureThumbnail()) return;
		Files.DeleteFile(PictureThumbnailPath);
		PictureThumbnailPath = null;
	}

	public void UpdatePictureThumbnail()
	{
		Context ctx = RedmapContext.getInstance().GetContext();
		String path = GetPictureThumbnailPath();

		// create directory if not exists
		ctx.getDir(path, Context.MODE_PRIVATE);

		try
		{
			// write file to internal storage
			FileOutputStream fos = ctx.openFileOutput(path, Context.MODE_PRIVATE);
			fos.write(Imaging.resizeImageByteArray(GetPicture(), 100, 100));
			fos.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public byte[] GetPicture()
	{
		String path = GetPicturePath();
		try
		{
			return Files.GetByteArrayByPath(path);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return new byte[0];
	}

	public byte[] GetPictureThumbnail()
	{
		String path = GetPictureThumbnailPath();
		try
		{
			return Files.GetByteArrayByPath(path);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return new byte[0];
	}
	
	public Bitmap GetPictureBitmap()
	{
		return Files.GetBitmapFromBytes(GetPicture());
	}

	public Bitmap GetPictureThumbnailBitmap()
	{
		return Files.GetBitmapFromBytes(GetPictureThumbnail());
	}

	public GeoPoint getGetPoint()
	{
		if (Latitude == null || Longitude == null)
			return null;
		
		return new GeoPoint(Latitude, Longitude);
	}

	public Species GetSpecies()
	{
		if (Species != null)
		{
			return Species;
		}

		if (SpeciesId > 0)
		{
			try
			{
				return RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository().getById(SpeciesId);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}

		if (OtherSpecies != null && !OtherSpecies.isEmpty())
		{
			return new Species("", OtherSpecies);
		}
		
		return null;
	}
}
