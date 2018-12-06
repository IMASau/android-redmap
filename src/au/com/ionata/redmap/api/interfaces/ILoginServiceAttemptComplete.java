package au.com.ionata.redmap.api.interfaces;

public interface ILoginServiceAttemptComplete {
	public void Complete(boolean success, String message, String token);
}
