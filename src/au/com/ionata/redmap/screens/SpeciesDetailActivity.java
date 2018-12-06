package au.com.ionata.redmap.screens;

import java.io.ByteArrayOutputStream;
import java.util.UnknownFormatConversionException;

import com.google.analytics.tracking.android.EasyTracker;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.data.tasks.GetSpeciesByIdAsyncTask;
import au.com.ionata.redmap.http.interfaces.GetSpeciesByIdCompleteInterface;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.overlay.LoadingOverlayActivityBase;

public class SpeciesDetailActivity extends LoadingOverlayActivityBase implements GetSpeciesByIdCompleteInterface
{

	private int speciesId;
	private int speciesCategoryId;

	private ImageView SpeciesImage;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_species_detail);
		SpeciesImage = (ImageView) findViewById(R.id.speciesImageView);
		SpeciesImage.setVisibility(View.INVISIBLE);

		// Show the Up button in the action bar.
		setupActionBar();

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("speciesId"))
		{
			speciesId = extras.getInt("speciesId");
			speciesCategoryId = extras.getInt("speciesCategoryId");
			GetSpeciesByIdAsyncTask at = new GetSpeciesByIdAsyncTask(this, getApplicationContext(), speciesId);
			at.execute();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_species_detail, menu);

		MenuItem item = menu.findItem(R.id.button_log_it_item);

		LayoutInflater linflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = linflater.inflate(R.layout.button_visit_web_layout, null);

		Button button = (Button) view.findViewById(R.id.button_visit_website);
		button.setText(getResources().getString(R.string.button_log_it_label));
		button.setOnClickListener(logItOnClickHandler);
		item.setActionView(view);

		return true;
	}

	private OnClickListener logItOnClickHandler = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(getApplicationContext(), LogSightingActivity.class);
			intent.putExtra("speciesId", speciesId);
			intent.putExtra("logItButton", true);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateInterface(Species species)
	{
		// gather elements
		TextView speciesNameLabel = (TextView) findViewById(R.id.speciesNameLabel);
		TextView commonNameLabel = (TextView) findViewById(R.id.commonNameLabel);
		ImageView speciesImageView = (ImageView) findViewById(R.id.speciesImageView);
		TextView imageCreditView = (TextView) findViewById(R.id.image_credit);
		WebView descriptionWebView = (WebView) findViewById(R.id.description_text);

		speciesNameLabel.setText(species.SpeciesName);
		commonNameLabel.setText(species.CommonName);
		String html = prepareDescriptionHtml(species);
		
		

		descriptionWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);

		if (species.ImageCredit != null && !species.ImageCredit.isEmpty())
		{
			String credit = String.format("(Image credit: %s)", species.ImageCredit);
			imageCreditView.setText(credit);
		}
		else
		{
			imageCreditView.setVisibility(View.GONE);
		}

		if (species.HasPicture())
		{
			speciesImageView.setImageBitmap(species.GetPictureBitmap());
			speciesImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			speciesImageView.setImageDrawable(getResources().getDrawable(R.drawable.fish_silhouette));
			speciesImageView.setVisibility(View.VISIBLE);
		}
	}

	private String prepareDescriptionHtml(Species species)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int fontSize = Math.round(16 * (metrics.densityDpi / 160));
		
		String html = "";

		String head_open = "<!doctype html><html class=\"no-js\" lang=\"en-AU\"><head><meta name=\"viewport\" content=\"target-densitydpi=device-dpi, width=device-width\" /><meta charset=\"utf-8\"><style type=\"text/css\">";
		// String style =
		// "/* @font-face {font-family: AlphaMacAOE; src: url('AlphaMacAOE.ttf'); font-weight: normal;} */html,body,p {padding:0; margin:0;}h2 {background-color: #ee2e24;color: white; padding: 5px 20px 10px 40px; display: block; font-family: AlphaMacAOE, \"AlphaMack AOE\"; font-size: 35px;position: absolute; line-height: 1; margin-left: 10px; }";
		String style = "@font-face {font-family: 'AlphaMacAOE'; src: url('AlphaMacAOE.ttf');font-weight: normal;} html,body,p {padding:0; margin:0; font-size: " + fontSize + "px;} h3 {padding:0; margin:0;font-weight: normal; color: #082c5d; font-family: 'AlphaMacAOE'; font-size: 2.5em;}";
		String head_close = "</style></head><body>";
		String footer = "</body></html>";

		if (species.HasDistributionPicture())
		{

			Bitmap map = BitmapFactory.decodeResource(getResources(), R.drawable.australia_google_map);
			Bitmap distroMap = species.GetDistributionPictureOverlayBitmap(map);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			distroMap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] distroMapByteArray = stream.toByteArray();

			// int width = 100;
			int width = (int)Math.round(metrics.widthPixels * 0.33);

			style += " img {padding: 0px 0px 15px 15px; float:right;width: " + width + "px;}";
			String tpl = head_open + style + head_close + "<img src=\"data:image/png;base64,%s\">";

			String tplLotIt = "<h3>Log it</h3><p>%s</p>";
			if (!species.Notes.isEmpty())
			{
				tpl += String.format(tplLotIt, species.Notes);
			}

			String tplDescription = "<h3>Species details</h3><p>%s</p>";
			if (!species.Description.isEmpty())
			{
				tpl += String.format(tplDescription, species.Description);
			}

			tpl += footer;

			String imageBase64String = Base64.encodeToString(distroMapByteArray, Base64.DEFAULT);
			try
			{
				html = String.format(tpl, imageBase64String);
			}
			catch (UnknownFormatConversionException e)
			{
				e.printStackTrace();
				Log.d("html fail", tpl);
			}
		}
		else
		{
			String tpl = head_open + style + head_close;

			String tplLotIt = "<h3>Log it</h3>%s";
			if (!species.Notes.isEmpty())
			{
				tpl += String.format(tplLotIt, species.Notes);
			}

			String tplDescription = "<h3>Species details</h3>%s";
			if (!species.Description.isEmpty())
			{
				tpl += String.format(tplDescription, species.Description);
			}

			tpl += footer;

			html = tpl;
		}

		return html;
	}

	@Override
	public void onGetSpeciesByIdComplete(Species species)
	{
		updateInterface(species);
		dismissLoadingOverlay();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		intent.putExtra("speciesCategoryId", speciesCategoryId);
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
