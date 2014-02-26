package cn.cloudchain.yboxclient;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
import cn.cloudchain.yboxclient.dialog.CustomDialogFragment;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;

public class MainGridActivity extends ActionBarActivity implements
		OnClickListener, OnCheckedChangeListener {
	private MyHandler handler = new MyHandler(this);
	private ReceiverHandler receiverHandler = new ReceiverHandler(this);
	private ApStatusReceiver statusReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GridLayout gridLay = new GridLayout(this);
		gridLay.setColumnCount(10);
		gridLay.setRowCount(10);

		GridLayout.LayoutParams params0 = new GridLayout.LayoutParams();
		params0.columnSpec = GridLayout.spec(0, 6, GridLayout.FILL);
		params0.rowSpec = GridLayout.spec(0, 2, GridLayout.FILL);
		params0.setMargins(0, 0, 0, 0);
		Button button0 = new Button(this);
		button0.setLayoutParams(params0);
		button0.setText("基本状态");
		gridLay.addView(button0);

		GridLayout.LayoutParams params1 = new GridLayout.LayoutParams();
		params1.columnSpec = GridLayout.spec(6, 2, GridLayout.FILL);
		Button button1 = new Button(this);
		button1.setId(R.id.tab_status);
		button1.setLayoutParams(params1);
		button1.setText("状态");
		button1.setOnClickListener(this);
		gridLay.addView(button1);

		GridLayout.LayoutParams params2 = new GridLayout.LayoutParams();
		params2.columnSpec = GridLayout.spec(8, 2);
		Button button2 = new Button(this);
		button2.setId(R.id.tab_setting);
		button2.setLayoutParams(params2);
		button2.setText("设置");
		button2.setOnClickListener(this);
		gridLay.addView(button2);

		GridLayout.LayoutParams params3 = new GridLayout.LayoutParams();
		params3.columnSpec = GridLayout.spec(6, 2);
		Button button3 = new Button(this);
		button3.setId(R.id.tab_shutdown);
		button3.setLayoutParams(params3);
		button3.setText("关机");
		button3.setOnClickListener(this);
		gridLay.addView(button3);

		GridLayout.LayoutParams params4 = new GridLayout.LayoutParams();
		params4.columnSpec = GridLayout.spec(8, 2);
		ToggleButton button4 = new ToggleButton(this);
		button4.setId(R.id.tab_mobile_data);
		button4.setLayoutParams(params4);
		button4.setTextOff("数据关");
		button4.setTextOn("数据开");
		button4.setChecked(MyApplication.getInstance().wifiMode.equals("3g"));
		button4.setOnCheckedChangeListener(this);
		gridLay.addView(button4);

		GridLayout.LayoutParams params5 = new GridLayout.LayoutParams();
		params5.columnSpec = GridLayout.spec(0, 10, GridLayout.FILL);
		params5.rowSpec = GridLayout.spec(2, 6, GridLayout.FILL);
		Button button5 = new Button(this);
		button5.setLayoutParams(params5);
		button5.setText("电视");
		gridLay.addView(button5);

		setContentView(gridLay);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver();
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
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.tab_mobile_data) {

		}
	}

	private void refreshView() {
		// 当以太网连接时，数据不可点
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
		registerReceiver(statusReceiver, filter);
	}

	private void unregisterReceiver() {
		if (statusReceiver != null) {
			unregisterReceiver(statusReceiver);
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
