package cn.cloudchain.yboxclient;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import cn.cloudchain.yboxclient.dialog.EtherSetDialogFragment;
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
	private Button mobileInfo;
	private Button etherInfo;
	private Button etherSet;
	private EditText wifiAutoDisableTime;

	private MyHandler handler = new MyHandler(this);
	private MyApStatusHandler apHandler = new MyApStatusHandler(this);
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
		mobileInfo = (Button) findViewById(R.id.mobile_info);
		wifiAutoDisableTime = (EditText) findViewById(R.id.wifi_auto_disable_time);

		mobileDataButton.setOnCheckedChangeListener(this);
		wifiSet.setOnClickListener(this);
		wifiUserList.setOnClickListener(this);
		shutdown.setOnClickListener(this);
		mobileInfo.setOnClickListener(this);

		etherInfo = (Button) findViewById(R.id.ether_info);
		etherInfo.setOnClickListener(this);
		etherSet = (Button) findViewById(R.id.ether_set);
		etherSet.setOnClickListener(this);

		findViewById(R.id.restart_wifi).setOnClickListener(this);
		findViewById(R.id.wifi_auto_disable_get).setOnClickListener(this);
		findViewById(R.id.wifi_auto_disable_set).setOnClickListener(this);

		apHandler.sendEmptyMessage(ApStatusHandler.WIFI_MODE_CHANGE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (receiver == null) {
			receiver = new ApStatusReceiver(apHandler);
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
			hasBind = false;
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
		case R.id.mobile_info:
			handleMobileInfo();
			break;
		case R.id.ether_info:
			handleEtherInfo();
			break;
		case R.id.ether_set:
			handleEtherSet();
			break;
		case R.id.restart_wifi:
			handleRestartWifi();
			break;
		case R.id.wifi_auto_disable_get:
			handleWifiAutoDisableGet();
			break;
		case R.id.wifi_auto_disable_set:
			handleWifiAutoDisableSet();
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
	}

	private void handleShutdown() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().shutdown(false);
				Log.i(TAG, "result = " + result);
				try {
					JSONObject obj = new JSONObject(result);
					boolean success = obj.optBoolean("result", false);
					Message msg = handler
							.obtainMessage(MyHandler.SHUTDOWN_COMPLETE);
					msg.arg1 = success ? 1 : 0;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
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

	private void handleMobileInfo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result1 = SetHelper.getInstance().getBattery();
				String result2 = SetHelper.getInstance().getSignalQuality();
				Bundle bundle = new Bundle();
				try {
					JSONObject obj1 = new JSONObject(result1);
					if (obj1.optBoolean("result")) {
						bundle.putInt("battery", obj1.optInt("remain"));
					}
					JSONObject obj2 = new JSONObject(result2);
					if (obj2.optBoolean("result")) {
						bundle.putInt("strength", obj2.optInt("strength"));
					}

					Message msg = handler
							.obtainMessage(MyHandler.MOBILE_INFO_COMPLETE);
					msg.setData(bundle);
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	private void handleEtherInfo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = handler.obtainMessage(MyHandler.REQUEST_COMPLETE);
				msg.obj = SetHelper.getInstance().getEthernetInfo();
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void handleEtherSet() {
		EtherSetDialogFragment fragment = new EtherSetDialogFragment();
		fragment.show(getSupportFragmentManager(), TAG);
	}

	private void handleRestartWifi() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SetHelper.getInstance().restartWifi();
			}
		}).start();
	}

	private void handleWifiAutoDisableSet() {
		final String timeStr = wifiAutoDisableTime.getText().toString().trim();
		if (TextUtils.isEmpty(timeStr)) {
			Toast.makeText(this, "请输入时长", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				int time = Integer.parseInt(timeStr);
				Message msg = handler.obtainMessage(MyHandler.REQUEST_COMPLETE);
				msg.obj = SetHelper.getInstance().setWifiAutoDisable(time);
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void handleWifiAutoDisableGet() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = handler
						.obtainMessage(MyHandler.WIFI_AUTO_DISABLE_GET_COMPLETE);
				msg.obj = SetHelper.getInstance().getWifiAutoDisable();
				handler.sendMessage(msg);
			}
		}).start();
	}

	private static class MyHandler extends WeakHandler<MainActivity> {
		private static final int MSG_DEVICES_COMPLETE = 1;
		private static final int WIFI_INFO_COMPLETE = 2;
		private static final int SHUTDOWN_COMPLETE = 3;
		private static final int MOBILE_INFO_COMPLETE = 4;
		private static final int REQUEST_COMPLETE = 5;
		private static final int WIFI_AUTO_DISABLE_GET_COMPLETE = 6;

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
//				Intent intent = new Intent();
//				intent.setClass(getOwner(), HotspotListActivity.class);
//				intent.putExtra("content", (String) msg.obj);
//				getOwner().startActivity(intent);
				break;
			case WIFI_INFO_COMPLETE:
				DialogFragment fragment = WifiSetDialogFragment
						.newInstance((String) msg.obj);
				fragment.show(getOwner().getSupportFragmentManager(), "");
				break;
			case SHUTDOWN_COMPLETE:
				Toast.makeText(getOwner(), msg.arg1 > 0 ? "请求关机成功" : "请求关机失败",
						Toast.LENGTH_SHORT).show();
				break;
			case MOBILE_INFO_COMPLETE:
				Bundle data = msg.getData();
				int battery = data.getInt("battery", -1);
				int strength = data.getInt("strength", -1);
				Toast.makeText(getOwner(),
						String.format("电量：%d; 信号强度：%dDbm", battery, strength),
						Toast.LENGTH_SHORT).show();
				break;
			case REQUEST_COMPLETE:
				Toast.makeText(getOwner(), (String) msg.obj, Toast.LENGTH_SHORT)
						.show();
				break;
			case WIFI_AUTO_DISABLE_GET_COMPLETE: {
				String result = (String) msg.obj;
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.optBoolean("result")) {
						int time = obj.optInt("time");
						getOwner().wifiAutoDisableTime.setText(String
								.valueOf(time));
					} else {
						Toast.makeText(getOwner(), "热点自动关闭时间，请求失败",
								Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
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
				String mode = MyApplication.getInstance().wifiMode;
				getOwner().wifiModeText.setText(mode);
				getOwner().setEthernetEnable(mode.equals("wlan"));
				break;
			}
		}

	}

	private void setEthernetEnable(boolean enable) {
		etherSet.setEnabled(true);
		etherInfo.setEnabled(true);
	}
}
