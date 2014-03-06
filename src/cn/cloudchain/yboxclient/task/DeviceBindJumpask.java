package cn.cloudchain.yboxclient.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import cn.cloudchain.yboxclient.DeviceBindActivity;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.utils.Util;

/**
 * 获取设备信息，获取成功后跳转到DeviceBindActivity
 * 
 * @author lazzy
 * 
 */
public class DeviceBindJumpask extends BaseFragmentTask {
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;
	private Context context;
	private String mac = "";

	public DeviceBindJumpask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		int result = RESULT_FAIL;
		String response = SetHelper.getInstance().getDeviceInfo();
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean("result")) {
				mac = obj.optString("mac");
				result = RESULT_SUCCESS;
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
			Intent intent = new Intent(context, DeviceBindActivity.class);
			intent.putExtra(DeviceBindActivity.BUNDLE_DEVICE_MAC, mac);
			context.startActivity(intent);
			break;
		}
	}

}
