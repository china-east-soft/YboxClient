package cn.cloudchain.yboxclient;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import cn.cloudchain.yboxclient.dialog.CustomDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.fragment.VideoPlayFragment;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.http.HttpHelper;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;
import cn.cloudchain.yboxclient.task.DeviceBindJumpask;
import cn.cloudchain.yboxclient.task.MobileDataControlTask;
import cn.cloudchain.yboxclient.utils.Util;
import cn.cloudchain.yboxclient.views.GridItem1;
import cn.cloudchain.yboxclient.views.GridItem2;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxcommon.bean.Types;

public class MainGridActivity extends BaseActionBarActivity implements
		OnClickListener {
	final static String TAG = MainGridActivity.class.getSimpleName();
	private CheckBox ethernetToggle;
	private CheckBox mobileDataToggle;
	private CheckBox wifiToggle;
	private CheckBox batteryToggle;

	private GridItem1 statusGItem;
	private GridItem1 settingGItem;
	private GridItem1 shutdownGItem;
	private GridItem1 dataGItem;

	private GridItem2 storageGItem;

	private MyHandler handler = new MyHandler(this);
	private ReceiverHandler receiverHandler = new ReceiverHandler(this);
	private ApStatusReceiver statusReceiver;
	private WifiChangeBroadcast wifiChangeReceiver;

	private String url = "http://192.168.43.1:8080/1.ts";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		ethernetToggle = (CheckBox) this.findViewById(R.id.status_ethernet);
		mobileDataToggle = (CheckBox) this.findViewById(R.id.status_data);
		wifiToggle = (CheckBox) this.findViewById(R.id.status_wifi);
		batteryToggle = (CheckBox) this.findViewById(R.id.status_battery);

		statusGItem = (GridItem1) this.findViewById(R.id.tab_status);
		settingGItem = (GridItem1) this.findViewById(R.id.tab_setting);
		shutdownGItem = (GridItem1) this.findViewById(R.id.tab_shutdown);
		dataGItem = (GridItem1) this.findViewById(R.id.tab_mobile_data);

		statusGItem.setOnClickListener(this);
		settingGItem.setOnClickListener(this);
		shutdownGItem.setOnClickListener(this);
		dataGItem.setOnClickListener(this);

		storageGItem = (GridItem2) this.findViewById(R.id.tab_storage);
		storageGItem.setOnClickListener(this);

		this.findViewById(R.id.tab_admin).setOnClickListener(this);
		this.findViewById(R.id.tab_recommend).setOnClickListener(this);
		this.findViewById(R.id.tab_cloud).setOnClickListener(this);

		this.findViewById(R.id.video_more).setOnClickListener(this);
		this.findViewById(R.id.video_full_screen).setOnClickListener(this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver();
		showVideoPlay();
	}

	private void showVideoPlay() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		VideoPlayFragment fragment = VideoPlayFragment.newInstance(url, null);
		ft.add(R.id.video_layout, fragment);
		ft.commitAllowingStateLoss();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshBattery();
		refreshConnType();
		handler.sendEmptyMessage(MyHandler.STORAGE_GET);
	}

	@Override
	protected void onStop() {
		unregisterReceiver();
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.video_layout);
		if (fragment != null && fragment.isAdded()) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.remove(fragment);
			ft.commitAllowingStateLoss();
		}

		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_setting: {
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.tab_shutdown:
			handler.sendEmptyMessage(MyHandler.REMIND_SHUTDOWN);
			break;
		case R.id.tab_status: {
			Intent intent = new Intent(this, StatusInfoActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.tab_mobile_data:
			handler.sendEmptyMessage(MyHandler.ACTION_MOBILE_DATA);
			break;
		case R.id.video_more: {
			Intent intent = new Intent(this, ProgramActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.video_full_screen:
			fullScreenVideo();
			break;
		case R.id.tab_admin: {
			DeviceBindJumpask task = new DeviceBindJumpask(this);
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment(null, true);
			fragment.setTask(task);
			fragment.show(getSupportFragmentManager(), null);
			break;
		}
		case R.id.tab_cloud: {
			Intent intent = new Intent(this, MobileInfoActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.tab_storage: {
			Intent intent = new Intent(this, FileManagerActivity.class);
			startActivity(intent);
			break;
		}
		}

	}

	private void fullScreenVideo() {
		VideoPlayerActivity.start(this, url, null);
	}

	private void refreshConnType() {
		int connType = MyApplication.getInstance().connType;
		dataGItem.setEnabled(connType > 1);
		switch (connType) {
		// 无
		case Types.CONN_TYPE_NONE:
			ethernetToggle.setChecked(false);
			mobileDataToggle.setChecked(false);
			dataGItem.setChecked(false);
			break;
		// 以太网
		case Types.CONN_TYPE_ETHERNET:
			ethernetToggle.setChecked(true);
			mobileDataToggle.setChecked(false);
			dataGItem.setChecked(false);
			break;
		// 手机数据开
		case Types.CONN_TYPE_MOBILE_DATA_ON:
			ethernetToggle.setChecked(false);
			mobileDataToggle.setChecked(true);
			dataGItem.setChecked(true);
			break;
		// 手机数据关
		case Types.CONN_TYPE_MOBILE_DATA_OFF:
			ethernetToggle.setChecked(false);
			mobileDataToggle.setChecked(false);
			dataGItem.setChecked(false);
			break;
		}
	}

	private void refreshBattery() {
		boolean low = MyApplication.getInstance().batteryLow;
		batteryToggle.setChecked(!low);
		if (low) {
			Util.toaster(R.string.warn_battery_low);
		}
	}

	private static class ReceiverHandler extends
			ApStatusHandler<MainGridActivity> {

		public ReceiverHandler(MainGridActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case WIFI_MODE_CHANGE:
				getOwner().refreshConnType();
				break;
			case BATTERY_LOW_CHANGE:
				getOwner().refreshBattery();
				break;
			}
		}
	}

	private void registerReceiver() {
		if (statusReceiver == null) {
			statusReceiver = new ApStatusReceiver(receiverHandler);
		}
		IntentFilter statusFilter = new IntentFilter();
		statusFilter.addAction(ApStatusReceiver.ACTION_WIFI_MODE_CHANGE);
		statusFilter.addAction(ApStatusReceiver.ACTION_BATTERY_LOW_CHANGE);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				statusReceiver, statusFilter);

		if (wifiChangeReceiver == null) {
			wifiChangeReceiver = new WifiChangeBroadcast();
		}
		IntentFilter wifiFilter = new IntentFilter();
		wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(wifiChangeReceiver, wifiFilter);
	}

	private void unregisterReceiver() {
		if (statusReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					statusReceiver);
			statusReceiver = null;
		}

		if (wifiChangeReceiver != null) {
			unregisterReceiver(wifiChangeReceiver);
			wifiChangeReceiver = null;
		}
	}

	private static class MyHandler extends WeakHandler<MainGridActivity> {
		private static final int REMIND_SHUTDOWN = 0;
		private static final int ACTION_SHUTDOWN = 1;
		private static final int ACTION_MOBILE_DATA = 2;
		private static final int STORAGE_GET = 3;
		private static final int STORAGE_GET_SUCCESS = 4;

		public MyHandler(MainGridActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case REMIND_SHUTDOWN:
				getOwner().showShutdownDialog();
				break;
			case ACTION_SHUTDOWN:
				getOwner().handleShutDownAction();
				break;
			case ACTION_MOBILE_DATA:
				getOwner().handleMobileData();
				break;
			case STORAGE_GET:
				getOwner().handleStorageGet();
				break;
			case STORAGE_GET_SUCCESS:
				Bundle data = msg.getData();
				if (data == null)
					break;
				long remain = data.getLong("remain");
				long total = data.getLong("total");
				getOwner().handleStorageGetComplete(remain, total);
				break;
			}
		}
	}

	/**
	 * 处理获取存储卡返回信息
	 */
	private void handleStorageGetComplete(long remain, long total) {
		storageGItem.setSubDes(getString(R.string.storage_info,
				Formatter.formatShortFileSize(this, remain),
				Formatter.formatShortFileSize(this, total)));
	}

	/**
	 * 请求获取存储卡信息剩余/总
	 */
	private void handleStorageGet() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String response = SetHelper.getInstance().getStorageInfo();
				try {
					JSONObject obj = new JSONObject(response);
					if (obj.optBoolean(Constants.RESULT)) {
						Bundle data = new Bundle();
						data.putDouble("remain",
								obj.optLong(Constants.File.MEM_REMAIN));
						data.putDouble("total",
								obj.optLong(Constants.File.MEM_TOTAL));
						Message msg = handler
								.obtainMessage(MyHandler.STORAGE_GET_SUCCESS);
						msg.setData(data);
						handler.sendMessage(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 处理手机数据开关
	 */
	private void handleMobileData() {
		boolean enable = !dataGItem.isChecked();
		MobileDataControlTask task = new MobileDataControlTask(this, enable);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				enable ? "正在打开数据" : "正在关闭数据", true);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), null);
	}

	/**
	 * 向终端发送关机命令
	 */
	private void handleShutDownAction() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SetHelper.getInstance().shutdown(false);
			}
		}).start();
	}

	/**
	 * 关机提示对话框
	 */
	private void showShutdownDialog() {
		CustomDialogFragment fragment = CustomDialogFragment.newInstance(-1,
				R.string.shutdown_reminder, R.string.confirm, R.string.cancel,
				true);
		fragment.setDialogService(new IDialogService() {

			@Override
			public void onClick(DialogFragment fragment, int actionId) {
				if (actionId == R.id.dialog_click_positive) {
					handler.sendEmptyMessage(MyHandler.ACTION_SHUTDOWN);
				}
			}

			@Override
			public View getDialogView() {
				return null;
			}
		});
		fragment.show(getSupportFragmentManager(), CustomDialogFragment.TAG);
	}

	/**
	 * 用于监听无线网络连接变化
	 * 
	 * @author lazzy
	 * 
	 */
	private class WifiChangeBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
				if (state == WifiManager.WIFI_STATE_DISABLED) {
					wifiToggle.setChecked(false);
				}
			} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				wifiToggle.setChecked(Util.isValidApSSID(HttpHelper
						.getWifiSsid(context)));
			}
		}
	}

}
