package cn.cloudchain.yboxclient;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import cn.cloudchain.yboxclient.fragment.CloudBindFragment;

public class CloudActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.cloud_title);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.base_container);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.container, new CloudBindFragment());
		ft.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
