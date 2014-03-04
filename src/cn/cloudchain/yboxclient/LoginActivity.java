package cn.cloudchain.yboxclient;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends ActionBarActivity {
	private EditText accountEditText;
	private EditText passEditText;
	private CheckBox passRememberCheckbox;
	private CheckBox loginAutoCheckbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.login_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.layout_login);

		accountEditText = (EditText) this.findViewById(R.id.login_account_edit);
		passEditText = (EditText) this.findViewById(R.id.login_pass_edit);
		passRememberCheckbox = (CheckBox) this.findViewById(R.id.pass_remember);
		loginAutoCheckbox = (CheckBox) this.findViewById(R.id.login_auto);

		this.findViewById(R.id.login).setOnClickListener(
				new LoginClickListener());
		this.findViewById(R.id.register).setOnClickListener(
				new RegisterClickListener());
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

	private class LoginClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

		}

	}

	private class RegisterClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

		}

	}

}
