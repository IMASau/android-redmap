package au.com.ionata.redmap.data.tasks;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import android.util.Log;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.data.repositories.ISpeciesRepository;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.utils.Http;

public class SpeciesService
{
	private static class RejectedHandler implements RejectedExecutionHandler
	{
		@Override
		public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1)
		{
			System.err.println(Thread.currentThread().getName() + " execution rejected: " + arg0);
		}
	}

	private static abstract class BasePictureTask implements Runnable{
		
		protected int id;

		public BasePictureTask(Species species)
		{
			id = species.Id;
		}
		
		@Override
		public void run()
		{
			byte[] pictureData = Http.getByteResponse(getUrl());
			save(pictureData);
			System.out.println(Thread.currentThread().getName() + " executed " + this);
		}
		
		protected abstract void setPicture(Species species, byte[] picture);
		
		protected abstract String getUrl();
		
		private void save(byte[] picture){
			try
			{
				ISpeciesRepository repository = RedmapContext.getInstance().Provider.AppRepositories.SpeciesRepository();
				Species species = repository.getById(id);
				
				if (species == null)
				{
					Log.d("SpeciesService", "Species object was missing from data store on save attempt, id:" + id);
					return;
				}
				
				// Set the new image
				setPicture(species, picture);
				repository.save(species);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static class PictureTask extends BasePictureTask
	{
		private String url;
		
		public PictureTask(Species species)
		{
			super(species);
			url = species.PictureUrl;
		}

		@Override
		protected String getUrl()
		{
			return url;
		}
		
		@Override
		protected void setPicture(Species species, byte[] picture)
		{
			species.SetPicture(picture);
			species.UpdatePictureThumbnail();
		}
	}
	
	private static class DistributionPictureTask extends BasePictureTask
	{
		private String url;
		
		public DistributionPictureTask(Species species)
		{
			super(species);
			url = species.DistributionUrl;
		}
		
		@Override
		protected String getUrl()
		{
			return url;
		}

		@Override
		protected void setPicture(Species species, byte[] picture)
		{
			species.SetDistributionPicture(picture);
		}
	}

	private ExecutorService service;
	private static ThreadPoolExecutor executor;
	
	public SpeciesService(){
		// fixed pool, unlimited queue
		service = Executors.newFixedThreadPool(5);
		executor = (ThreadPoolExecutor) service;
		
		// set rejected execution handler
		// or catch exception from executor.execute (see below)
		final boolean wantExceptionOnReject = false;
		if (!wantExceptionOnReject) executor.setRejectedExecutionHandler(new RejectedHandler());
	}
	
	public void UpdatePicture(Species species)
	{
		PictureTask task = new PictureTask(species);
		executeTask(task);
	}
	
	public void UpdateDistributionPicture(Species species)
	{
		DistributionPictureTask task = new DistributionPictureTask(species);
		executeTask(task);
	}
	
	private void executeTask(Runnable task)
	{
		System.out.println(Thread.currentThread().getName() + " submitted " + task + ", queue size = " + executor.getQueue().size());
		try
		{
			executor.execute(task);
		}
		catch (RejectedExecutionException e)
		{
			// will be thrown if rejected execution handler
			// is not set with executor.setRejectedExecutionHandler
			e.printStackTrace();
		}
	}
}
