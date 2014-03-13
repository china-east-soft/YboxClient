package cn.cloudchain.yboxclient;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import cn.cloudchain.yboxclient.fragment.WifiSearchFragment;

public class WifiSearchActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_container);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.container, new WifiSearchFragment(), null);
		ft.commit();
	}

}
