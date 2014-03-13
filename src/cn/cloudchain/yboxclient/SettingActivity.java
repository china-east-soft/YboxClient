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
import cn.cloudchain.yboxclient.helper.UpdateUtil;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;
import cn.cloudchain.yboxclient.task.AppUpdateCheckTask;
import cn.cloudchain.yboxclient.task.DevicesJumpTask;
import cn.cloudchain.yboxclient.task.TvModeJumpTask;
import cn.cloudchain.yboxclient.task.WifiInfoJumpTask;
import cn.cloudchain.yboxclient.task.WlanInfoJumpTask;
import cn.cloudchain.yboxclient.task.YboxUpdateCheckTask;
import cn.cloudchain.yboxclient.utils.Util;
import cn.cloudchain.yboxclient.views.GridItemSetting;
import cn.cloudchain.yboxcommon.bean.Types;

public class SettingActivity extends BaseActionBarActivity implements
		OnClickListener {
	private final String TAG = SettingActivity.class.getSimpleName();
	private TextView autoSleepType;

	private GridItemSetting wlanGrid;
	private GridItemSetting clientsGrid;
	private GridItemSetting yboxUpdateGrid;
	private GridItemSetting appUpdateGrid;

	private int autoSleepIndex;
	private ApStatusReceiver statusReceiver;
	private MyHandler handler = new MyHandler(this);
	private ReceiverHandler receiverHandler = new ReceiverHandler(this);

	/**
	 * 用户手动点击更新
	 */
	private final int CHECK_UPDATE_HAND = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setTitle(R.string.setting_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_setting);
		wlanGrid = (GridItemSetting) this.findViewById(R.id.setting_wlan);
		clientsGrid = (GridItemSetting) this.findViewById(R.id.setting_devices);
		yboxUpdateGrid = (GridItemSetting) this
				.findViewById(R.id.setting_update_ybox);
		appUpdateGrid = (GridItemSetting) this
				.findViewById(R.id.setting_update_app);

		wlanGrid.setOnClickListener(this);
		clientsGrid.setOnClickListener(this);
		yboxUpdateGrid.setOnClickListener(this);
		appUpdateGrid.setOnClickListener(this);

		this.findViewById(R.id.setting_wifi).setOnClickListener(this);
		this.findViewById(R.id.setting_tvmode).setOnClickListener(this);
		// this.findViewById(R.id.setting_auto_sleep).setOnClickListener(this);

		// autoSleepType = (TextView) this
		// .findViewById(R.id.setting_auto_sleep_type);
		// deviceNums = (TextView) this.findViewById(R.id.setting_devices_num);
		// netType = (TextView) this.findViewById(R.id.setting_net_type);
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
		handler.sendEmptyMessage(MyHandler.YBOX_UPDATE_CHECK);
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
		case R.id.setting_wlan:
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
		// case R.id.setting_auto_sleep:
		// showAutoSleepDialog();
		// break;
		case R.id.setting_update_ybox: {
			Message msg = handler.obtainMessage(MyHandler.YBOX_UPDATE_CHECK);
			msg.arg1 = CHECK_UPDATE_HAND;
			handler.sendMessage(msg);
			break;
		}
		case R.id.setting_update_app: {
			Message msg = handler.obtainMessage(MyHandler.APP_UPDATE_CHECK);
			msg.arg1 = CHECK_UPDATE_HAND;
			handler.sendMessage(msg);
			break;
		}
		case R.id.setting_tvmode:
			jumpToTvMode();
			break;
		}
	}

	private void refreshNetType() {
		int connType = MyApplication.getInstance().connType;
		switch (connType) {
		case Types.CONN_TYPE_ETHERNET:
			wlanGrid.setSubDes(getString(R.string.conn_type_ethernet));
			break;
		case Types.CONN_TYPE_MOBILE_DATA_ON:
			wlanGrid.setSubDes(getString(R.string.conn_type_3g));
			break;
		default:
			wlanGrid.setSubDes("");
			break;
		}
	}

	private void showAutoSleepDialog() {
		AutoSleepDialogFragment fragment = AutoSleepDialogFragment
				.newInstance(autoSleepIndex);
		fragment.show(getSupportFragmentManager(), null);
	}

	private void jumpToTvMode() {
		TvModeJumpTask task = new TvModeJumpTask(this);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				null, true);
		fragment.setTask(task);
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
		private static final int AUTO_SLEEP_GET = 2;
		private static final int AUTO_SLEEP_GET_COMPLETE = 3;
		private static final int DEVICE_NUM_GET = 4;
		private static final int DEVICE_NUM_GET_COMPLETE = 5;
		private static final int YBOX_UPDATE_CHECK = 6;
		private static final int APP_UPDATE_CHECK = 7;

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
				getOwner().clientsGrid.setSubDes(String.valueOf(msg.arg1));
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
			case APP_UPDATE_CHECK:
				getOwner().appUpdateCheck(
						msg.arg1 == getOwner().CHECK_UPDATE_HAND);
				break;
			case AppUpdateCheckTask.APP_CURRENT_VERSION_INVALID:
				getOwner().appUpdateGrid.setSubDes("");
				getOwner().appUpdateGrid.setTag(null);
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					Util.toaster(R.string.update_invalid_version);
				}
				break;
			case AppUpdateCheckTask.APP_HAS_UPDATE: {
				Bundle data = msg.getData();
				getOwner().appUpdateGrid.setSubDes("有更新");
				getOwner().appUpdateGrid.setTag(data);
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					UpdateUtil updateUtil = new UpdateUtil(getOwner());
					updateUtil.handleAppUpdate(data);
				}
				break;
			}
			case AppUpdateCheckTask.APP_NO_NEED_UPDATE:
				getOwner().appUpdateGrid.setSubDes("已是最新");
				getOwner().appUpdateGrid.setTag(msg.getData());
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					Util.toaster(R.string.no_need_update);
				}
				break;
			case AppUpdateCheckTask.APP_UPDATE_INFO_FAIL:
				getOwner().appUpdateGrid.setSubDes("");
				getOwner().appUpdateGrid.setTag(null);
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					Util.toaster(R.string.update_info_get_fail);
				}
				break;
			case YBOX_UPDATE_CHECK:
				getOwner().yboxUpdateCheck(
						msg.arg1 == getOwner().CHECK_UPDATE_HAND);
				break;
			case YboxUpdateCheckTask.YBOX_UPDATE_CHECK_SOCKET_FAIL:
				getOwner().yboxUpdateGrid.setSubDes("");
				getOwner().yboxUpdateGrid.setTag(null);
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					Util.toaster("请求终端失败", null);
				}
				break;
			case YboxUpdateCheckTask.YBOX_UPDATE_CHECK_HTTP_FAIL:
				getOwner().yboxUpdateGrid.setSubDes("");
				getOwner().yboxUpdateGrid.setTag(null);
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					Util.toaster(R.string.update_info_get_fail);
				}
				break;
			case YboxUpdateCheckTask.YBOX_CURRENT_VERSION_INVALID:
				getOwner().yboxUpdateGrid.setSubDes("");
				getOwner().yboxUpdateGrid.setTag(null);
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					Util.toaster(R.string.update_invalid_version);
				}
				break;
			case YboxUpdateCheckTask.YBOX_NO_NEED_UPDATE:
				getOwner().yboxUpdateGrid.setSubDes("已是最新");
				getOwner().yboxUpdateGrid.setTag(msg.getData());
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					Util.toaster(R.string.no_need_update);
				}
				break;
			case YboxUpdateCheckTask.YBOX_HAS_UPDATE: {
				Bundle data = msg.getData();
				getOwner().yboxUpdateGrid.setSubDes("有更新");
				getOwner().yboxUpdateGrid.setTag(data);
				if (msg.arg1 == getOwner().CHECK_UPDATE_HAND) {
					UpdateUtil updateUtil = new UpdateUtil(getOwner());
					updateUtil.handleYboxUpdate(data);
				}
				break;
			}
			}
		}
	}

	/**
	 * 检测APP升级
	 * 
	 * @param clickByUser
	 */
	private void appUpdateCheck(final boolean clickByUser) {
		Bundle data = (Bundle) appUpdateGrid.getTag();
		// 如果已经检测过更新了，则直接返回结果
		if (data != null) {
			Message msg = handler
					.obtainMessage(data.isEmpty() ? AppUpdateCheckTask.APP_NO_NEED_UPDATE
							: AppUpdateCheckTask.APP_HAS_UPDATE);
			if (clickByUser) {
				msg.arg1 = CHECK_UPDATE_HAND;
			}
			msg.setData(data);
			handler.sendMessage(msg);
			return;
		}

		AppUpdateCheckTask task = new AppUpdateCheckTask(this, clickByUser,
				handler);
		if (clickByUser) {
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment("正在检查更新...", true);
			fragment.setTask(task);
			fragment.show(getSupportFragmentManager(), null);
		} else {
			task.execute();
		}
	}

	/**
	 * 检测YBOX升级
	 * 
	 * @param clickByUser
	 *            true时为用户点击升级，显示升级对话框
	 */
	private void yboxUpdateCheck(final boolean clickByUser) {
		Bundle data = (Bundle) yboxUpdateGrid.getTag();
		if (data != null) {
			Message msg = handler
					.obtainMessage(data.isEmpty() ? YboxUpdateCheckTask.YBOX_NO_NEED_UPDATE
							: YboxUpdateCheckTask.YBOX_HAS_UPDATE);
			if (clickByUser) {
				msg.arg1 = CHECK_UPDATE_HAND;
			}
			msg.setData(data);
			handler.sendMessage(msg);
			return;
		}

		YboxUpdateCheckTask task = new YboxUpdateCheckTask(this, clickByUser,
				handler);
		if (clickByUser) {
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment("正在检查更新...", true);
			fragment.setTask(task);
			fragment.show(getSupportFragmentManager(), null);
		} else {
			task.execute();
		}
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
