package au.com.ionata.redmap.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.j256.ormlite.stmt.PreparedQuery;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sromku.simple.fb.SimpleFacebook;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import au.com.ionata.redmap.api.impl.UserDetailService;
import au.com.ionata.redmap.api.impl.UserFacebookLoginService;
import au.com.ionata.redmap.api.impl.UserLoginService;
import au.com.ionata.redmap.api.interfaces.ILoginServiceAttemptComplete;
import au.com.ionata.redmap.api.models.UserDetailPrim;
import au.com.ionata.redmap.data.repositories.IRegionRepository;
import au.com.ionata.redmap.data.repositories.IUserRepository;
import au.com.ionata.redmap.exceptions.RedmapRuntimeException;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.User;
import au.com.ionata.redmap.overlay.BaseActivity;
import au.com.ionata.redmap.utils.Callback;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapApplication;
import au.com.ionata.redmap.RedmapContext;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends BaseActivity
{

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	// fb setup
	private UiLifecycleHelper fbUiLifecycleHelper;
	private Button mFacebookLoginButton;
	private boolean isResumed = false;
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private AsyncHttpClient mHttpClient = new AsyncHttpClient();

	// Values for username and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	private String userToken = "";
	
	private SimpleFacebook mSimpleFacebook;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// This is a redirect from the notifications bar, incase notifications are sent and the user isn't logged in.
		if (Provider.hasCurrentUser())
		{
			enterMySightings();
			return;
		}

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);
		mPasswordView = (EditText) findViewById(R.id.password);
		mFacebookLoginButton = (Button) findViewById(R.id.facebookLoginButton);

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		setupActionBar();
		setupFacebook();

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("username") && extras.containsKey("password"))
		{
			mUsernameView.setText(extras.getString("username"));
			mPasswordView.setText(extras.getString("password"));
			AttemptLogin();
		}

		// setup fb lifecycle helper
		fbUiLifecycleHelper = new UiLifecycleHelper(this, new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception)
			{
				executeFacebookStateChanged(session);
			}
		});

	}
	
	private void setupFacebook()
	{
		mSimpleFacebook = SimpleFacebook.getInstance(this);
	}

	public void AttemptFacebookLogin(View view)
	{
		mSimpleFacebook.login(new SimpleFacebook.OnLoginListener()
		{
			
			@Override
			public void onFail(String reason)
			{
				showLoginError("Facebook login failed: " + reason);
			}
			
			@Override
			public void onException(Throwable throwable)
			{
				throw new RedmapRuntimeException(throwable);
			}
			
			@Override
			public void onThinking()
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onNotAcceptingPermissions()
			{
				showLoginError("Facebook login failed: could not get permissions from Facebook.");
			}
			
			@Override
			public void onLogin()
			{
				executeFacebookStateChanged(SimpleFacebook.getOpenSession());
			}
		});
	}

	private void executeFacebookStateChanged(Session session)
	{
		if (session.isOpened())
		{
			setProgressMessage("Logging in with Facebook...");
			showProgress(true);
			String facebookToken = session.getAccessToken();

			UserFacebookLoginService svc = new UserFacebookLoginService(getApplicationContext(), facebookToken);
			svc.AddOnFinishedListener(new ILoginServiceAttemptComplete()
			{
				@Override
				public void Complete(boolean success, String message, String token)
				{
					if (success)
					{
						procressUserToken(token);
					}
					else
					{
						showLoginError(message);
						showProgress(false);
					}
				}
			});
			svc.Execute();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		fbUiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		isResumed = true;
		fbUiLifecycleHelper.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		isResumed = false;
		fbUiLifecycleHelper.onPause();
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

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		fbUiLifecycleHelper.onDestroy();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_log_in, menu);

		MenuItem actionDiscardMenuItem = menu.findItem(R.id.action_sign_in);
		actionDiscardMenuItem.setOnMenuItemClickListener(actionBarSignInOnClickHandler);

		return true;
	}

	public MenuItem.OnMenuItemClickListener actionBarSignInOnClickHandler = new MenuItem.OnMenuItemClickListener()
	{
		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			AttemptLogin();
			return true;
		}
	};

	public void CreateAccountOnClick(View view)
	{
		Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
		startActivity(intent);
	}

	public void OfflineLoginOnClick(View view)
	{
		enterMySightings();
	}

	public void AttemptLogin(MenuItem menuItem)
	{
		AttemptLogin();
	}

	public void AttemptLogin(View view)
	{
		AttemptLogin();
	}

	/**
	 * Attempts to sign in specified by the login form. If there are form errors (invalid email, missing fields, etc.), the errors are presented and no actual login attempt is made.
	 */
	public void AttemptLogin()
	{
		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword))
		{
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}
		else if (mPassword.length() < 4)
		{
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mUsername))
		{
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel)
		{
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else
		{
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);

			UserLoginService svc = new UserLoginService(getApplicationContext(), mUsername, mPassword);
			svc.AddOnFinishedListener(new ILoginServiceAttemptComplete()
			{
				@Override
				public void Complete(boolean success, String message, String token)
				{
					if (success)
					{
						procressUserToken(token);
					}
					else
					{
						showLoginError(message);
						showProgress(false);
					}
				}
			});
			svc.Execute();
		}
	}

	private void procressUserToken(final String token)
	{
		showProgress(true);
		setProgressMessage("Authenticated...");

		this.userToken = token;
		UserDetailService uds = new UserDetailService(getApplicationContext(), token);
		uds.AddOnFinishedListener(userDetailServiceHandler);
		uds.Execute();
	}

	private Callback<UserDetailPrim> userDetailServiceHandler = new Callback<UserDetailPrim>()
	{
		@Override
		public void Complete(UserDetailPrim userPrim)
		{

			try
			{
				IUserRepository repo = RedmapContext.getInstance().Provider.AppRepositories.UserRepository();

				User user = repo.getByPk(userPrim.id);

				if (user == null)
				{
					user = new User(userPrim, userToken);
				}
				else
				{
					user.Update(userPrim);
				}

				if (userPrim.region != null)
				{
					IRegionRepository repoRegion = RedmapContext.getInstance().Provider.AppRepositories.RegionRepository();
					Region region = repoRegion.getById(userPrim.region);
					user.Region = region;
				}

				repo.save(user);
				repo.logoutAllUsers();
				repo.loginUser(user);

				syncCurrentUserSightings();

				return;
			}
			catch (SQLException e)
			{
				throw new RedmapRuntimeException(e);
			}
		}
	};

	private void syncCurrentUserSightings()
	{
		setProgressMessage("Syncronising sightings...");

		final Callback<Void> syncFinishedCallback = new Callback<Void>()
		{

			@Override
			public void Complete(Void t)
			{
				enterMySightings();
			}
		};

		AsyncTask task = new AsyncTask()
		{
			@Override
			protected Object doInBackground(Object... params)
			{

				RedmapContext.getInstance().Provider.SyncCurrentUserSightings(userToken, syncFinishedCallback);
				return null;
			}
		};
		task.execute();
	}

	private void setProgressMessage(String message)
	{
		mLoginStatusMessageView.setText(message);
	}

	private void enterMySightings()
	{
		Intent intent = new Intent(getApplicationContext(), PersonalMapActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void showLoginError(final String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("Dismiss", null);
		builder.setTitle("Login error");
		builder.setMessage(message);
		builder.create().show();
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show)
	{

		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});
		}
		else
		{
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
