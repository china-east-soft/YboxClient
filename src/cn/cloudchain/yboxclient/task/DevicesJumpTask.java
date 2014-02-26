package cn.cloudchain.yboxclient.task;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import cn.cloudchain.yboxclient.DevicesActivity;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;

/**
 * 获取成功后跳转到DevicesActivity
 * 
 * @author lazzy
 * 
 */
public class DevicesJumpTask extends BaseFragmentTask {
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;
	private Context context;
	private ArrayList<DeviceInfo> devices;

	public DevicesJumpTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		int result = RESULT_FAIL;
		String response = SetHelper.getInstance().getDevices();
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean("result")) {
				result = RESULT_SUCCESS;

				JSONArray array = obj.optJSONArray("devices");
				int size = array.length();
				devices = new ArrayList<DeviceInfo>(size);
				for (int i = 0; i < size; ++i) {
					DeviceInfo info = new DeviceInfo();
					JSONObject item = array.getJSONObject(i);
					info.ip = item.optString("ip");
					info.mac = item.optString("mac");
					info.name = item.optString("name");
					info.blocked = item.optBoolean("block");
					devices.add(info);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (isCancelled())
			return;
		switch (result) {
		case RESULT_FAIL:
			Util.toaster(R.string.request_fail);
			break;
		case RESULT_SUCCESS:
			Intent intent = new Intent(context, DevicesActivity.class);
			if (devices != null)
				intent.putExtra(DevicesActivity.BUNDLE_DEVICES, devices);
			context.startActivity(intent);
			break;
		}
	}
}
