package au.com.ionata.redmap.api.interfaces;

import java.util.List;

import au.com.ionata.redmap.api.models.UserSightingPrim;
import au.com.ionata.redmap.models.Sighting;

public interface ISubmitSightingComplete {
	public List<Sighting> GetResult();
}
