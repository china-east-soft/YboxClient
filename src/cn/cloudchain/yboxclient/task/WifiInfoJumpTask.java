package cn.cloudchain.yboxclient.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.WifiSetActivity;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.utils.Util;
import cn.cloudchain.yboxcommon.bean.Constants;

/**
 * 获取wifi信息成功后跳转到WifiSetActivity
 * 
 * @author lazzy
 * 
 */
public class WifiInfoJumpTask extends BaseFragmentTask {
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;
	private Context context;
	private Bundle bundle = null;

	public WifiInfoJumpTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		int result = RESULT_FAIL;
		String response = SetHelper.getInstance().getWifiInfo();
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean(Constants.RESULT)) {
				result = RESULT_SUCCESS;
				bundle = new Bundle();
				bundle.putString(WifiSetActivity.BUNDLE_SSID,
						obj.optString(Constants.Wifi.SSID));
				bundle.putInt(WifiSetActivity.BUNDLE_CHANNEL,
						obj.optInt(Constants.Wifi.CHANNEL));
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
			Intent intent = new Intent(context, WifiSetActivity.class);
			if (bundle != null)
				intent.putExtras(bundle);
			context.startActivity(intent);
			break;
		}
	}

}
