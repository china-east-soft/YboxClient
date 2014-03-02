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
import cn.cloudchain.yboxclient.dialog.AutoSleepDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;
import cn.cloudchain.yboxclient.task.DevicesJumpTask;
import cn.cloudchain.yboxclient.task.WifiInfoJumpTask;
import cn.cloudchain.yboxclient.task.WlanInfoJumpTask;
import cn.cloudchain.yboxcommon.bean.Types;

public class SettingActivity extends BaseActionBarActivity implements
		OnClickListener {
	private TextView updateYboxStatus;
	private TextView updateAppStatus;
	private TextView deviceNums;
	private TextView netType;
	private TextView autoSleepType;

	private int autoSleepIndex;

	private ApStatusReceiver statusReceiver;
	private MyHandler handler = new MyHandler(this);
	private ReceiverHandler receiverHandler = new ReceiverHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setTitle(R.string.setting_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_setting);
		this.findViewById(R.id.setting_net).setOnClickListener(this);
		this.findViewById(R.id.setting_wifi).setOnClickListener(this);
		this.findViewById(R.id.setting_devices).setOnClickListener(this);
		this.findViewById(R.id.setting_update_ybox).setOnClickListener(this);
		this.findViewById(R.id.setting_update_app).setOnClickListener(this);
		this.findViewById(R.id.setting_auto_sleep).setOnClickListener(this);

		updateYboxStatus = (TextView) this
				.findViewById(R.id.setting_update_ybox_latest);
		updateAppStatus = (TextView) this
				.findViewById(R.id.setting_update_app_latest);
		autoSleepType = (TextView) this
				.findViewById(R.id.setting_auto_sleep_type);
		deviceNums = (TextView) this.findViewById(R.id.setting_devices_num);
		netType = (TextView) this.findViewById(R.id.setting_net_type);
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
		handler.sendEmptyMessage(MyHandler.AUTO_SLEEP_GET);
		refreshNetType();
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
		case R.id.setting_net:
			int connType = MyApplication.getInstance().connType;
			if (connType == Types.CONN_TYPE_ETHERNET
					|| connType == Types.CONN_TYPE_MOBILE_DATA_ON) {
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
		case R.id.setting_auto_sleep:
			showAutoSleepDialog();
			break;
		case R.id.setting_update_app:
			handler.sendEmptyMessage(MyHandler.MIDDLE_APK_UPDATE);
			break;
		case R.id.setting_update_ybox:
			handler.sendEmptyMessage(MyHandler.ROOT_IMAGE_UPDATE);
			break;
		}
	}

	private void refreshNetType() {
		int connType = MyApplication.getInstance().connType;
		switch (connType) {
		case Types.CONN_TYPE_ETHERNET:
			netType.setText(R.string.conn_type_ethernet);
			break;
		case Types.CONN_TYPE_MOBILE_DATA_ON:
			netType.setText(R.string.conn_type_3g);
			break;
		default:
			netType.setText("");
			break;
		}
	}

	private void showAutoSleepDialog() {
		AutoSleepDialogFragment fragment = AutoSleepDialogFragment
				.newInstance(autoSleepIndex);
		fragment.show(getSupportFragmentManager(), null);
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
		private static final int AUTO_SLEEP_GET = 2;
		private static final int AUTO_SLEEP_GET_COMPLETE = 3;
		private static final int DEVICE_NUM_GET = 4;
		private static final int DEVICE_NUM_GET_COMPLETE = 5;
		private static final int ROOT_IMAGE_UPDATE = 6;
		private static final int MIDDLE_APK_UPDATE = 7;

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
			case ROOT_IMAGE_UPDATE:
				getOwner().handleRootImageUpdate();
				break;
			case MIDDLE_APK_UPDATE:
				getOwner().handleMiddleApkUpdate();
				break;
			case AUTO_SLEEP_GET:
				getOwner().handleAutoSleepGet();
				break;
			case AUTO_SLEEP_GET_COMPLETE:
				int index = msg.arg1;
				getOwner().autoSleepIndex = index;
				String[] autoSleepTypes = getOwner().getResources()
						.getStringArray(R.array.auto_sleep_types);
				if (index >= 0 && autoSleepTypes.length > index) {
					getOwner().autoSleepType.setText(autoSleepTypes[index]);
				}
				break;
			}
		}
	}

	private void handleRootImageUpdate() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SetHelper.getInstance().updateRootImage("");
			}
		}).start();
	}

	private void handleMiddleApkUpdate() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SetHelper.getInstance().updateMiddleApk("");
			}
		}).start();
	}

	private void handleAutoSleepGet() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getAutoSleepType();
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.optBoolean("result")) {
						Message msg = handler
								.obtainMessage(MyHandler.AUTO_SLEEP_GET_COMPLETE);
						msg.arg1 = obj.optInt("type");
						handler.sendMessage(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void handleDeviceNumGet() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getDevices(
						Types.DEVICES_UNBLOCK);
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
			case WIFI_MODE_CHANGE:
				getOwner().refreshNetType();
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
		filter.addAction(ApStatusReceiver.ACTION_WIFI_MODE_CHANGE);
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
