package cn.cloudchain.yboxclient;

import cn.cloudchain.yboxclient.server.BroadcastService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;

public class BaseActionBarActivity extends ActionBarActivity {
	private boolean hasBind = false;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			hasBind = true;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		if (!hasBind) {
			Intent intent = new Intent(this, BroadcastService.class);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onPause() {
		if (hasBind) {
			unbindService(conn);
			hasBind = false;
		}
		super.onPause();
	}
}
