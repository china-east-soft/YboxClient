package cn.cloudchain.yboxclient;

import cn.cloudchain.yboxclient.fragment.LoginFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class LoginActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.login_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.base_container);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.container, new LoginFragment());
		ft.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
