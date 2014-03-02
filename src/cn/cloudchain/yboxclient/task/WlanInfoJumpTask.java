package cn.cloudchain.yboxclient.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.WlanSetActivity;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import cn.cloudchain.yboxcommon.bean.ErrorBean;
import cn.cloudchain.yboxcommon.bean.Types;

/**
 * 获取以太网信息成功后跳转到WlanSetActivity
 * 
 * @author lazzy
 * 
 */
public class WlanInfoJumpTask extends BaseFragmentTask {
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;
	private Bundle bundle = null;
	private Context context;
	private int errorCode = ErrorBean.REQUEST_TIMEOUT;

	public WlanInfoJumpTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		int result = RESULT_FAIL;
		String response = "";
		if (MyApplication.getInstance().connType == Types.CONN_TYPE_ETHERNET) {
			response = SetHelper.getInstance().getEthernetInfo();
		} else {
			response = SetHelper.getInstance().getMobileNetInfo();
		}
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean("result")) {
				result = RESULT_SUCCESS;
				bundle = new Bundle();
				bundle.putInt(WlanSetActivity.BUNDLE_MODE,
						obj.optInt("mode", Types.ETHERNET_MODE_NONE));
				bundle.putString(WlanSetActivity.BUNDLE_IPADDRESS,
						obj.optString("ip"));
				bundle.putString(WlanSetActivity.BUNDLE_MASK,
						obj.optString("mask"));
				bundle.putString(WlanSetActivity.BUNDLE_GATEWAY,
						obj.optString("gateway"));
				bundle.putString(WlanSetActivity.BUNDLE_DNS1,
						obj.optString("dns1"));
				bundle.putString(WlanSetActivity.BUNDLE_DNS2,
						obj.optString("dns2"));
			} else {
				errorCode = obj.optInt("error_code", -1);
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
			if (errorCode == ErrorBean.REQUEST_TIMEOUT) {
				Util.toaster("请求超时", null);
			} else {
				Util.toaster(R.string.request_fail);
			}
			break;
		case RESULT_SUCCESS:
			Intent intent = new Intent(context, WlanSetActivity.class);
			if (bundle != null)
				intent.putExtras(bundle);
			context.startActivity(intent);
			break;
		}
	}
}