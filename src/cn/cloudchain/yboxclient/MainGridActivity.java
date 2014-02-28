package cn.cloudchain.yboxclient;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;
import cn.cloudchain.yboxclient.dialog.CustomDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;
import cn.cloudchain.yboxclient.task.MobileDataControlTask;
import cn.cloudchain.yboxclient.views.GridItem1;

public class MainGridActivity extends BaseActionBarActivity implements
		OnClickListener {
	final static String TAG = MainGridActivity.class.getSimpleName();
	private ToggleButton ethernetToggle;
	private ToggleButton mobileDataToggle;
	private ToggleButton wifiToggle;
	private ToggleButton batteryToggle;

	private GridItem1 statusGItem;
	private GridItem1 settingGItem;
	private GridItem1 shutdownGItem;
	private GridItem1 dataGItem;

	private MyHandler handler = new MyHandler(this);
	private ReceiverHandler receiverHandler = new ReceiverHandler(this);
	private ApStatusReceiver statusReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.layout_main);
		ethernetToggle = (ToggleButton) this.findViewById(R.id.status_ethernet);
		mobileDataToggle = (ToggleButton) this.findViewById(R.id.status_data);
		wifiToggle = (ToggleButton) this.findViewById(R.id.status_wifi);
		batteryToggle = (ToggleButton) this.findViewById(R.id.status_battery);

		statusGItem = (GridItem1) this.findViewById(R.id.tab_status);
		settingGItem = (GridItem1) this.findViewById(R.id.tab_setting);
		shutdownGItem = (GridItem1) this.findViewById(R.id.tab_shutdown);
		dataGItem = (GridItem1) this.findViewById(R.id.tab_mobile_data);

		statusGItem.setOnClickListener(this);
		settingGItem.setOnClickListener(this);
		shutdownGItem.setOnClickListener(this);
		dataGItem.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshBattery();
		refreshConnType();
	}

	@Override
	protected void onStop() {
		unregisterReceiver();
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
		}

	}

	private void refreshConnType() {
		int connType = MyApplication.getInstance().connType;
		dataGItem.setEnabled(connType != 0);
		switch (connType) {
		// 无
		case 0:
			ethernetToggle.setChecked(false);
			mobileDataToggle.setChecked(false);
			dataGItem.setChecked(false);
			break;
		// 以太网
		case 1:
			ethernetToggle.setChecked(true);
			mobileDataToggle.setChecked(false);
			dataGItem.setChecked(false);
			break;
		// 手机数据开
		case 2:
			ethernetToggle.setChecked(false);
			mobileDataToggle.setChecked(true);
			dataGItem.setChecked(true);
			break;
		// 手机数据关
		case 3:
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
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApStatusReceiver.ACTION_WIFI_MODE_CHANGE);
		filter.addAction(ApStatusReceiver.ACTION_BATTERY_LOW_CHANGE);
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

	private static class MyHandler extends WeakHandler<MainGridActivity> {
		private static final int REMIND_SHUTDOWN = 0;
		private static final int ACTION_SHUTDOWN = 1;
		private static final int ACTION_MOBILE_DATA = 2;

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
			}
		}
	}

	private void handleMobileData() {
		boolean enable = !dataGItem.isChecked();
		MobileDataControlTask task = new MobileDataControlTask(this, enable);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				enable ? "正在打开数据" : "正在关闭数据", true);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), null);
	}

	private void handleShutDownAction() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SetHelper.getInstance().shutdown(false);
			}
		}).start();
	}

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

}
