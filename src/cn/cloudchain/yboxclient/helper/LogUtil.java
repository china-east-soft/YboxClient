package cn.cloudchain.yboxclient.helper;

import android.util.Log;
import cn.cloudchain.yboxclient.BuildConfig;

/**
 * 管理日志
 * 
 * @author lazzy
 * 
 */
public class LogUtil {
	public static void i(String tag, String message) {
		if (BuildConfig.DEBUG) {
			Log.i(tag, message);
		}
	}

	public static void e(String tag, String message) {
		if (BuildConfig.DEBUG) {
			Log.e(tag, message);
		}
	}

	public static void v(String tag, String message) {
		if (BuildConfig.DEBUG) {
			Log.v(tag, message);
		}
	}

	public static void d(String tag, String message) {
		if (BuildConfig.DEBUG) {
			Log.d(tag, message);
		}
	}

	public static void w(String tag, String message) {
		if (BuildConfig.DEBUG) {
			Log.w(tag, message);
		}
	}

}
