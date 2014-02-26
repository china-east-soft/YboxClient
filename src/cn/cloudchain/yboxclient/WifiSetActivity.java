package cn.cloudchain.yboxclient;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class WifiSetActivity extends ActionBarActivity {
	final String TAG = WifiSetActivity.class.getSimpleName();
	public static final String BUNDLE_SSID = "ssid";
	public static final String BUNDLE_PASS = "pass";
	public static final String BUNDLE_AUTO_DISABLE = "auto_disable";
	public static final String BUNDLE_KEYMGMT = "keymagmt";
	public static final String BUNDLE_MAX_USERS = "max_users";

	private EditText ssidEditText;
	private EditText passEditText;
	private TextView passTextView;
	private CheckBox passShowCheckbox;
	private Spinner securitySpinner;
	private Spinner maxUserSpinner;
	private Spinner autoDisableSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.wlan_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_wifi);
		ssidEditText = (EditText) this.findViewById(R.id.ssid_edittext);
		passEditText = (EditText) this.findViewById(R.id.pass_edittext);
		passTextView = (TextView) this.findViewById(R.id.pass_text);
		passShowCheckbox = (CheckBox) this
				.findViewById(R.id.pass_show_checkbox);
		securitySpinner = (Spinner) this.findViewById(R.id.security_spinner);
		maxUserSpinner = (Spinner) this.findViewById(R.id.max_users_spinner);
		autoDisableSpinner = (Spinner) this
				.findViewById(R.id.auto_disable_spinner);

		securitySpinner
				.setOnItemSelectedListener(new SecurityItemSelectedListener());
		passShowCheckbox
				.setOnCheckedChangeListener(new PassShowCheckListener());
		initView(getIntent().getExtras());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView(Bundle bundle) {
		if (bundle == null)
			return;
		String ssid = bundle.getString(BUNDLE_SSID);
		String pass = bundle.getString(BUNDLE_PASS);
		int securityType = bundle.getInt(BUNDLE_KEYMGMT);
		int maxUsers = bundle.getInt(BUNDLE_MAX_USERS);
		int autodisable = bundle.getInt(BUNDLE_AUTO_DISABLE);
		ssidEditText.setText(ssid);
		showPassViews(securityType > 0);
		passEditText.setText(pass);
		securitySpinner.setSelection(securityType);
		maxUserSpinner.setSelection(maxUsers > 0 ? maxUsers - 1 : 0);
		autoDisableSpinner.setSelection(autodisable);
	}

	private void showPassViews(boolean show) {
		int visibility = show ? View.VISIBLE : View.GONE;
		passTextView.setVisibility(visibility);
		passEditText.setVisibility(visibility);
		passShowCheckbox.setVisibility(visibility);
	}

	private class SecurityItemSelectedListener implements
			OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			showPassViews(arg2 > 0);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}

	private class PassShowCheckListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				passEditText.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			} else {
				passEditText.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}
		}

	}
}
