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
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.utils.LogUtil;

public class YboxUpdateCheckTask extends BaseFragmentTask {
	final String TAG = YboxUpdateCheckTask.class.getSimpleName();

	public static final String MIDDLE_UPDATE_ENFORCE = "middle_update_enforce";
	public static final String MIDDLE_UPDATE_VERSION = "middle_update_version";
	public static final String MIDDLE_UPDATE_MESSAGE = "middle_update_log";
	public static final String MIDDLE_UPDATE_FILEPATH = "middle_update_link";

	public static final String IMAGE_UPDATE_ENFORCE = "image_update_enforce";
	public static final String IMAGE_UPDATE_VERSION = "image_update_version";
	public static final String IMAGE_UPDATE_MESSAGE = "image_update_log";
	public static final String IMAGE_UPDATE_FILEPATH = "image_update_link";

	public static final int YBOX_UPDATE_CHECK_SOCKET_FAIL = 400;
	public static final int YBOX_UPDATE_CHECK_HTTP_FAIL = 401;
	public static final int YBOX_NO_NEED_UPDATE = 402;
	public static final int YBOX_HAS_UPDATE = 403;
	public static final int YBOX_CURRENT_VERSION_INVALID = 404;

	private boolean checkByUser;
	private Handler handler;
	private Bundle bundle = null;

	/**
	 * 检查YBOX更新，若信息获取失败或者该版本号不存在，则返回null，否则不为null
	 * 
	 * @param context
	 * @param checkedByUser
	 *            是否用户主动点击，若为true时，msg.arg1=1，为false时，msg.arg1=0
	 * @param handler
	 *            若task被cancel，则不会sendMessage
	 */
	public YboxUpdateCheckTask(Context context, boolean checkByUser,
			Handler handler) {
		this.checkByUser = checkByUser;
		this.handler = handler;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		String yboxInfo = SetHelper.getInstance().getDeviceInfo();
		String imageVersion = "";
		String middleVersion = "";
		String imageName = "";
		String middleName = "";
		try {
			JSONObject yboxObj = new JSONObject(yboxInfo);
			if (yboxObj.optBoolean("result")) {
				imageVersion = yboxObj.optString("image_version");
				imageName = yboxObj.optString("image_name");
				middleVersion = yboxObj.optString("middle_version");
				middleName = yboxObj.optString("middle_name");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		LogUtil.d(TAG, String.format("image version = %s middle version = %s",
				imageVersion, middleVersion));
		if (TextUtils.isEmpty(imageVersion) || TextUtils.isEmpty(middleVersion)
				|| TextUtils.isEmpty(imageName)
				|| TextUtils.isEmpty(middleName)) {
			return YBOX_UPDATE_CHECK_SOCKET_FAIL;
		}

		// 检查rootimage是否存在更新
		int errorCode = YunmaoException.ERROR_CODE_DEFAULT;
		String response = "";
		try {
			ServerHelper.getInstance().getYboxUpdate(true, imageVersion,
					imageName);
		} catch (YunmaoException e) {
			errorCode = e.getErrorCode();
			if (errorCode == YunmaoException.ERROR_CODE_NONE) {
				response = e.getMessage();
			}
		}

		if (errorCode == 1) {
			return YBOX_CURRENT_VERSION_INVALID;
		} else if (TextUtils.isEmpty(response)) {
			return YBOX_UPDATE_CHECK_HTTP_FAIL;
		}

		bundle = new Bundle();
		try {
			JSONObject imageObj = new JSONObject(response);
			if (!imageObj.optBoolean("is_latest", true)) {
				bundle.putBoolean(IMAGE_UPDATE_ENFORCE,
						imageObj.optBoolean("enforce"));
				bundle.putString(IMAGE_UPDATE_VERSION,
						imageObj.optString("release_version"));
				bundle.putString(IMAGE_UPDATE_MESSAGE,
						imageObj.optString("changelog"));
				bundle.putString(IMAGE_UPDATE_FILEPATH,
						imageObj.optString("link"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// 检查中间件是否存在更新
		try {
			ServerHelper.getInstance().getYboxUpdate(false, middleVersion,
					middleName);
		} catch (YunmaoException e) {
			errorCode = e.getErrorCode();
			if (errorCode == YunmaoException.ERROR_CODE_NONE) {
				response = e.getMessage();
			} else {
				// 如果有错误，重置bundle为null
				bundle = null;
			}
		}

		if (errorCode == 1) {
			return YBOX_CURRENT_VERSION_INVALID;
		} else if (TextUtils.isEmpty(response)) {
			return YBOX_UPDATE_CHECK_HTTP_FAIL;
		}

		if (bundle == null) {
			bundle = new Bundle();
		}

		try {
			JSONObject middleObj = new JSONObject(response);
			if (!middleObj.optBoolean("is_latest", true)) {
				bundle.putBoolean(MIDDLE_UPDATE_ENFORCE,
						middleObj.optBoolean("enforce"));
				bundle.putString(MIDDLE_UPDATE_VERSION,
						middleObj.optString("release_version"));
				bundle.putString(MIDDLE_UPDATE_MESSAGE,
						middleObj.optString("changelog"));
				bundle.putString(MIDDLE_UPDATE_FILEPATH,
						middleObj.optString("link"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return bundle.isEmpty() ? YBOX_NO_NEED_UPDATE : YBOX_HAS_UPDATE;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (isCancelled() || handler == null)
			return;
		Message msg = handler.obtainMessage(result);
		msg.arg1 = checkByUser ? 1 : 0;
		if (bundle != null) {
			msg.setData(bundle);
		}
		handler.sendMessage(msg);
	}

}
