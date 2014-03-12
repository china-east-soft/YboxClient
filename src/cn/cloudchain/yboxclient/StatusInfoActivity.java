package cn.cloudchain.yboxclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.task.TrafficJumpTask;
import cn.cloudchain.yboxclient.views.BatteryView;
import cn.cloudchain.yboxclient.views.SignalView;
import cn.cloudchain.yboxclient.views.TrafficView;

public class StatusInfoActivity extends ActionBarActivity implements
		OnClickListener {
	final String TAG = StatusInfoActivity.class.getSimpleName();
	private BatteryView batteryView;
	private TrafficView trafficView;
	private SignalView signalView;

	private MyHandler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setTitle(R.string.status_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_status);
		batteryView = (BatteryView) this.findViewById(R.id.status_battery);
		trafficView = (TrafficView) this.findViewById(R.id.status_traffic);
		signalView = (SignalView) this.findViewById(R.id.status_signal);

		batteryView.setOnClickListener(this);
		trafficView.setOnClickListener(this);
		signalView.setOnClickListener(this);
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
		case R.id.status_traffic:
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
				getOwner().batteryView.setBatteryRemain(msg.arg1);
				// getOwner().batteryView.setBatteryRemainTime(time);
				break;
			case TRAFFIC_COMPLETE:
				Bundle data = msg.getData();
				if (data == null) {
					getOwner().trafficView.setTrafficDetail(-1, -1);
				} else {
					long used = data.getLong("used", -1);
					long total = data.getLong("total", -1);
					getOwner().trafficView.setTrafficDetail(used, total);
				}
				break;
			case SIGNAL_STRENGTH_COMPLETE:
				int strength = msg.arg1;
				if (strength < 0) {
					getOwner().signalView.setSignalStrength(String.format(
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
		new Thread(new Runnable() {

			@Override
			public void run() {
				String response = SetHelper.getInstance()
						.getMobileTrafficInfo();
				Bundle bundle = new Bundle();
				try {
					JSONObject obj = new JSONObject(response);
					if (obj != null && obj.optBoolean("result")) {
						JSONArray array = obj.getJSONArray("data");
						JSONObject item = array.optJSONObject(0);
						if (item != null) {
							JSONObject month = item.optJSONObject("month");
							long limit = item.optLong("limit");
							long monthUsed = month.optLong("tx")
									+ month.optLong("rx");
							bundle.putLong("used", monthUsed);
							bundle.putLong("limit", limit);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Message msg = handler.obtainMessage(MyHandler.TRAFFIC_COMPLETE);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		}).start();
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
