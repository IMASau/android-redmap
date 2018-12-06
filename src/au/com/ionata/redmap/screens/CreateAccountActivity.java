package au.com.ionata.redmap.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import au.com.ionata.redmap.R;
import au.com.ionata.redmap.RedmapContext;
import au.com.ionata.redmap.R.layout;
import au.com.ionata.redmap.R.menu;
import au.com.ionata.redmap.api.impl.CreateAccountService;
import au.com.ionata.redmap.api.interfaces.ICreateAccountServiceAttemptComplete;
import au.com.ionata.redmap.api.models.SightingOptionsPrim;
import au.com.ionata.redmap.models.Region;
import au.com.ionata.redmap.models.Sighting;
import au.com.ionata.redmap.models.Species;
import au.com.ionata.redmap.models.User;
import au.com.ionata.redmap.screens.gui.GuiTouchManager;
import au.com.ionata.redmap.utils.Callback;
import au.com.ionata.redmap.utils.sdk.EditTextFix;

public class CreateAccountActivity extends Activity {

	private final String REQUIRED_MSG = "Field required";
	
	protected GuiTouchManager gtm = new GuiTouchManager();
	
	protected SightingOptionsPrim mOptions;
	
	protected User user;
	
	private EditText firstInvalidEditText = null;
	
	private EditText mUsernameView;
	private EditText mPasswordView;
	private EditText mFirstNameView;
	private EditText mLastNameView;
	private EditText mEmailView;
	private Switch mJoinMailingListView;

	private Region region = null;
	
	private ProgressDialog serviceDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mOptions = RedmapContext.getInstance().Provider.getSettings().GetSightingOptions();
		setPressedColours();
		setupInputs();
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
	
	private void setupInputs(){

		mUsernameView = getEditText(R.id.username_layout);
		mPasswordView = getEditText(R.id.password_layout);
		mFirstNameView = getEditText(R.id.first_name_layout);
		mLastNameView = getEditText(R.id.last_name_layout);
		mEmailView = getEditText(R.id.email_layout);
		mJoinMailingListView = (Switch)findViewById(R.id.join_mailing_list_switch);
		
		user = new User();

		EditText username = getEditText(R.id.username_layout);
		username.requestFocus();
	}
	
	private void setPressedColours() {
		List<RelativeLayout> layouts = new ArrayList<RelativeLayout>();
		layouts.add((RelativeLayout) findViewById(R.id.region_layout));

		for (final RelativeLayout layout : layouts) {
			View name = (View) layout.findViewById(R.id.name);
			gtm.setPressedColours((TextView) name);

			View value_label = (View) layout.findViewById(R.id.value_label);
			Class klass = value_label.getClass();
			if (klass == TextView.class) {
				gtm.setPressedColours((TextView) value_label);
			} else if (klass == EditTextFix.class || klass == EditText.class) {
				EditText editText = (EditText) value_label;
				gtm.setPressedColours(editText);

				// Override edittext pressed state to set the parent layout as
				// also pressed (duplicate parent state doesn't work for
				// edittext)
				editText.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						switch (event.getAction()) {
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
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_create_account, menu);
		MenuItem actionDiscardMenuItem = menu.findItem(R.id.action_submit);
		actionDiscardMenuItem.setOnMenuItemClickListener(actionBarSubmitOnClickHandler);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setRegionText() {
		EditText label = getEditText(R.id.region_layout);
		if (region == null) {
			label.setText(null);
			return;
		}

		
		String text = region.Description;

		label.setText(text);
	}

	public void RegionOnClick(View view) {
		CharSequence[] cs = new CharSequence[mOptions.region.size()];
		for (int i = 0; i < mOptions.region.size(); i++) {
			cs[i] = mOptions.region.get(i).fields.description;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Select your region").setItems(cs, regionOnClickHandler);
		builder.create().show();
	}
	
	private DialogInterface.OnClickListener regionOnClickHandler = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			int pk = mOptions.region.get(which).pk;
			try {
	            region = RedmapContext.getInstance().Provider.AppRepositories.RegionRepository().getById(pk);
            } catch (SQLException e) {
	            e.printStackTrace();
            }
			
			setRegionText();
			validateRegion();
		}
	};
	
	private EditText getEditText(int id) {
		RelativeLayout layout = (RelativeLayout) findViewById(id);
		EditText field = (EditText) layout.findViewById(R.id.value_label);
		return field;
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
	
	public void ActionBarSubmitOnClick(){
		if (Validate()) {
		    Submit();
		}
	}
	
	private void Submit(){
		serviceDialog = ProgressDialog.show(this, "Create account", "Submitting your account information", true, false);
		
		user = new User();
        user.Username = mUsernameView.getText().toString();
        user.NewPassword = mPasswordView.getText().toString();
        user.FirstName = mFirstNameView.getText().toString(); 
        user.LastName = mLastNameView.getText().toString();
        user.Email = mEmailView.getText().toString();
        user.JoinMailingList = mJoinMailingListView.isActivated();
        user.Region = region;

        CreateAccountService cas = new CreateAccountService(getApplicationContext(), user);
        cas.AddOnFinishedListener(submitCompleteHandler);
        cas.Execute();
	}
	
	private void moveToLoginActivity(String username, String password){
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.putExtra("username", username);
		intent.putExtra("password", password);
		
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		startActivity(intent);
		finish();
	}
	
	private void clearForm(){
		List<TextView> fields = new ArrayList<TextView>(){{
			add(mUsernameView);
			add(mPasswordView);
			add(mFirstNameView);
			add(mLastNameView);
			add(mEmailView);
		}};
		
		for (TextView field : fields){
			field.setText(null);
			field.setError(null);
		}

        mJoinMailingListView.setChecked(true);
	}

	private ICreateAccountServiceAttemptComplete submitCompleteHandler = new ICreateAccountServiceAttemptComplete() {
        @Override
        public void Complete(boolean success, String message) {
        	
        	if (success){
        		moveToLoginActivity(user.Username, user.NewPassword);
        		clearForm();
        	}

        	if (serviceDialog.isShowing()){
        		serviceDialog.dismiss();
        	}

        	if (!success){
        		showCreateAccountError(message);
        	}
        }
    };
	
	private void showCreateAccountError(final String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("Dismiss", null);
		builder.setTitle("Create account error");
		builder.setMessage(message);
		builder.create().show();
	}
    
	private boolean validateRegion() {
		boolean isValid = true;
		EditText field = getEditText(R.id.region_layout);
		if (region == null) {
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (isValid) {
			field.setError(null);
		}

		return isValid;
	};
	
	private void setFirstInvalidEditText(EditText editText) {
		if (this.firstInvalidEditText == null) {
			this.firstInvalidEditText = editText;
		}
	}
	
	public boolean Validate() {

		boolean isValid = true;
		this.firstInvalidEditText = null;

		
		if (!validateUsername())
			isValid = false;
		if (!validatePassword())
			isValid = false;
		if (!validateFirstName())
			isValid = false;
		if (!validateLastName())
			isValid = false;
		if (!validateEmail())
			isValid = false;
		if (!validateRegion())
			isValid = false;

		if (!isValid && this.firstInvalidEditText != null) {
			this.firstInvalidEditText.requestFocus();
		}

		return isValid;
	};
	
	private boolean validateUsername() {
		boolean isValid = true;
		EditText field = mUsernameView;
		String value = field.getText().toString();
		
		if (value.isEmpty()) {
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (value.length() < 3 || value.length() > 30) {
			field.setError("Between 3 and 30 characters");
			setFirstInvalidEditText(field);
			return false;
		}
		
		if (!value.matches("^[\\w.@+-]+$")) {
			field.setError("Invalid characters");
			setFirstInvalidEditText(field);
			return false;
		}
		
		if (isValid) {
			field.setError(null);
		}

		return isValid;
	};
	
	private boolean validatePassword() {
		boolean isValid = true;
		EditText field = mPasswordView;
		String value = field.getText().toString();
		
		if (value.isEmpty()) {
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (value.length() < 6) {
			field.setError("Minimum of 6 characters");
			setFirstInvalidEditText(field);
			return false;
		}
		
		if (isValid) {
			field.setError(null);
		}

		return isValid;
	};
	
	
	private boolean validateFirstName() {
		boolean isValid = true;
		EditText field = mFirstNameView;
		String value = field.getText().toString();
		
		if (value.isEmpty()) {
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (value.length() > 30) {
			field.setError("Less than 30 characters");
			setFirstInvalidEditText(field);
			return false;
		}
		
		if (isValid) {
			field.setError(null);
		}

		return isValid;
	};
	
	private boolean validateLastName() {
		boolean isValid = true;
		EditText field = mLastNameView;
		String value = field.getText().toString();
		
		if (value.isEmpty()) {
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (value.length() > 30) {
			field.setError("Less than 30 characters");
			setFirstInvalidEditText(field);
			return false;
		}
		
		if (isValid) {
			field.setError(null);
		}

		return isValid;
	};
	
	
	private boolean validateEmail() {
		boolean isValid = true;
		EditText field = mEmailView;
		String value = field.getText().toString();
		
		if (value.isEmpty()) {
			field.setError(REQUIRED_MSG);
			setFirstInvalidEditText(field);
			return false;
		}

		if (value.length() > 75) {
			field.setError("Less than 75 characters");
			setFirstInvalidEditText(field);
			return false;
		}
		
		if (isValid) {
			field.setError(null);
		}

		return isValid;
	};
}
