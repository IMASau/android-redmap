package au.com.ionata.redmap.data;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.api.impl.BaseListService;
import au.com.ionata.redmap.api.impl.RegionApiService;
import au.com.ionata.redmap.api.impl.SightingOptionsApiService;
import au.com.ionata.redmap.api.impl.SpeciesApiService;
import au.com.ionata.redmap.api.impl.SpeciesCategoriesApiService;
import au.com.ionata.redmap.api.impl.SubmitSightingService;
import au.com.ionata.redmap.api.impl.UserSightingApiService;
import au.com.ionata.redmap.api.interfaces.ISubmitSightingComplete;
import au.com.ionata.redmap.api.models.RegionListPrim;
import au.com.ionata.redmap.api.models.RegionPrim;
import au.com.ionata.redmap.api.models.SightingOptionsListPrim;
import au.com.ionata.redmap.api.models.SightingOptionsPrim;
import au.com.ionata.redmap.api.models.SpeciesCategoriesListPrim;
import au.com.ionata.redmap.api.models.SpeciesCategoryPrim;
import au.com.ionata.redmap.api.models.SpeciesListPrim;
import au.com.ionata.redmap.api.models.SpeciesPrim;
import au.com.ionata.redmap.api.models.UserSightingListPrim;
import au.com.ionata.redmap.api.models.UserSightingPrim;
import au.com.ionata.redmap.data.repositories.IRegionRepository;
import au.com.ionata.redmap.data.repositories.ISettingsRepository;
import au.com.ionata.redmap.data.repositories.ISightingRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesCategoryRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesCategorySpeciesRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesRegionRepository;
import au.com.ionata.redmap.data.repositories.ISpeciesRepository;
import au.com.ionata.redmap.data.repositories.IUserRepository;
import au.com.ionata.redmap.data.tasks.SpeciesService;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Settings;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.SpeciesCategory;
import au.com.ionata.redmap.models.SpeciesCategorySpecies;
import au.com.ionata.redmap.models.SpeciesRegion;
import au.com.ionata.redmap.models.User;
import au.com.ionata.redmap.screens.LogSightingActivity;
import au.com.ionata.redmap.screens.LoginActivity;
import au.com.ionata.redmap.screens.SightingDetailActivity;
import au.com.ionata.redmap.utils.Callback;
import au.com.ionata.redmap.utils.Http;
import au.com.ionata.redmap.utils.graphics.Imaging;

public class Provider
{

	public DatabaseHelper DatabaseHelper;
	public IAppRepository AppRepositories;

	private boolean regionsUpdateComplete = false;
	private boolean speciesCategoriesUpdateComplete = false;

	private SpeciesService speciesService;

	public Provider(Context context)
	{
		this.DatabaseHelper = new DatabaseHelper(context);
		this.AppRepositories = new AppRepository(this.DatabaseHelper.getConnectionSource());

		speciesService = new SpeciesService();
	}

	public void clearDatabase()
	{
		try
		{
			AppRepositories.RegionRepository().deleteAll();
			AppRepositories.SpeciesCategorySpeciesRepository().deleteAll();
			AppRepositories.SpeciesCategoryRepository().deleteAll();
			AppRepositories.SpeciesRepository().deleteAll();
			AppRepositories.SpeciesRegionRepository().deleteAll();
			AppRepositories.SettingsRepository().deleteAll();
			AppRepositories.SightingRepository().deleteAll();
			AppRepositories.UserRepository().deleteAll();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Settings getSettings()
	{
		ISettingsRepository repo;
		Settings settings = null;
		try
		{
			repo = AppRepositories.SettingsRepository();
			Iterator<Settings> itr = repo.getAll().iterator();
			if (itr.hasNext())
			{
				return itr.next();
			}
		}
		catch (SQLException e)
		{
		}
		finally
		{
			if (settings == null)
			{
				settings = new Settings();
			}
		}
		return settings;
	}

	public User getCurrentUser()
	{
		IUserRepository repo;
		User user = null;
		try
		{
			repo = AppRepositories.UserRepository();
			user = repo.getCurrentUser();
		}
		catch (SQLException ex)
		{
			throw new RedmapRuntimeException(ex);
		}
		return user;
	}

	public boolean hasCurrentUser()
	{
		User user = getCurrentUser();
		return user != null;
	}

	public boolean draftSightingExists()
	{
		Sighting sighting = getDraftSighting();
		return sighting == null;
	}

	public Sighting getDraftSighting()
	{
		ISightingRepository repo;
		Sighting sighting = null;
		try
		{
			repo = AppRepositories.SightingRepository();
			Iterator<Sighting> itr = repo.getByFieldEquals("Status", Sighting.STATUS_DRAFT).iterator();
			if (itr.hasNext())
			{
				return itr.next();
			}
		}
		catch (SQLException e)
		{
		}
		return sighting;
	}

	public List<Species> getSpecies()
	{
		try
		{
			return AppRepositories.SpeciesRepository().getAll();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public List<SpeciesCategorySpecies> getSpeciesCategoriesSpecies() throws SQLException
	{
		return AppRepositories.SpeciesCategorySpeciesRepository().getAll();
	}

	public void AsyncUpdate()
	{
		AsyncTask task = new AsyncTask()
		{
			@Override
			protected Object doInBackground(Object... params)
			{
				try
				{
					Update();
				}
				catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
		task.execute();
	}

	public void Update() throws SQLException
	{
		Log.d("Provider", "Data sync update starting");
		regionsUpdateComplete = false;
		speciesCategoriesUpdateComplete = false;

		UpdateRegions();
		UpdateSpeciesCategories();
		UpdateSettings();
	}

	private void onUpdateRegionsComplete(BaseListService<RegionPrim, RegionListPrim> service)
	{
		try
		{
			IRegionRepository repository = AppRepositories.RegionRepository();
			for (RegionPrim regionPrim : (Collection<RegionPrim>) service.GetResults())
			{
				Region region = new Region(regionPrim);
				repository.save(region);
			}
			regionsUpdateComplete = true;
			UpdateSpecies();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	private void onUpdateSpeciesCategoryComplete(BaseListService<SpeciesCategoryPrim, SpeciesCategoriesListPrim> service)
	{
		try
		{
			final ISpeciesCategoryRepository repository = AppRepositories.SpeciesCategoryRepository();
			for (SpeciesCategoryPrim speciesCategoryPrim : (Collection<SpeciesCategoryPrim>) service.GetResults())
			{
				SpeciesCategory speciesCategory = new SpeciesCategory(speciesCategoryPrim);

				// picture
				if (speciesCategoryPrim.picture_url != null)
				{
					byte[] pictureData = Http.getByteResponse(speciesCategoryPrim.picture_url);
					speciesCategory.Picture = pictureData;
				}

				repository.save(speciesCategory);
			}
			speciesCategoriesUpdateComplete = true;
			UpdateSpecies();
		}
		catch (SQLException e)
		{
		}
		finally
		{
		}
	}

	private void onSettingsUpdateComplete(BaseListService<SightingOptionsPrim, SightingOptionsListPrim> service)
	{
		try
		{
			ISettingsRepository repository = AppRepositories.SettingsRepository();
			for (SightingOptionsPrim sightingOptionsPrim : (Collection<SightingOptionsPrim>) service.GetResults())
			{
				Settings settings = getSettings();
				settings.SightingOptions = sightingOptionsPrim.response;
				settings.LastSync = new Date();
				repository.save(settings);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	public void UpdateRegions() throws SQLException
	{
		Log.d("Provider", "Begin Region update");
		final Provider provider = this;
		Callback<BaseListService<RegionPrim, RegionListPrim>> callback = new Callback<BaseListService<RegionPrim, RegionListPrim>>()
		{
			@Override
			public void Complete(BaseListService<RegionPrim, RegionListPrim> service)
			{
				provider.onUpdateRegionsComplete(service);
			}
		};

		RegionApiService service = new RegionApiService();
		service.AddOnFinishedListener(callback);
		service.Execute();
	}

	public void UpdateSettings() throws SQLException
	{
		Log.d("Provider", "Begin Settings update");
		final Provider provider = this;
		Callback<BaseListService<SightingOptionsPrim, SightingOptionsListPrim>> callback = new Callback<BaseListService<SightingOptionsPrim, SightingOptionsListPrim>>()
		{
			@Override
			public void Complete(BaseListService<SightingOptionsPrim, SightingOptionsListPrim> service)
			{
				provider.onSettingsUpdateComplete(service);
			}
		};

		SightingOptionsApiService service = new SightingOptionsApiService();
		service.AddOnFinishedListener(callback);
		service.Execute();
	}

	public void UpdateSpecies() throws SQLException
	{
		if (!regionsUpdateComplete || !speciesCategoriesUpdateComplete)
		{
			return;
		}
		Log.d("Provider", "Begin Species update");

		final ISpeciesRepository repository = AppRepositories.SpeciesRepository();

		// Relations
		final ISpeciesCategorySpeciesRepository speciesCategorySpeciesRepository = AppRepositories.SpeciesCategorySpeciesRepository();
		final List<SpeciesCategory> speciesCategories = AppRepositories.SpeciesCategoryRepository().getAll();

		// Relations
		final ISpeciesRegionRepository speciesRegionRepository = AppRepositories.SpeciesRegionRepository();
		final List<Region> regions = AppRepositories.RegionRepository().getAll();

		Callback<BaseListService<SpeciesPrim, SpeciesListPrim>> callback = new Callback<BaseListService<SpeciesPrim, SpeciesListPrim>>()
		{
			@Override
			public void Complete(BaseListService<SpeciesPrim, SpeciesListPrim> service)
			{

				try
				{
					Collection<SpeciesPrim> speciesPrimCollection = (Collection<SpeciesPrim>) service.GetResults();
					Log.d("Provider", String.format("UpdateSpecies processing %s", speciesPrimCollection.size()));
					for (SpeciesPrim speciesPrim : speciesPrimCollection)
					{

						boolean updatePicture = false;
						boolean updateDistributionPicture = false;

						Species species = repository.getById(speciesPrim.id);

						if (species == null)
						{
							species = new Species(speciesPrim);
						}

						// Picture
						if (!species.HasPicture() || !species.PictureUrl.equals(speciesPrim.picture_url))
						{
							if (speciesPrim.picture_url != null) updatePicture = true;
						}
						else if (species.HasPicture() && !species.HasPictureThumbnail())
						{
							// Picture thumbnail needs an update
							species.UpdatePictureThumbnail();
						}

						// Distribution picture
						if (!species.HasDistributionPicture() || !species.DistributionUrl.equals(speciesPrim.distribution_url))
						{
							if (speciesPrim.distribution_url != null) updateDistributionPicture = true;
						}

						species.UpdateAttributes(speciesPrim);
						repository.save(species);

						// Add species to update image task queues?
						if (updatePicture) speciesService.UpdatePicture(species);
						if (updateDistributionPicture) speciesService.UpdateDistributionPicture(species);

						// Add category relations
						if (speciesPrim.category_id_list != null)
						{
							for (int i : speciesPrim.category_id_list)
							{
								Iterator<SpeciesCategory> it = speciesCategories.iterator();
								while (it.hasNext())
								{
									SpeciesCategory speciesCategory = it.next();

									if (speciesCategorySpeciesRepository.SpeciesInCategory(species, speciesCategory)) break; // relation already exists from previous sync

									if (speciesCategory.Id == i)
									{
										SpeciesCategorySpecies speciesCategorySpecies = new SpeciesCategorySpecies(species, speciesCategory);
										speciesCategorySpeciesRepository.save(speciesCategorySpecies);
									}
								}
							}
						}

						// Add region relations
						if (speciesPrim.region_id_list != null)
						{
							for (int i : speciesPrim.region_id_list)
							{
								Iterator<Region> it = regions.iterator();
								while (it.hasNext())
								{
									Region region = it.next();

									if (speciesRegionRepository.SpeciesInRegion(species, region)) break; // relation already exists from previous sync

									if (region.Id == i)
									{
										SpeciesRegion speciesRegion = new SpeciesRegion(species, region);
										speciesRegionRepository.save(speciesRegion);
									}
								}
							}
						}
						Log.d("Provider", String.format("UpdateSpecies finished: %s PK", speciesPrim.id));
					}
				}
				catch (SQLException e)
				{
				}
				finally
				{
				}
			}
		};

		SpeciesApiService service = new SpeciesApiService();
		service.AddOnFinishedListener(callback);
		service.Execute();
	}

	public void UpdateSpeciesCategories() throws SQLException
	{
		Log.d("Provider", "Begin SpeciesCategories update");
		final Provider provider = this;
		final Callback<BaseListService<SpeciesCategoryPrim, SpeciesCategoriesListPrim>> callback = new Callback<BaseListService<SpeciesCategoryPrim, SpeciesCategoriesListPrim>>()
		{
			@Override
			public void Complete(BaseListService<SpeciesCategoryPrim, SpeciesCategoriesListPrim> service)
			{
				provider.onUpdateSpeciesCategoryComplete(service);
			}
		};

		SpeciesCategoriesApiService service = new SpeciesCategoriesApiService();
		service.AddOnFinishedListener(callback);
		service.Execute();
	}

	public void AsyncCurrentUserSightingsSync()
	{
		AsyncTask task = new AsyncTask()
		{
			@Override
			protected Object doInBackground(Object... params)
			{

				RedmapContext.getInstance().Provider.CurrentUserSightingsSync();
				return null;
			}
		};
		task.execute();
	}

	public void CurrentUserSightingsSync()
	{

		if (!hasCurrentUser()) return;
		String token = getCurrentUser().AuthToken;
		SyncCurrentUserSightings(token);
	}

	public void SyncCurrentUserSightings(final String token)
	{
		SyncCurrentUserSightings(token, null);
	}

	public void SyncCurrentUserSightings(final String token, final Callback<Void> finishedCallback)
	{
		if (!hasCurrentUser()) return;
		
		final Provider provider = this;
		final Callback<BaseListService<UserSightingPrim, UserSightingListPrim>> callback = new Callback<BaseListService<UserSightingPrim, UserSightingListPrim>>()
		{
			@Override
			public void Complete(BaseListService<UserSightingPrim, UserSightingListPrim> service)
			{
				provider.onSyncCurrentUserSightingsComplete(service);
				if (finishedCallback != null)
				{
					finishedCallback.Complete(null);
				}
			}
		};

		SubmitPendingSightings(getCurrentUser());
		UserSightingApiService service = new UserSightingApiService(getContext(), token);
		service.AddOnFinishedListener(callback);
		service.Execute();
	}

	private void onSyncCurrentUserSightingsComplete(BaseListService<UserSightingPrim, UserSightingListPrim> service)
	{
		if (!hasCurrentUser()) return;
		
		User user = getCurrentUser();

		for (UserSightingPrim userSightingPrim : (Collection<UserSightingPrim>) service.GetResults())
		{
			SyncSighting(userSightingPrim, user);
			Log.d("Provider", String.format("onSyncCurrentUserSightingsComplete %s", user.Username));
		}
	}

	private void SyncSighting(UserSightingPrim userSightingPrim, User user)
	{
		Log.d("Provider", String.format("SyncSighting %s", user.Username));
		try
		{
			final ISightingRepository repository = AppRepositories.SightingRepository();
			boolean created = false;
			Sighting sighting = repository.getByPk(userSightingPrim.id);

			if (sighting == null)
			{
				sighting = new Sighting(userSightingPrim, user);
				created = true;
			}
			else
			{
				sighting.UpdateAttributes(userSightingPrim, user);
			}

			// picture
			if (userSightingPrim.photo_url != null && !userSightingPrim.photo_url.isEmpty())
			{
				if (created || userSightingPrim.photo_url != sighting.PictureUrl)
				{
					byte[] pictureData = Http.getByteResponse(userSightingPrim.photo_url);
					sighting.SetPicture(pictureData);
					sighting.UpdatePictureThumbnail();
				}
			}

			if (sighting.HasPicture() && !sighting.HasPictureThumbnail())
			{
				sighting.UpdatePictureThumbnail();
			}

			sighting.Status = Sighting.STATUS_SYNCED;
			repository.save(sighting);
		}
		catch (SQLException e)
		{
		}
		finally
		{
		}
	}

	public void ProcessValidSighting(Sighting sighting)
	{
		try
		{
			final ISightingRepository repository = AppRepositories.SightingRepository();
			sighting.Status = Sighting.STATUS_PENDING;
			sighting.UpdateLoggingDate();
			repository.save(sighting);
		}
		catch (SQLException e)
		{
			throw new RedmapRuntimeException(e);
		}
		finally
		{
		}
	}

	private Context getContext()
	{
		return RedmapContext.getInstance().GetContext();
	}

	public void sendSuccessfulSightingSubmissionNotification(Sighting sighting)
	{
		Context ctx = getContext();

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_launcher);

		mBuilder.setContentTitle("Sighting submitted!");
		mBuilder.setContentText("Your sighting has been successfully submitted for verification!");

		// Creates an explicit intent for an Activity in your app
		Intent intent = new Intent(ctx, SightingDetailActivity.class);
		intent.putExtra("sightingId", sighting.Id);

		// The stack builder object will contain an artificial back stack for
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);

		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(SightingDetailActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(intent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		// Send notification
		NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = mBuilder.build();
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(1, notification);
	}

	public void AsyncSubmitPendingSightings(final User user){
		AsyncTask task = new AsyncTask()
		{
			@Override
			protected Object doInBackground(Object... params)
			{

				RedmapContext.getInstance().Provider.SubmitPendingSightings(user);
				return null;
			}
		};
		task.execute();
	}
	
	public void SubmitPendingSightings(User user)
	{
		List<Sighting> sightings;
		try
		{
			ISightingRepository repository = AppRepositories.SightingRepository();
			sightings = repository.getPendingSightings();

			Log.d("Provider", String.format("SubmitPendingSightings called for %s, submiting %s", user.Username, sightings.size()));

			SubmitSightingService submitSightingService = new SubmitSightingService(sightings);
			submitSightingService.AddOnFinishedListener(new Callback<ISubmitSightingComplete>()
			{
				@Override
				public void Complete(ISubmitSightingComplete service)
				{
					List<Sighting> sightings = service.GetResult();
					for (Sighting sighting : sightings){

						ISightingRepository repository;
						try
						{
							repository = AppRepositories.SightingRepository();
							sighting.Status = Sighting.STATUS_SYNCED;
							if (hasCurrentUser())
							{
								sighting.User = getCurrentUser();
							}
							repository.save(sighting);
							sendSuccessfulSightingSubmissionNotification(sighting);
						}
						catch (SQLException e)
						{
							e.printStackTrace();
							throw new RedmapRuntimeException(e);
						}
					}
				}
			});
			submitSightingService.Execute();


		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
