package au.com.ionata.redmap.models;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.api.models.SpeciesPrim;
import au.com.ionata.redmap.api.models.UserSightingPrim;
import au.com.ionata.redmap.utils.graphics.Imaging;
import au.com.ionata.redmap.utils.io.Files;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "species")
public class Species
{

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;

	@DatabaseField(canBeNull = true)
	public String Url;

	@DatabaseField
	public String SpeciesName;

	@DatabaseField(canBeNull = true)
	public String CommonName = null;

	@DatabaseField(canBeNull = true)
	public Date UpdateDateTime = null;

	@DatabaseField(canBeNull = true)
	public String ShortDescription = null;

	@DatabaseField(canBeNull = true)
	public String Description = null;

	@DatabaseField(canBeNull = true)
	public String Notes = null;

	@DatabaseField(canBeNull = true)
	public String ImageCredit = null;

	@DatabaseField(canBeNull = true)
	public String PictureUrl = null;

	@DatabaseField(canBeNull = true)
	public String PicturePath = null;

	@DatabaseField(canBeNull = true)
	public String PictureThumbnailPath = null;

	@DatabaseField(canBeNull = true)
	public String DistributionUrl = null;

	@DatabaseField(canBeNull = true)
	public String DistributionPicturePath = null;

	@DatabaseField(canBeNull = true)
	public String SightingsUrl = null;

	@ForeignCollectionField(eager = true)
	public ForeignCollection<SpeciesCategorySpecies> SpeciesCategorySpecies;

	public Collection<SpeciesCategory> GetSpeciesCategories()
	{
		Collection<SpeciesCategory> speciesCategories = new ArrayList<SpeciesCategory>();

		if (SpeciesCategorySpecies != null)
		{
			for (SpeciesCategorySpecies scs : SpeciesCategorySpecies)
			{
				speciesCategories.add(scs.SpeciesCategory);
			}
		}

		return speciesCategories;
	}

	public Species()
	{
		// Empty constructor is needed by orm
	}

	public Species(SpeciesPrim speciesPrim)
	{
		this();
		UpdateAttributes(speciesPrim);
	}

	public Species(String speciesName, String commonName)
	{
		this();
		SpeciesName = speciesName;
		CommonName = commonName;
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

	public boolean HasDistributionPicture()
	{
		if (DistributionPicturePath == null) return false;
		return Files.FileExists(DistributionPicturePath);
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

	public String GetDistributionPicturePath()
	{
		if (DistributionPicturePath == null)
		{
			DistributionPicturePath = Files.GetNewFilePath();
		}

		return DistributionPicturePath;
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
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void SetDistributionPicture(byte[] picture)
	{
		Context ctx = RedmapContext.getInstance().GetContext();
		String path = GetDistributionPicturePath();

		// create directory if not exists
		ctx.getDir(path, Context.MODE_PRIVATE);

		try
		{
			// write file to internal storage
			FileOutputStream fos = ctx.openFileOutput(path, Context.MODE_PRIVATE);
			fos.write(picture);
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
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
		}
		catch (IOException e)
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
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return new byte[0];
	}

	public byte[] GetDistributionPicture()
	{
		String path = GetDistributionPicturePath();
		try
		{
			return Files.GetByteArrayByPath(path);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return new byte[0];
	}

	public void UpdateAttributes(SpeciesPrim speciesPrim)
	{
		this.Id = speciesPrim.id;
		this.SpeciesName = speciesPrim.species_name;
		this.CommonName = speciesPrim.common_name;
		this.UpdateDateTime = speciesPrim.update_time;
		this.ShortDescription = speciesPrim.short_description;
		this.Description = speciesPrim.description;
		this.ImageCredit = speciesPrim.image_credit;
		this.PictureUrl = speciesPrim.picture_url;
		this.DistributionUrl = speciesPrim.distribution_url;
		this.SightingsUrl = speciesPrim.sightings_url;
		this.Notes = speciesPrim.notes;
	}

	public Bitmap GetPictureBitmap()
	{
		return Files.GetBitmapFromBytes(GetPicture());
	}

	public Bitmap GetPictureThumbnailBitmap()
	{
		return Files.GetBitmapFromBytes(GetPictureThumbnail());
	}

	public Bitmap GetDistributionPictureBitmap()
	{
		return Files.GetBitmapFromBytes(GetDistributionPicture());
	}

	public Bitmap GetDistributionPictureOverlayBitmap(Bitmap map)
	{
		Bitmap distributionOverlay = GetDistributionPictureBitmap();
		Bitmap rendered = Bitmap.createBitmap(map.getWidth(), map.getHeight(), distributionOverlay.getConfig());
		distributionOverlay = resizeBitmap(distributionOverlay, map.getWidth(), map.getHeight());

		Canvas canvas = new Canvas(rendered);
		canvas.drawBitmap(map, new Matrix(), null);
		canvas.drawBitmap(distributionOverlay, new Matrix(), null);
		return rendered;
	}

	private Bitmap resizeBitmap(Bitmap bitmapOrg, int newWidth, int newHeight)
	{
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
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

	public void ClearDistributionPicture()
	{
		if (!HasDistributionPicture()) return;
		Files.DeleteFile(DistributionPicturePath);
		DistributionPicturePath = null;
	}
}
