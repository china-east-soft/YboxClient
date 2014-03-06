package cn.cloudchain.yboxclient.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.utils.Util;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;

/**
 * 处理黑名单，如果当前设备未在黑名单上则加入黑名单，否则从黑名单上移除，请求成功后请求刷新黑名单
 * 
 * @author lazzy
 * 
 */
public class BlackListHandleTask extends BaseFragmentTask {
	private static final int RESULT_SUCCESS = 0;
	private static final int RESULT_FAIL = 1;
	private static final int DEVICE_INVALID = 2;

	private DeviceInfo device;
	private ApStatusHandler<?> handler;

	public BlackListHandleTask(DeviceInfo device, ApStatusHandler<?> handler) {
		this.device = device;
		this.handler = handler;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		if (device == null || TextUtils.isEmpty(device.mac))
			return DEVICE_INVALID;
		int result = RESULT_FAIL;
		String response = "";
		if (device.blocked) {
			response = SetHelper.getInstance().clearBlackList(device.mac);
		} else {
			response = SetHelper.getInstance().addToBlackList(device.mac);
		}

		if (TextUtils.isEmpty(response)) {
			return result;
		}

		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean("result")) {
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
		if (result == RESULT_SUCCESS && handler != null) {
			handler.sendEmptyMessage(ApStatusHandler.HOTSPOT_CLIENT_CHANGE);
		} else if (!isCancelled() && result != RESULT_SUCCESS) {
			Util.toaster(R.string.request_fail);
		}
	}
}