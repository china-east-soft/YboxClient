package cn.cloudchain.yboxclient;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.task.TrafficJumpTask;

public class StatusInfoActivity extends ActionBarActivity implements
		OnClickListener {
	final String TAG = StatusInfoActivity.class.getSimpleName();
	private TextView trafficRemainView;
	private TextView batteryRemainView;
	private TextView signalStrengthView;

	private MyHandler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_status);

		getSupportActionBar().setTitle(R.string.status_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		this.findViewById(R.id.status_traffic_layout).setOnClickListener(this);
		trafficRemainView = (TextView) this
				.findViewById(R.id.status_traffic_remain);
		batteryRemainView = (TextView) this
				.findViewById(R.id.status_battery_remain);
		signalStrengthView = (TextView) this
				.findViewById(R.id.status_signal_strength);

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
	protected void onStart() {
		super.onStart();
		handler.sendEmptyMessage(MyHandler.REFRESH_ALL_STATUS);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.status_traffic_layout:
			jumpToTraffic();
			break;
		}
	}

	private void jumpToTraffic() {
		TrafficJumpTask task = new TrafficJumpTask(this);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				null, true);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), null);
	}

	private static class MyHandler extends WeakHandler<StatusInfoActivity> {
		private static final int REFRESH_ALL_STATUS = 1;
		private static final int BATTERY_COMPLETE = 2;
		private static final int TRAFFIC_COMPLETE = 3;
		private static final int SIGNAL_STRENGTH_COMPLETE = 4;

		public MyHandler(StatusInfoActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case REFRESH_ALL_STATUS:
				getOwner().refreshTrafficInfo();
				getOwner().refreshBatteryInfo();
				getOwner().refreshSignalStrength();
				break;
			case BATTERY_COMPLETE:
				int battery = msg.arg1;
				if (battery > 0) {
					getOwner().batteryRemainView.setText(String.format("%d%%",
							battery));
				}
				break;
			case TRAFFIC_COMPLETE:
				String traffic = (String) msg.obj;
				if (!TextUtils.isEmpty(traffic)) {
					getOwner().trafficRemainView.setText(traffic);
				}
				break;
			case SIGNAL_STRENGTH_COMPLETE:
				int strength = msg.arg1;
				if (strength < 0) {
					getOwner().signalStrengthView.setText(String.format(
							"%ddBm", strength));
				}
				break;
			}
		}
	}

	private void refreshBatteryInfo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getBattery();
				int battery = -1;
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.optBoolean("result")) {
						battery = obj.optInt("remain");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Message msg = handler.obtainMessage(MyHandler.BATTERY_COMPLETE);
				msg.arg1 = battery;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void refreshTrafficInfo() {
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// String result = SetHelper.getInstance().getMobileTrafficInfo();
		// Log.i(TAG, result);
		// }
		// }).start();
	}

	private void refreshSignalStrength() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getSignalQuality();
				int strength = 0;
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.optBoolean("result")) {
						strength = obj.optInt("strength");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Message msg = handler
						.obtainMessage(MyHandler.SIGNAL_STRENGTH_COMPLETE);
				msg.arg1 = strength;
				handler.sendMessage(msg);
			}
		}).start();
	}

}
