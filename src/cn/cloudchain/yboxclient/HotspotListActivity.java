package cn.cloudchain.yboxclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.Toast;
import cn.cloudchain.yboxclient.adapter.HotspotListAdapter;
import cn.cloudchain.yboxclient.face.IBlackListService;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;

public class HotspotListActivity extends Activity implements IBlackListService {
	final String TAG = HotspotListActivity.class.getSimpleName();
	private ListView listView;
	private MyHandler handler = new MyHandler(this);
	private HotspotListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_hotspot_list);
		String jsonContent = getIntent().getStringExtra("content");
		List<DeviceInfo> array = getArray(jsonContent);
		listView = (ListView) this.findViewById(R.id.listview);
		adapter = new HotspotListAdapter(this, this);
		adapter.setDeviceList(array);
		listView.setAdapter(adapter);
	}

	private List<DeviceInfo> getArray(String jsonContent) {
		List<DeviceInfo> array = new ArrayList<DeviceInfo>();
		try {
			JSONObject object = new JSONObject(jsonContent);
			boolean result = object.optBoolean("result", false);
			if (!result) {
				Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show();
				return array;
			}
			JSONArray devices = object.optJSONArray("devices");
			for (int i = 0; i < devices.length(); i++) {
				JSONObject device = devices.getJSONObject(i);
				DeviceInfo info = new DeviceInfo();
				info.mac = device.optString("mac");
				info.ip = device.optString("ip");
				info.blocked = device.optBoolean("block");
				array.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array;
	}

	@Override
	public void dealBlackList(DeviceInfo info) {
		if (info == null) {
			return;
		}

		Message msg = handler.obtainMessage();
		msg.obj = info.mac;
		msg.what = info.blocked ? MyHandler.MSG_BLACK_CLEAR
				: MyHandler.MSG_BLACK_ADD;
		handler.sendMessage(msg);
	}

	private static class MyHandler extends WeakHandler<HotspotListActivity> {
		private final static int MSG_DEVICES_GET = 0;
		private final static int MSG_BLACK_ADD = 1;
		private final static int MSG_BLACK_CLEAR = 2;
		private final static int MSG_DEVICES_GET_COMPLETE = 3;
		private final static int MSG_BLACK_COMPLETE = 4;

		public MyHandler(HotspotListActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case MSG_BLACK_ADD:
				getOwner().handleBlackListAdd((String) msg.obj);
				break;
			case MSG_BLACK_CLEAR:
				getOwner().handleBlackListClear((String) msg.obj);
				break;
			case MSG_BLACK_COMPLETE:
				getOwner().handleBlackComplete((String) msg.obj);
				break;
			case MSG_DEVICES_GET:
				getOwner().handleDevice();
				break;
			case MSG_DEVICES_GET_COMPLETE:
				getOwner().handleDeviceComplete((String) msg.obj);
				break;
			}
		}
	}

	private void handleBlackListAdd(final String mac) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().addToBlackList(mac,
						false);
				Message msg = handler
						.obtainMessage(MyHandler.MSG_BLACK_COMPLETE);
				msg.obj = result;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void handleBlackListClear(final String mac) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().clearBlackList(mac);
				Message msg = handler
						.obtainMessage(MyHandler.MSG_BLACK_COMPLETE);
				msg.obj = result;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void handleBlackComplete(String message) {
		Toast.makeText(this, TextUtils.isEmpty(message) ? "请求失败，返回空" : message,
				Toast.LENGTH_SHORT).show();
		handler.sendEmptyMessage(MyHandler.MSG_DEVICES_GET);
	}

	private void handleDevice() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().getDevices();
				Message msg = handler
						.obtainMessage(MyHandler.MSG_DEVICES_GET_COMPLETE);
				msg.obj = result;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void handleDeviceComplete(String message) {
		List<DeviceInfo> array = getArray(message);
		adapter.setDeviceList(array);
		adapter.notifyDataSetChanged();
	}

}
