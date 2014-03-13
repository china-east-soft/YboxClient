package cn.cloudchain.yboxclient.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.FragmentManager;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.dialog.UpdateDialogFragment;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.utils.Util;

public class YboxUpdateJumpTask extends BaseFragmentTask {
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;

	private FragmentManager fm;
	private String middleUrl;
	private String imageUrl;

	public YboxUpdateJumpTask(FragmentManager fm, String middleUrl,
			String imageUrl) {
		this.fm = fm;
		this.middleUrl = middleUrl;
		this.imageUrl = imageUrl;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		String response = SetHelper.getInstance().yboxUpdateDownload(imageUrl,
				middleUrl);
		int result = RESULT_FAIL;
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
		if (isCancelled())
			return;

		switch (result) {
		case RESULT_SUCCESS:
			if (fm != null) {
				UpdateDialogFragment fragment = UpdateDialogFragment
						.newInstance(middleUrl, imageUrl);
				fragment.show(fm, null);
			}
			break;
		case RESULT_FAIL:
			Util.toaster(R.string.request_fail);
			break;
		}
	}
}
