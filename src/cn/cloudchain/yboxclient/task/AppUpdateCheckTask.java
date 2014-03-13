package cn.cloudchain.yboxclient.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.helper.ServerHelper;

/**
 * 检查APP更新，若信息获取失败或者该版本号不存在，则返回null，否则均不为null
 * 
 * @author lazzy
 * 
 */
public class AppUpdateCheckTask extends BaseFragmentTask {
	final static String TAG = AppUpdateCheckTask.class.getSimpleName();

	public static final String APP_UPDATE_ENFORCE = "update_enforce";
	public static final String APP_UPDATE_VERSION = "update_version";
	public static final String APP_UPDATE_MESSAGE = "update_log";
	public static final String APP_UPDATE_FILEPATH = "update_link";

	public static final int APP_UPDATE_INFO_FAIL = 301;
	public static final int APP_NO_NEED_UPDATE = 302;
	public static final int APP_HAS_UPDATE = 303;
	public static final int APP_CURRENT_VERSION_INVALID = 304;

	private Handler mHandler;
	private Bundle bundle = null;
	private boolean checkedByUser;

	/**
	 * 检查APP更新，若信息获取失败或者该版本号不存在，则返回null，否则不为null
	 * 
	 * @param context
	 * @param checkedByUser
	 *            是否用户主动点击，若为true时，msg.arg1=1，为false时，msg.arg1=0
	 * @param handler
	 *            若task被cancel，则不会sendMessage
	 */
	public AppUpdateCheckTask(Context context, boolean checkedByUser,
			Handler handler) {
		this.mHandler = handler;
		this.checkedByUser = checkedByUser;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		String responseStr = null;
		int errorCode = YunmaoException.ERROR_CODE_DEFAULT;
		try {
			ServerHelper.getInstance().getAppVersions();
		} catch (YunmaoException e) {
			errorCode = e.getErrorCode();
			if (errorCode == YunmaoException.ERROR_CODE_NONE) {
				responseStr = e.getMessage();
			}
		}
		if (errorCode == 1) {
			return APP_CURRENT_VERSION_INVALID;
		} else if (TextUtils.isEmpty(responseStr)) {
			return APP_UPDATE_INFO_FAIL;
		}

		// 默认当前版本为最新的
		boolean isLatest = true;
		bundle = new Bundle();
		try {
			JSONObject object = new JSONObject(responseStr);
			isLatest = object.optBoolean("is_latest", true);

			if (!isLatest) {
				bundle.putBoolean(APP_UPDATE_ENFORCE,
						object.optBoolean("enforce"));
				bundle.putString(APP_UPDATE_VERSION,
						object.optString("release_version"));
				bundle.putString(APP_UPDATE_MESSAGE,
						object.optString("changelog"));
				bundle.putString(APP_UPDATE_FILEPATH, object.optString("link"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return isLatest ? APP_NO_NEED_UPDATE : APP_HAS_UPDATE;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (isCancelled() || mHandler == null) {
			return;
		}
		Message msg = mHandler.obtainMessage(result);
		msg.arg1 = checkedByUser ? 1 : 0;
		if (bundle != null) {
			msg.setData(bundle);
		}
		mHandler.sendMessage(msg);
	}
}