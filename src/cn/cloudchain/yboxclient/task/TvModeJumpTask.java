package cn.cloudchain.yboxclient.task;

import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.TvModeActivity;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.helper.ApHelper;
import cn.cloudchain.yboxclient.utils.Util;

public class TvModeJumpTask extends BaseFragmentTask {
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;

	private String currentMode;
	private String[] allModes;
	private Context context;

	public TvModeJumpTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int result = RESULT_FAIL;
		try {
			String response = ApHelper.getInstance().getTVModes();
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean("result")) {
				result = RESULT_SUCCESS;
				currentMode = obj.optString("current");

				String modes = obj.optString("modes");
				if (TextUtils.isEmpty(modes)) {
					return result;
				}

				StringTokenizer tokenizer = new StringTokenizer(modes);
				allModes = new String[tokenizer.countTokens()];

				for (int i = 0; tokenizer.hasMoreTokens(); ++i) {
					allModes[i] = tokenizer.nextToken();
				}
			}
		} catch (YunmaoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (isCancelled()) {
			return;
		}
		switch (result) {
		case RESULT_FAIL:
			Util.toaster(R.string.request_fail);
			break;
		case RESULT_SUCCESS:
			if (allModes == null || allModes.length == 0) {
				Util.toaster(R.string.tvmodes_empty);
				break;
			}
			Intent intent = new Intent(context, TvModeActivity.class);
			intent.putExtra("current", currentMode);
			intent.putExtra("modes", allModes);
			context.startActivity(intent);
			break;
		}

	}

}
