package cn.cloudchain.yboxclient.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.TrafficDetailActivity;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.utils.Util;

public class TrafficJumpTask extends BaseFragmentTask {
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;

	private Context context;
	private Bundle bundle;

	public TrafficJumpTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		String response = SetHelper.getInstance().getMobileTrafficInfo();
		int result = RESULT_FAIL;
		try {
			JSONObject obj = new JSONObject(response);
			if (obj != null && obj.optBoolean("result")) {
				JSONArray array = obj.getJSONArray("data");
				JSONObject item = array.optJSONObject(0);
				if (item == null) {
					return result;
				}
				result = RESULT_SUCCESS;
				bundle = new Bundle();

				JSONObject today = item.optJSONObject("today");
				JSONObject month = item.optJSONObject("month");
				long limit = item.optLong("limit");
				long warn = item.optLong("warn");

				bundle.putString(
						TrafficDetailActivity.BUNDLE_TODAY_USED,
						Formatter.formatFileSize(context, today.optLong("tx")
								+ today.optLong("rx")));
				bundle.putString(
						TrafficDetailActivity.BUNDLE_MONTH_USED,
						Formatter.formatFileSize(context, month.optLong("tx")
								+ month.optLong("rx")));
				if (limit > 0L) {
					bundle.putString(TrafficDetailActivity.BUNDLE_LIMIT,
							Formatter.formatFileSize(context, limit));
				}
				if (warn > 0L) {
					bundle.putString(TrafficDetailActivity.BUNDLE_WARNINT,
							Formatter.formatFileSize(context, warn));
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
			Intent intent = new Intent(context, TrafficDetailActivity.class);
			if (bundle != null)
				intent.putExtras(bundle);
			context.startActivity(intent);
			break;
		}
	}
}