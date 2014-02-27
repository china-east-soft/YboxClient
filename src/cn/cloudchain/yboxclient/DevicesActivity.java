package cn.cloudchain.yboxclient;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import cn.cloudchain.yboxclient.adapter.DeviceListAdapter;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.face.IBlackListService;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.server.ApStatusReceiver;
import cn.cloudchain.yboxclient.task.BlackListHandleTask;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;

public class DevicesActivity extends ActionBarActivity implements
		IBlackListService {
	public static final String BUNDLE_DEVICES = "devices";
	private ListView listView;
	private ProgressBar progressBar;
	private DeviceListAdapter adapter;

	private ArrayList<DeviceInfo> deviceList;
	private ReceiveHandler receiveHandler = new ReceiveHandler(this);
	private ApStatusReceiver statusReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.devices_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.base_listview);
		listView = (ListView) this.findViewById(R.id.base_listview);
		progressBar = (ProgressBar) this.findViewById(R.id.progressbar);
		adapter = new DeviceListAdapter(this, this);
		listView.setAdapter(adapter);

		initView(getIntent().getExtras());
	}

	private void initView(Bundle bundle) {
		if (bundle == null)
			return;
		deviceList = bundle.getParcelableArrayList(BUNDLE_DEVICES);
		adapter.setDevices(deviceList);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver();
	}

	@Override
	protected void onPause() {
		unregisterReceiver();
		super.onPause();
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
	public void dealBlackList(DeviceInfo info) {
		if (info == null)
			return;
		handleBlackSet(info);
	}

	private static class ReceiveHandler extends
			ApStatusHandler<DevicesActivity> {
		private static final int BLACK_GET_FAIL = 8;
		private static final int BLACK_GET_SUCCESS = 9;

		public ReceiveHandler(DevicesActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case HOTSPOT_CLIENT_CHANGE:
				getOwner().progressBar.setVisibility(View.VISIBLE);
				getOwner().handleBlackRefresh();
				break;
			case BLACK_GET_SUCCESS:
				Bundle data = msg.getData();
				if (data != null) {
					ArrayList<DeviceInfo> devices = data
							.getParcelableArrayList(BUNDLE_DEVICES);
					getOwner().handleBlackGetSuccess(devices);
				}
				getOwner().progressBar.setVisibility(View.GONE);
				break;
			case BLACK_GET_FAIL:
				getOwner().progressBar.setVisibility(View.GONE);
				break;
			}
		}
	}

	private void handleBlackGetSuccess(ArrayList<DeviceInfo> devices) {
		deviceList = devices;
		adapter.setDevices(deviceList);
		adapter.notifyDataSetChanged();
	}

	private void handleBlackRefresh() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String response = SetHelper.getInstance().getDevices();
				try {
					JSONObject obj = new JSONObject(response);
					if (obj.optBoolean("result")) {
						JSONArray array = obj.optJSONArray("devices");
						int size = array.length();
						ArrayList<DeviceInfo> devices = new ArrayList<DeviceInfo>(
								size);
						for (int i = 0; i < size; ++i) {
							DeviceInfo info = new DeviceInfo();
							JSONObject item = array.getJSONObject(i);
							info.ip = item.optString("ip");
							info.mac = item.optString("mac");
							info.name = item.optString("name");
							info.blocked = item.optBoolean("block");
							devices.add(info);
						}

						Message msg = receiveHandler
								.obtainMessage(ReceiveHandler.BLACK_GET_SUCCESS);
						Bundle data = new Bundle();
						data.putParcelableArrayList(BUNDLE_DEVICES, devices);
						msg.setData(data);
						receiveHandler.sendMessage(msg);
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				receiveHandler.sendEmptyMessage(ReceiveHandler.BLACK_GET_FAIL);
			}
		}).start();
	}

	private void handleBlackSet(DeviceInfo device) {
		BlackListHandleTask task = new BlackListHandleTask(device,
				receiveHandler);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				"正在处理中...", true);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), null);
	}

	private void registerReceiver() {
		if (statusReceiver == null) {
			statusReceiver = new ApStatusReceiver(receiveHandler);
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
