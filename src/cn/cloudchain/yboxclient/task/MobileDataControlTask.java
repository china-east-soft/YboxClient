package cn.cloudchain.yboxclient.task;

import org.json.JSONException;
import org.json.JSONObject;

import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import android.content.Context;

public class MobileDataControlTask extends BaseFragmentTask {
	private static final int RESULT_SUCCESS = 0;
	private static final int RESULT_FAIL = 1;
	private boolean enable;

	public MobileDataControlTask(Context context, boolean enable) {
		this.enable = enable;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int result = RESULT_FAIL;
		String response = SetHelper.getInstance().setMobileDataEnable(enable);
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
		switch (result) {
		case RESULT_FAIL:
			Util.toaster(R.string.data_open_fail);
			break;
		}
	}

}
