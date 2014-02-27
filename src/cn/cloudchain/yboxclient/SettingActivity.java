package cn.cloudchain.yboxclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;
import cn.cloudchain.yboxclient.task.DevicesJumpTask;
import cn.cloudchain.yboxclient.task.WifiInfoJumpTask;
import cn.cloudchain.yboxclient.task.WlanInfoJumpTask;

public class SettingActivity extends BaseActionBarActivity implements
		OnClickListener {
	// private TextView updateYboxStatus;
	// private TextView updateAppStatus;
	private TextView deviceNums;

	private ApStatusReceiver statusReceiver;
	private MyHandler handler = new MyHandler(this);
	private ReceiverHandler receiverHandler = new ReceiverHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setTitle(R.string.setting_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_setting);
		this.findViewById(R.id.setting_wlan).setOnClickListener(this);
		this.findViewById(R.id.setting_wifi).setOnClickListener(this);
		this.findViewById(R.id.setting_devices).setOnClickListener(this);
		this.findViewById(R.id.setting_update_ybox).setOnClickListener(this);
		this.findViewById(R.id.setting_update_app).setOnClickListener(this);

		// updateYboxStatus = (TextView) this
		// .findViewById(R.id.setting_update_ybox_latest);
		// updateAppStatus = (TextView) this
		// .findViewById(R.id.setting_update_app_latest);
		deviceNums = (TextView) this.findViewById(R.id.setting_devices_num);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();
		handler.sendEmptyMessage(MyHandler.DEVICE_NUM_GET);
	}

	@Override
	protected void onStop() {
		unregisterReceiver();
		super.onStop();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_wlan:
			if (MyApplication.getInstance().connType == 1) {
				jumpToWlan();
			} else {
				Util.toaster(R.string.ethernet_not_conn_reminder);
			}
			break;
		case R.id.setting_wifi:
			jumpToWifi();
			break;
		case R.id.setting_devices:
			jumpToDevices();
			break;
		}
	}

	private void jumpToWlan() {
		WlanInfoJumpTask task = new WlanInfoJumpTask(this);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				null, true);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), TaskDialogFragment.TAG);
	}

	private void jumpToWifi() {
		WifiInfoJumpTask task = new WifiInfoJumpTask(this);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				null, true);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), TaskDialogFragment.TAG);
	}

	private void jumpToDevices() {
		DevicesJumpTask task = new DevicesJumpTask(this);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				null, true);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), TaskDialogFragment.TAG);
	}

	private static class MyHandler extends WeakHandler<SettingActivity> {
		// private static final int YBOX_UPDATE_CHECK = 0;
		// private static final int YBOX_UPDATE_COMPLETE = 1;
		// private static final int APP_UPDATE_CHECK = 2;
		// private static final int APP_UPDATE_COMPLETE = 3;
		private static final int DEVICE_NUM_GET = 4;
		private static final int DEVICE_NUM_GET_COMPLETE = 5;

		public MyHandler(SettingActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case DEVICE_NUM_GET:
				getOwner().handleDeviceNumGet();
				break;
			case DEVICE_NUM_GET_COMPLETE:
				getOwner().deviceNums.setText(getOwner().getString(
						R.string.setting_devices_num, msg.arg1));
				break;
			}
		}
	}

	private void handleDeviceNumGet() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getDevices();
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.optBoolean("result")) {
						JSONArray array = obj.optJSONArray("devices");
						int count = array.length();
						Message msg = handler
								.obtainMessage(MyHandler.DEVICE_NUM_GET_COMPLETE);
						msg.arg1 = count;
						handler.sendMessage(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private static class ReceiverHandler extends
			ApStatusHandler<SettingActivity> {

		public ReceiverHandler(SettingActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case HOTSPOT_CLIENT_CHANGE:
				getOwner().handler.sendEmptyMessage(MyHandler.DEVICE_NUM_GET);
				break;
			}
		}
	}

	private void registerReceiver() {
		if (statusReceiver == null) {
			statusReceiver = new ApStatusReceiver(receiverHandler);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApStatusReceiver.ACTION_WIFI_CLIENTS_CHANGE);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				statusReceiver, filter);
	}

	private void unregisterReceiver() {
		if (statusReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					statusReceiver);
			statusReceiver = null;
		}
	}

}
