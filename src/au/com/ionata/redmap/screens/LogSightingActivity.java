package au.com.ionata.redmap.screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;

import com.google.analytics.tracking.android.EasyTracker;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.api.impl.BaseListService;
import au.com.ionata.redmap.api.impl.SubmitSightingService;
import au.com.ionata.redmap.api.interfaces.ISubmitSightingComplete;
import au.com.ionata.redmap.api.models.SightingOptionsPrim;
import au.com.ionata.redmap.api.models.SightingOptionsPrim.Time;
import au.com.ionata.redmap.data.repositories.ISightingRepository;
import au.com.ionata.redmap.data.tasks.GetSpeciesByIdAsyncTask;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.User;
import au.com.ionata.redmap.overlay.BaseActivity;
import au.com.ionata.redmap.screens.gui.GuiTouchManager;
import au.com.ionata.redmap.screens.gui.pickers.DatePickerFragment;
import au.com.ionata.redmap.screens.gui.pickers.TimePickerFragment;
import au.com.ionata.redmap.utils.Callback;
import au.com.ionata.redmap.utils.graphics.Imaging;
import au.com.ionata.redmap.utils.sdk.EditTextFix;

public class LogSightingActivity extends BaseActivity
{

	public static final int AR_SELECT_LOCATION = 102;
	public static final int AR_SELECT_PHOTO = 100;
	public static final int AR_SELECT_SPECIES = 101;
	Uri mCapturedImageURI;
	
	private Bitmap tempPhoto;
	
	private int tempPhotoRotation = 0;

	private DialogInterface.OnClickListener accuracyOnClickHandler = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			sighting.AccuracyId = mOptions.accuracy.get(which).pk;
			setAccuracyText();
			validateAccuracy();
			fieldChanged();
			dialog.dismiss();
		}
	};

	private DialogInterface.OnClickListener actionBarDiscardDialogCancel = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int id)
		{
			// User cancelled the dialog
		}
	};

	private DialogInterface.OnClickListener actionBarDiscardDialogPosition = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int id)
		{
			discardDraft();
			Toast.makeText(getApplicationContext(), "Draft discarded.", Toast.LENGTH_SHORT).show();
		}
	};

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

	private void discardDraft()
	{
		if (sighting.Id > 0)
		{
			try
			{
				ISightingRepository repo = RedmapContext.getInstance().Provider.AppRepositories.SightingRepository();
				repo.delete(sighting.Id);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		loadDraft();
		setupInputs();
		Validate();
	}

	private DialogInterface.OnClickListener activityOnClickHandler = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			sighting.ActivityId = mOptions.activity.get(which).pk;
			setActivityText();
			validateActivity();
			fieldChanged();
			dialog.dismiss();
		}
	};

	private DialogInterface.OnClickListener timeOnClickHandler = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			SightingOptionsPrim.Time time = mOptions.time.get(which);
			sighting.TimeId = time.pk;
			setTimeText();
			validateTime();
			fieldChanged();
			dialog.dismiss();
		}
	};

	private DialogInterface.OnClickListener countOnClickHandler = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			sighting.CountId = mOptions.count.get(which).pk;
			setCountText();
			validateCount();
			fieldChanged();
			dialog.dismiss();
		}
	};

	private DatePickerFragment datePicker = new DatePickerFragment()
	{
		public void onDateSet(android.widget.DatePicker view, int year, int month, int day)
		{
			sighting.SightingDateYear = year;
			sighting.SightingDateMonth = month;
			sighting.SightingDateDay = day;
			setDateText();
			validateDate();
			fieldChanged();
		};
	};

	private EditText firstInvalidEditText = null;

	protected GuiTouchManager gtm = new GuiTouchManager();

	private DialogInterface.OnClickListener habitatOnClickHandler = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			sighting.HabitatId = mOptions.habitat.get(which).pk;
			setHabitatText();
			fieldChanged();
			dialog.dismiss();
		}
	};

	protected SightingOptionsPrim mOptions;

	private final String REQUIRED_MSG = "Field required";

	private DialogInterface.OnClickListener sexOnClickHandler = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			sighting.SexId = mOptions.sex.get(which).pk;
			setSexText();
			validateSex();
			fieldChanged();
			dialog.dismiss();
		}
	};

	protected Sighting sighting;

	public void AccuracyOnClick(View view)
	{
		CharSequence[] cs = new CharSequence[mOptions.accuracy.size()];
		int selectedId = -1;
		for (int i = 0; i < mOptions.accuracy.size(); i++)
		{
			SightingOptionsPrim.Accuracy accuracy = mOptions.accuracy.get(i);
			cs[i] = accuracy.fields.description;
			if (sighting.AccuracyId == accuracy.pk) selectedId = i;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Select the accuracy").setSingleChoiceItems(cs, selectedId, accuracyOnClickHandler);
		builder.create().show();
	}

	public MenuItem.OnMenuItemClickListener actionBarDiscardOnClickHandler = new MenuItem.OnMenuItemClickListener()
	{
		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			ActionBarDiscardOnClick();
			return true;
		}
	};

	public void ActionBarDiscardOnClick()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Discard draft sighting?").setMessage("Are you sure you want to discard this sighting?").setPositiveButton(android.R.string.yes, actionBarDiscardDialogPosition).setNegativeButton(android.R.string.cancel, actionBarDiscardDialogCancel);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void ActionBarSaveDraftOnClick(MenuItem menuItem)
	{
		saveDraft();
	}

	private void fieldChanged()
	{
		saveDraft();
	}

	private void saveDraft()
	{
		try
		{
			ISightingRepository repo = RedmapContext.getInstance().Provider.AppRepositories.SightingRepository();
			repo.save(sighting);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public MenuItem.OnMenuItemClickListener actionBarSubmitOnClickHandler = new MenuItem.OnMenuItemClickListener()
	{
		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			ActionBarSubmitOnClick();
			return true;
		}
	};

	public void ActionBarSubmitOnClick()
	{
		if (Validate())
		{
			Submit();
		}
	}

	public void ActivityOnClick(View view)
	{
		CharSequence[] cs = new CharSequence[mOptions.activity.size()];
		int selectedId = -1;
		for (int i = 0; i < mOptions.activity.size(); i++)
		{
			SightingOptionsPrim.Activity activity = mOptions.activity.get(i);
			cs[i] = activity.fields.description;
			if (sighting.ActivityId == activity.pk) selectedId = i;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Select the activity").setSingleChoiceItems(cs, selectedId, activityOnClickHandler);
		builder.create().show();
	}

	public void TimeOnClick(View view)
	{
		CharSequence[] cs = new CharSequence[mOptions.time.size()];
		int selectedId = -1;
		for (int i = 0; i < mOptions.time.size(); i++)
		{
			Time time = mOptions.time.get(i);
			cs[i] = time.fields.description;
			if (sighting.TimeId == time.pk) selectedId = i;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Select the hour of the sighting").setSingleChoiceItems(cs, selectedId, timeOnClickHandler);
		builder.create().show();
	}

	public void CountOnClick(View view)
	{
		CharSequence[] cs = new CharSequence[mOptions.count.size()];
		int selectedId = -1;
		for (int i = 0; i < mOptions.count.size(); i++)
		{
			SightingOptionsPrim.Count count = mOptions.count.get(i);
			cs[i] = count.fields.description;
			if (sighting.CountId == count.pk) selectedId = i;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("How many did you see?").setSingleChoiceItems(cs, selectedId, countOnClickHandler);
		builder.create().show();
	}

	private EditText getEditText(int id)
	{
		RelativeLayout layout = (RelativeLayout) findViewById(id);
		EditText field = (EditText) layout.findViewById(R.id.value_label);
		return field;
	}

	private DialogInterface.OnClickListener getSizeOnClickEstimatedHandler(EditText field)
	{

		final EditText resultField = field;

		DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				String value = resultField.getText().toString();

				try
				{
					sighting.Size = Integer.valueOf(value);
				}
				catch (NumberFormatException e)
				{
					sighting.Size = null;
					if (!value.isEmpty())
					{
						EditText field = getEditText(R.id.size_layout);
						field.setError("Invalid value");
					}
				}
				sighting.SizeMethodId = 1;
				setSizeText();
				fieldChanged();
			}
		};

		return handler;
	}

	private DialogInterface.OnClickListener getSizeOnClickMeasuredHandler(EditText field)
	{

		final EditText resultField = field;

		DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				String value = resultField.getText().toString();
				try
				{
					sighting.Size = Integer.valueOf(value);
				}
				catch (NumberFormatException e)
				{
					sighting.Size = null;
					if (!value.isEmpty())
					{
						EditText field = getEditText(R.id.size_layout);
						field.setError("Invalid value");
					}
				}
				sighting.SizeMethodId = 2;
				setSizeText();
				fieldChanged();
			}
		};

		return handler;
	}

	private DialogInterface.OnClickListener getWeightOnClickEstimatedHandler(EditText field)
	{

		final EditText resultField = field;

		DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				String value = resultField.getText().toString();
				try
				{
					sighting.Weight = Double.valueOf(value);
					sighting.WeightMethodId = 2;
				}
				catch (NumberFormatException e)
				{
					sighting.Weight = null;
					sighting.WeightMethodId = null;
					if (!value.isEmpty())
					{
						EditText field = getEditText(R.id.weight_layout);
						field.setError("Invalid value");
					}
				}
				setWeightText();
				fieldChanged();
			}
		};

		return handler;
	}

	private DialogInterface.OnClickListener getWeightOnClickMeasuredHandler(EditText field)
	{

		final EditText resultField = field;

		DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				String value = resultField.getText().toString();
				try
				{
					sighting.Weight = Double.valueOf(value);
					sighting.WeightMethodId = 1;
				}
				catch (NumberFormatException e)
				{
					sighting.Weight = null;
					sighting.WeightMethodId = null;
					if (!value.isEmpty())
					{
						EditText field = getEditText(R.id.weight_layout);
						field.setError("Invalid value");
					}
				}
				setWeightText();
				fieldChanged();
			}
		};

		return handler;
	}

	public void HabitatOnClick(View view)
	{
		CharSequence[] cs = new CharSequence[mOptions.habitat.size()];
		int selectedId = -1;
		for (int i = 0; i < mOptions.habitat.size(); i++)
		{
			SightingOptionsPrim.Habitat habitat = mOptions.habitat.get(i);
			cs[i] = habitat.fields.description;
			if (sighting.HabitatId != null && sighting.HabitatId == habitat.pk) selectedId = i;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Select the habitat").setSingleChoiceItems(cs, selectedId, habitatOnClickHandler);

		builder.create().show();
	}

	private void loadDraft()
	{
		sighting = RedmapContext.getInstance().Provider.getDraftSighting();

		if (sighting == null)
		{
			sighting = new Sighting();

			// set defaults

			// time
			for (SightingOptionsPrim.Time time : mOptions.time)
			{
				if (time.fields.code.equals("NS"))
				{
					sighting.TimeId = time.pk;
				}
			}

			// sex
			for (SightingOptionsPrim.Sex sex : mOptions.sex)
			{
				if (sex.fields.description.equals("Unknown"))
				{
					sighting.SexId = sex.pk;
				}
			}

			// habitat
			for (SightingOptionsPrim.Habitat habitat : mOptions.habitat)
			{
				if (habitat.fields.description.equals("Unknown"))
				{
					sighting.HabitatId = habitat.pk;
				}
			}

			// count
			for (SightingOptionsPrim.Count count : mOptions.count)
			{
				if (count.fields.description.equals("1"))
				{
					sighting.CountId = count.pk;
				}
			}
		}
	}

	public void LocationOnClick(View view)
	{
		Intent intent = new Intent(getApplicationContext(), LogSightingLocationActivity.class);
		intent.putExtra("isLogSightingLocationActivity", true);
		if (sighting.Latitude != null && sighting.Longitude != null)
		{
			intent.putExtra("latitude", sighting.Latitude);
			intent.putExtra("longitude", sighting.Longitude);
		}
		startActivityForResult(intent, AR_SELECT_LOCATION);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent returnedIntent)
	{
		switch (requestCode)
		{
			case (AR_SELECT_SPECIES):
			{
				if (resultCode == Activity.RESULT_OK)
				{
					Bundle extras = returnedIntent.getExtras();
					if (extras != null)
					{

						if (extras.containsKey("speciesId"))
						{
							sighting.SpeciesId = extras.getInt("speciesId");
							sighting.OtherSpecies = null;
							setSpeciesText();
							validateSpecies();
							fieldChanged();
						}
						else if (extras.containsKey("otherSpecies"))
						{
							sighting.OtherSpecies = extras.getString("otherSpecies"); // could be empty on edge case
							if (sighting.OtherSpecies.isEmpty())
							{
								// api needs to post null values, check and set
								sighting.OtherSpecies = null;
							}
							sighting.SpeciesId = -1;
							setSpeciesText();
							validateSpecies();
							fieldChanged();
						}
					}
				}
				return;
			}

			case (AR_SELECT_LOCATION):
			{
				if (resultCode == Activity.RESULT_OK)
				{
					Bundle extras = returnedIntent.getExtras();
					if (extras != null && extras.containsKey("latitude") && extras.containsKey("longitude"))
					{
						sighting.Latitude = extras.getDouble("latitude");
						sighting.Longitude = extras.getDouble("longitude");
						setLocationText();
						validateLocation();
						fieldChanged();
					}
				}
				return;
			}

			case (AR_SELECT_PHOTO):
			{
				if (resultCode == Activity.RESULT_OK)
				{
					Uri selectedImage;
					InputStream imageStream;

					if (returnedIntent == null)
					{
						try
						{
							selectedImage = mCapturedImageURI;
							
							// Fix orientation
							String[] projection = { MediaStore.Images.Media.DATA };
							Cursor cursor = getContentResolver().query(selectedImage, projection, null, null, null);
							int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
							cursor.moveToFirst();
							String imageFilePath = cursor.getString(column_index_data);
							tempPhotoRotation = getCameraPhotoOrientation(getApplicationContext(), selectedImage, imageFilePath);
							
							File file = new File(imageFilePath);
							imageStream = new FileInputStream(file);
						}
						catch (FileNotFoundException e)
						{
							throw new RedmapRuntimeException(e);
						}
					}
					else
					{
						try
						{
							selectedImage = returnedIntent.getData();
							
							// Fix orientation
							String[] filePathColumn = { MediaStore.Images.Media.DATA };
							Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
							cursor.moveToFirst();
							int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
							String imageFilePath = cursor.getString(columnIndex);
							tempPhotoRotation = getCameraPhotoOrientation(getApplicationContext(), selectedImage, imageFilePath);
							
							imageStream = getContentResolver().openInputStream(selectedImage);
						}
						catch (FileNotFoundException e)
						{
							return; // cancel from gallery
						}
					}

					try
					{
						byte[] buffer = new byte[8192];
						int bytesRead;
						ByteArrayOutputStream output = new ByteArrayOutputStream();
						while ((bytesRead = imageStream.read(buffer)) != -1)
						{
							output.write(buffer, 0, bytesRead);
						}
						
						tempPhoto = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size());
						
						// Correct bitmap rotation?
						if (tempPhotoRotation > 0){
							Matrix matrix = new Matrix();
							matrix.postRotate(tempPhotoRotation);
							tempPhoto = Bitmap.createBitmap(tempPhoto , 0, 0, tempPhoto.getWidth(), tempPhoto.getHeight(), matrix, true);
						}

						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle("Photo license agreement");
						builder.setMessage("Do you own all rights to this photo, and give permission for Redmap Australia to display it on their website and use in other related publications and articles?");
						builder.setPositiveButton("Yes", photoRightsAcceptAgree);
						builder.setNegativeButton("No", null);
						AlertDialog dialog = builder.create();
						dialog.show();

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
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, returnedIntent);
	}

	private DialogInterface.OnClickListener photoRightsAcceptAgree = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int id)
		{
			// check size
			int maxWH = RedmapContext.PHOTO_MAX_WIDTH_HEIGHT;
			if (tempPhoto.getWidth() > maxWH || tempPhoto.getHeight() > maxWH)
			{
				tempPhoto = Imaging.resizeImageBitmap(tempPhoto, maxWH, maxWH);
			}

			AsyncTask task = new AsyncTask()
			{
				@Override
				protected Object doInBackground(Object... params)
				{
					sighting.SetPicture(tempPhoto);
					sighting.UpdatePictureThumbnail();
					saveDraft();
					tempPhoto = null;
					return null;
				}
			};
			task.execute();
			setPhotoImage();
			validatePhoto();
			fieldChanged();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_sighting);
		setupActionBar();

		setPressedColours();
		mOptions = RedmapContext.getInstance().Provider.getSettings().GetSightingOptions();
		loadDraft();
		setupInputs();

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("speciesId") && extras.containsKey("logItButton"))
		{
			int speciesId = extras.getInt("speciesId");
			discardDraft();
			sighting.SpeciesId = speciesId;
			setSpeciesText();
		}
		Validate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_log_sighting, menu);

		MenuItem actionDiscardMenuItem = menu.findItem(R.id.action_discard);
		MenuItem actionSubmitMenuItem = menu.findItem(R.id.action_submit);

		actionDiscardMenuItem.setOnMenuItemClickListener(actionBarDiscardOnClickHandler);
		actionSubmitMenuItem.setOnMenuItemClickListener(actionBarSubmitOnClickHandler);

		return true;
	}

	private DialogInterface.OnClickListener clearSightingPhotoAlertHandler = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int id)
		{
			sighting.ClearPicture();
			sighting.ClearPictureThumbnail();
			tempPhoto = null;
			fieldChanged();
			setPhotoImage();
			validatePhoto();
			dialog.dismiss();
		}
	};

	public void SelectSightingPhotoOnClick(View view)
	{
		// clear existing photo first.
		if (sighting.HasPicture())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Remove current photo").setMessage("Do you want to remove the photo from this sighting?");
			builder.setPositiveButton("Yes", clearSightingPhotoAlertHandler);
			builder.setNegativeButton("No", null);
			builder.create().show();
			return;
		}

		Intent pickIntent = new Intent();
		pickIntent.setType("image/*");
		pickIntent.setAction(Intent.ACTION_GET_CONTENT);

		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String fileName = "temp_photo";
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, fileName);
		mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
		takePhotoIntent.putExtra("camera_action", true);

		String pickTitle = "Select or take a new Picture"; // Or get from
															// strings.xml
		Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { takePhotoIntent });
		startActivityForResult(chooserIntent, AR_SELECT_PHOTO);

	}

	private void setAccuracyText()
	{
		EditText label = getEditText(R.id.accuracy_layout);
		if (sighting.AccuracyId <= 0)
		{
			label.setText(null);
			return;
		}

		String text = null;

		SightingOptionsPrim.Accuracy accuracy = mOptions.getAccuracyById(sighting.AccuracyId);

		if (accuracy != null)
		{
			text = accuracy.fields.description;
		}

		label.setText(text);
	}

	private void setActivityText()
	{
		EditText label = getEditText(R.id.activity_layout);
		if (sighting.ActivityId <= 0)
		{
			label.setText(null);
			return;
		}

		String text = null;

		SightingOptionsPrim.Activity activity = mOptions.getActivityById(sighting.ActivityId);

		if (activity != null)
		{
			text = activity.fields.description;
		}

		label.setText(text);
	}

	private void setAdditionalCommentsText()
	{
		EditText label = (EditText) findViewById(R.id.additional_comments);
		if (sighting.AdditionalComments == null || sighting.AdditionalComments == "")
		{
			label.setText(null);
			return;
		}

		label.setText(sighting.AdditionalComments);
	}

	private void setCountText()
	{
		EditText label = getEditText(R.id.count_layout);
		if (sighting.CountId <= 0)
		{
			label.setText(null);
			return;
		}

		String text = null;

		SightingOptionsPrim.Count count = mOptions.getCountById(sighting.CountId);

		if (count != null)
		{
			text = count.fields.description;
		}

		label.setText(text);
	}

	private void setDateText()
	{
		EditText label = getEditText(R.id.date_layout);
		if (sighting.SightingDateYear <= 0)
		{
			label.setText(null);
			return;
		}

		SimpleDateFormat df = new SimpleDateFormat("MMM d, y");

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, sighting.SightingDateYear);
		cal.set(Calendar.MONTH, sighting.SightingDateMonth);
		cal.set(Calendar.DAY_OF_MONTH, sighting.SightingDateDay);

		String text = df.format(cal.getTime());

		label.setText(text);
	}

	private void setDepthText()
	{
		EditText label = getEditText(R.id.depth_layout);
		if (sighting.Depth == null)
		{
			label.setText(null);
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(sighting.Depth));
		sb.append(String.valueOf(" meters"));

		label.setText(String.valueOf(sb.toString()));
	}

	private void setTemperatureText()
	{
		EditText label = getEditText(R.id.temperature_layout);
		if (sighting.Temperature == null)
		{
			label.setText(null);
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(sighting.Temperature));
		sb.append(String.valueOf(" celsius"));

		label.setText(String.valueOf(sb.toString()));
	}

	private void setFirstInvalidEditText(EditText editText)
	{
		if (this.firstInvalidEditText == null)
		{
			this.firstInvalidEditText = editText;
		}
	}

	private void setHabitatText()
	{
		EditText label = getEditText(R.id.habitat_layout);
		if (sighting.HabitatId == null || sighting.HabitatId <= 0)
		{
			label.setText(null);
			return;
		}

		String text = null;

		SightingOptionsPrim.Habitat habitat = mOptions.getHabitatById(sighting.HabitatId);

		if (habitat != null)
		{
			text = habitat.fields.description;
		}

		label.setText(text);
	}

	private void setLocationText()
	{
		EditText label = getEditText(R.id.location_layout);
		if (sighting.Longitude == null || sighting.Latitude == null)
		{
			label.setText(null);
			return;
		}
		DecimalFormat format = new DecimalFormat("###.###");
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(format.format(sighting.Latitude)));
		sb.append(String.valueOf("~ / "));
		sb.append(String.valueOf(format.format(sighting.Longitude)));
		sb.append(String.valueOf("~"));

		label.setText(sb.toString());
	}

	private void setPhotoImage()
	{
		ImageView sightingPhoto = (ImageView) findViewById(R.id.sighting_photo);

		if (!sighting.HasPictureThumbnail() && !sighting.HasPicture())
		{
			if (tempPhoto != null)
			{
				sightingPhoto.setImageBitmap(tempPhoto);
				return;
			}
			sightingPhoto.setImageResource(R.drawable.add_image);
			return;
		}
		else if (sighting.HasPictureThumbnail())
		{
			sightingPhoto.setImageBitmap(sighting.GetPictureThumbnailBitmap());
			return;
		}
		
		sightingPhoto.setImageBitmap(sighting.GetPictureBitmap());
	}

	private void setPressedColours()
	{
		List<RelativeLayout> layouts = new ArrayList<RelativeLayout>();
		layouts.add((RelativeLayout) findViewById(R.id.species_layout));
		layouts.add((RelativeLayout) findViewById(R.id.activity_layout));
		layouts.add((RelativeLayout) findViewById(R.id.accuracy_layout));
		layouts.add((RelativeLayout) findViewById(R.id.location_layout));
		layouts.add((RelativeLayout) findViewById(R.id.date_layout));
		layouts.add((RelativeLayout) findViewById(R.id.time_layout));
		layouts.add((RelativeLayout) findViewById(R.id.count_layout));
		layouts.add((RelativeLayout) findViewById(R.id.weight_layout));
		layouts.add((RelativeLayout) findViewById(R.id.size_layout));
		layouts.add((RelativeLayout) findViewById(R.id.sex_layout));
		layouts.add((RelativeLayout) findViewById(R.id.habitat_layout));
		layouts.add((RelativeLayout) findViewById(R.id.depth_layout));
		layouts.add((RelativeLayout) findViewById(R.id.temperature_layout));

		for (final RelativeLayout layout : layouts)
		{
			View name = (View) layout.findViewById(R.id.name);
			gtm.setPressedColours((TextView) name);

			View value_label = (View) layout.findViewById(R.id.value_label);
			Class klass = value_label.getClass();
			if (klass == TextView.class)
			{
				gtm.setPressedColours((TextView) value_label);
			}
			else if (klass == EditTextFix.class || klass == EditText.class)
			{
				EditText editText = (EditText) value_label;
				gtm.setPressedColours(editText);

				// Override edittext pressed state to set the parent layout as
				// also pressed (duplicate parent state doesn't work for
				// edittext)
				editText.setOnTouchListener(new View.OnTouchListener()
				{
					@Override
					public boolean onTouch(View v, MotionEvent event)
					{
						// TODO Auto-generated method stub
						switch (event.getAction())
						{
							case MotionEvent.ACTION_DOWN:
								layout.setPressed(true);
								break;
							default:
								layout.setPressed(false);
								break;
						}
						return false;
					}
				});

			}
		}
	}

	private void setSexText()
	{
		EditText label = getEditText(R.id.sex_layout);
		if (sighting.SexId == null || sighting.SexId <= 0)
		{
			label.setText(null);
			return;
		}

		String text = null;

		SightingOptionsPrim.Sex sex = mOptions.getSexById(sighting.SexId);

		if (sex != null)
		{
			text = sex.fields.description;
		}

		label.setText(text);
	}

	private void setSizeText()
	{
		EditText label = getEditText(R.id.size_layout);
		if (sighting.Size == null || sighting.SizeMethodId <= 0)
		{
			label.setText(null);
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(sighting.Size));
		sb.append(String.valueOf("cm"));

		SightingOptionsPrim.SizeMethod sizeMethod = mOptions.getSizeMethodById(sighting.SizeMethodId);

		if (sizeMethod != null)
		{
			sb.append(" (");
			sb.append(sizeMethod.fields.description);
			sb.append(")");
		}

		label.setText(sb.toString());
	}

	private void setSpeciesText()
	{
		EditText label = getEditText(R.id.species_layout);
		if (sighting.SpeciesId > 0)
		{
			setSpeciesTextBySpecies(label);
		}
		else if (sighting.OtherSpecies != null && !sighting.OtherSpecies.isEmpty())
		{
			setSpeciesTextByOtherSpecies(label);
		}
		else
		{
			label.setText(null);
			return;
		}
	}

	private void setSpeciesTextBySpecies(EditText label)
	{
		String text = null;

		try
		{
			Species species = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository().getById(sighting.SpeciesId);
			text = String.format("%s (%s)", species.CommonName, species.SpeciesName);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		label.setText(text);
	}

	private void setSpeciesTextByOtherSpecies(EditText label)
	{
		label.setText(sighting.OtherSpecies);
	}

	private void setTimeText()
	{
		EditText label = getEditText(R.id.time_layout);
		if (sighting.TimeId <= 0)
		{
			label.setText(null);
			return;
		}

		SightingOptionsPrim.Time time = mOptions.time.get(sighting.TimeId - 1);
		String text = "Not sure";

		if (!time.fields.code.equals("NS"))
		{
			SimpleDateFormat df = new SimpleDateFormat("h:mm a");
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.fields.code));
			cal.set(Calendar.MINUTE, 0);
			text = df.format(cal.getTime());
		}

		label.setText(text);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		ActionBar actionbar = getActionBar();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			actionbar.setDisplayHomeAsUpEnabled(true);
		}
	}

	private void setupInputs()
	{
		clearValidationErrors();

		setPhotoImage();
		setAccuracyText();
		setActivityText();
		setCountText();
		setDateText();
		setHabitatText();
		setSexText();
		setSizeText();
		setSpeciesText();
		setTimeText();
		setWeightText();
		setTemperatureText();
		setDepthText();
		setLocationText();

		setAdditionalCommentsText();
		setAdditionalCommentsOnChange();
	}

	private void setAdditionalCommentsOnChange()
	{
		EditText additionalComments = (EditText) findViewById(R.id.additional_comments);
		additionalComments.addTextChangedListener(additionalCommentsOnChangeHandler);
	}

	private TextWatcher additionalCommentsOnChangeHandler = new TextWatcher()
	{
		public void afterTextChanged(Editable s)
		{
			sighting.AdditionalComments = s.toString();
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
		}

		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
		}
	};

	private void setWeightText()
	{
		EditText label = getEditText(R.id.weight_layout);
		if (sighting.Weight == null)
		{
			label.setText(null);
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(sighting.Weight));
		sb.append(String.valueOf("kg"));

		SightingOptionsPrim.WeightMethod weightMethod = mOptions.getWeightMethodById(sighting.WeightMethodId);

		if (weightMethod != null)
		{
			sb.append(" (");
			sb.append(weightMethod.fields.description);
			sb.append(")");
		}

		label.setText(sb.toString());
	}

	public void SexOnClick(View view)
	{
		CharSequence[] cs = new CharSequence[mOptions.sex.size()];
		int selectedId = -1;
		for (int i = 0; i < mOptions.sex.size(); i++)
		{
			SightingOptionsPrim.Sex sex = mOptions.sex.get(i);
			cs[i] = sex.fields.description;
			if (sighting.SexId == sex.pk) selectedId = i;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Select the sex").setSingleChoiceItems(cs, selectedId, sexOnClickHandler);
		builder.create().show();
	}

	public void showDatePickerDialog(View v)
	{
		datePicker.show(getFragmentManager(), "datePicker");
	}

	public void SizeOnClick(View view)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// Set an EditText view to get user input
		EditText inputField = new EditText(this);
		inputField.setId(R.id.value_label);
		inputField.setInputType(InputType.TYPE_CLASS_NUMBER);
		if (sighting.Size != null)
		{
			inputField.setText(sighting.Size.toString());
		}
		inputField.selectAll();
		alert.setView(inputField);
		alert.setTitle("Sighting size");
		alert.setMessage("Please enter the size in centimeters");
		alert.setPositiveButton("Measured", getSizeOnClickMeasuredHandler(inputField));
		alert.setNegativeButton("Estimated", getSizeOnClickEstimatedHandler(inputField));

		// Show keyboard and focus because android is silly
		AlertDialog dialog = alert.show();
		focusAlertDialogEditText(dialog, inputField);
	}

	public void SpeciesOnClick(View view)
	{
		Intent intent = new Intent(getApplicationContext(), SpeciesCategoriesActivity.class);
		intent.putExtra("isLogSightingSelect", true);
		startActivityForResult(intent, AR_SELECT_SPECIES);
	}

	public void Submit()
	{
		Provider.ProcessValidSighting(sighting);

		if (Provider.hasCurrentUser())
		{
			Provider.AsyncSubmitPendingSightings(Provider.getCurrentUser());
		}

		completeSubmit();
	}

	private void completeSubmit()
	{
		loadDraft();
		Intent intent = new Intent(getApplicationContext(), PersonalMapActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	public boolean Validate()
	{

		boolean isValid = true;
		this.firstInvalidEditText = null;

		if (!validatePhoto()) isValid = false;
		if (!validateSpecies()) isValid = false;
		if (!validateLocation()) isValid = false;
		if (!validateActivity()) isValid = false;
		if (!validateAccuracy()) isValid = false;
		if (!validateDate()) isValid = false;
		if (!validateTime()) isValid = false;
		if (!validateCount()) isValid = false;
		if (!validateSex()) isValid = false;

		if (!isValid && this.firstInvalidEditText != null)
		{
			this.firstInvalidEditText.requestFocus();
		}

		return isValid;
	};

	private void clearValidationErrors()
	{
		List<RelativeLayout> layouts = new ArrayList<RelativeLayout>();
		layouts.add((RelativeLayout) findViewById(R.id.species_layout));
		layouts.add((RelativeLayout) findViewById(R.id.activity_layout));
		layouts.add((RelativeLayout) findViewById(R.id.accuracy_layout));
		layouts.add((RelativeLayout) findViewById(R.id.location_layout));
		layouts.add((RelativeLayout) findViewById(R.id.date_layout));
		layouts.add((RelativeLayout) findViewById(R.id.time_layout));
		layouts.add((RelativeLayout) findViewById(R.id.count_layout));

		for (final RelativeLayout layout : layouts)
		{
			EditText field = (EditText) layout.findViewById(R.id.value_label);
			field.setError(null);
		}
	}

	private boolean validateAccuracy()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.accuracy_layout);
		if (sighting.AccuracyId <= 0)
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	};

	private boolean validateActivity()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.activity_layout);
		if (sighting.ActivityId <= 0)
		{
			field.setError(REQUIRED_MSG);
			field.invalidate();
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	}

	private boolean validateCount()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.count_layout);
		if (sighting.CountId <= 0)
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	};

	private boolean validateDate()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.date_layout);
		if (sighting.SightingDateDay <= 0 && sighting.SightingDateMonth <= 0 && sighting.SightingDateYear <= 0)
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, sighting.SightingDateYear);
		cal.set(Calendar.MONTH, sighting.SightingDateMonth);
		cal.set(Calendar.DAY_OF_MONTH, sighting.SightingDateDay);
		java.util.Date date = cal.getTime();
		Calendar now = Calendar.getInstance();

		if (date.after(now.getTime()))
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	};

	private boolean validateSpecies()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.species_layout);
		if (sighting.SpeciesId <= 0 && (sighting.OtherSpecies == null || sighting.OtherSpecies.isEmpty()))
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	}

	private boolean validateSex()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.sex_layout);
		if (sighting.SexId == null || sighting.SexId <= 0)
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	}

	private boolean validatePhoto()
	{
		boolean isValid = true;
		ImageView icon = (ImageView) findViewById(R.id.log_sighting_photo_error);
		if (!sighting.HasPicture() && tempPhoto == null)
		{
			icon.setVisibility(ImageView.VISIBLE);
			return false;
		}

		if (isValid)
		{
			icon.setVisibility(ImageView.INVISIBLE);
		}

		return isValid;
	}

	private boolean validateTime()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.time_layout);
		if (sighting.TimeId <= 0)
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	}

	private boolean validateLocation()
	{
		boolean isValid = true;
		EditText field = getEditText(R.id.location_layout);
		if (sighting.Latitude == null || sighting.Longitude == null)
		{
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid)
		{
			field.setError(null);
		}

		return isValid;
	}

	public void WeightOnClick(View view)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// Set an EditText view to get user input
		EditText inputField = new EditText(this);
		inputField.setId(R.id.value_label);
		inputField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		if (sighting.Weight != null)
		{
			inputField.setText(sighting.Weight.toString());
		}
		inputField.selectAll();
		alert.setView(inputField);
		alert.setTitle("Sighting weight");
		alert.setMessage("Please enter the weight in kilograms");
		alert.setPositiveButton("Measured", getWeightOnClickMeasuredHandler(inputField));
		alert.setNegativeButton("Estimated", getWeightOnClickEstimatedHandler(inputField));

		// Show keyboard and focus because android is silly
		AlertDialog dialog = alert.show();
		focusAlertDialogEditText(dialog, inputField);
	}

	public void DepthOnClick(View view)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// Set an EditText view to get user input
		EditText inputField = new EditText(this);
		inputField.setId(R.id.value_label);
		inputField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		if (sighting.Depth != null)
		{
			inputField.setText(String.valueOf(sighting.Depth));
		}
		inputField.selectAll();
		alert.setView(inputField);
		alert.setTitle("Sighting depth");
		alert.setMessage("Please enter the depth in meters");
		alert.setPositiveButton("Ok", getDepthOnClickMeasuredHandler(inputField));
		alert.setNegativeButton("Cancel", null);

		// Show keyboard and focus because android is silly
		AlertDialog dialog = alert.show();
		focusAlertDialogEditText(dialog, inputField);
	}

	private DialogInterface.OnClickListener getDepthOnClickMeasuredHandler(EditText field)
	{

		final EditText resultField = field;

		DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				String value = resultField.getText().toString();
				try
				{
					sighting.Depth = Integer.valueOf(value);
				}
				catch (NumberFormatException e)
				{
					sighting.Depth = null;
					if (!value.isEmpty())
					{
						EditText field = getEditText(R.id.depth_layout);
						field.setError("Invalid value");
					}
				}
				setDepthText();
				fieldChanged();
			}
		};
		return handler;
	}

	public void TemperatureOnClick(View view)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// Set an EditText view to get user input
		EditText inputField = new EditText(this);
		inputField.setId(R.id.value_label);
		inputField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		if (sighting.Temperature != null)
		{
			inputField.setText(String.valueOf(sighting.Temperature));
		}
		inputField.selectAll();
		alert.setView(inputField);
		alert.setTitle("Water temperature");
		alert.setMessage("Please enter the water temperature in celsius");
		alert.setPositiveButton("Ok", getTemperatureOnClickMeasuredHandler(inputField));
		alert.setNegativeButton("Cancel", null);

		// Show keyboard and focus because android is silly
		AlertDialog dialog = alert.show();
		focusAlertDialogEditText(dialog, inputField);
	}

	private DialogInterface.OnClickListener getTemperatureOnClickMeasuredHandler(EditText field)
	{

		final EditText resultField = field;

		DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				String value = resultField.getText().toString();
				try
				{
					sighting.Temperature = Integer.valueOf(value);
				}
				catch (NumberFormatException e)
				{
					sighting.Temperature = null;
					if (!value.isEmpty())
					{
						EditText field = getEditText(R.id.temperature_layout);
						field.setError("Invalid value");
					}
				}
				setTemperatureText();
				fieldChanged();
			}
		};
		return handler;
	}

	private void focusAlertDialogEditText(AlertDialog dialog, EditText editText)
	{
		Window window = dialog.getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		editText.requestFocus();
	}
	
	/*
	 * http://stackoverflow.com/questions/12726860/android-how-to-detect-the-image
	 * -orientation-portrait-or-landscape-picked-fro
	 */
	private int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath)
	{
		int rotate = 0;
		try
		{
			context.getContentResolver().notifyChange(imageUri, null);
			File imageFile = new File(imagePath);

			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation)
			{
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;
			}

			Log.i("RotateImage", "Exif orientation: " + orientation);
			Log.i("RotateImage", "Rotate value: " + rotate);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return rotate;
	}
}
