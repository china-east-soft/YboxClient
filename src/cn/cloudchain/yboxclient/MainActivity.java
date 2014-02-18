package cn.cloudchain.yboxclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import cn.cloudchain.yboxclient.dialog.WifiSetDialogFragment;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;
import cn.cloudchain.yboxclient.server.BroadcastService;

public class MainActivity extends FragmentActivity implements
		OnCheckedChangeListener, OnClickListener {
	private final String TAG = MainActivity.class.getSimpleName();
	private ToggleButton mobileDataButton;
	private TextView wifiModeText;
	private Button wifiSet;
	private Button shutdown;
	private Button wifiUserList;

	private MyHandler handler = new MyHandler(this);
	private ApStatusReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mobileDataButton = (ToggleButton) findViewById(R.id.mobile_data_button);
		wifiModeText = (TextView) findViewById(R.id.wifi_mode);
		wifiSet = (Button) findViewById(R.id.hotspot_set);
		wifiUserList = (Button) findViewById(R.id.hotspot_list);
		shutdown = (Button) findViewById(R.id.shutdown);

		mobileDataButton.setOnCheckedChangeListener(this);
		wifiSet.setOnClickListener(this);
		wifiUserList.setOnClickListener(this);
		shutdown.setOnClickListener(this);

		wifiModeText.setText(MyApplication.getInstance().wifiMode);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (receiver == null) {
			receiver = new ApStatusReceiver(new MyApStatusHandler(this));
		}
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
				new IntentFilter(ApStatusReceiver.ACTION_WIFI_MODE_CHANGE));
		if (!hasBind) {
			Intent intent = new Intent(this, BroadcastService.class);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
		}
	}

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
	protected void onStop() {
		if (receiver != null) {
			LocalBroadcastManager.getInstance(this)
					.unregisterReceiver(receiver);
			receiver = null;
		}
		if (hasBind) {
			unbindService(conn);
		}
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hotspot_set:
			handleWifiSet();
			break;
		case R.id.hotspot_list:
			handleDeviceList();
			break;
		case R.id.shutdown:
			handleShutdown();
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.mobile_data_button:
			handleMobileData(isChecked);
			break;
		}
	}

	private void handleMobileData(final boolean enable) {
		Log.i(TAG, "handle mobile data");
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().setMobileDataEnable(
						enable);
				Log.i(TAG, "result = " + result);
			}
		}).start();
		;
	}

	private void handleShutdown() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().shutdown(false);
				Log.i(TAG, "result = " + result);
			}
		}).start();
	}

	private void handleDeviceList() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getDevices();
				Message msg = handler
						.obtainMessage(MyHandler.MSG_DEVICES_COMPLETE);
				msg.obj = result;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void handleWifiSet() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getWifiInfo();
				Message msg = handler
						.obtainMessage(MyHandler.WIFI_INFO_COMPLETE);
				msg.obj = result;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private static class MyHandler extends WeakHandler<MainActivity> {
		private static final int MSG_DEVICES_COMPLETE = 1;
		private static final int WIFI_INFO_COMPLETE = 2;

		public MyHandler(MainActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case MSG_DEVICES_COMPLETE:
				Intent intent = new Intent();
				intent.setClass(getOwner(), HotspotListActivity.class);
				intent.putExtra("content", (String) msg.obj);
				getOwner().startActivity(intent);
				break;
			case WIFI_INFO_COMPLETE:
				DialogFragment fragment = WifiSetDialogFragment
						.newInstance((String) msg.obj);
				fragment.show(getOwner().getSupportFragmentManager(), "");
				break;
			}
		}

	}

	private static class MyApStatusHandler extends
			ApStatusHandler<MainActivity> {

		public MyApStatusHandler(MainActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case WIFI_MODE_CHANGE:
				getOwner().wifiModeText
						.setText(MyApplication.getInstance().wifiMode);
				break;
			}
		}

	}

}
