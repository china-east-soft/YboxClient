package cn.cloudchain.yboxclient.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import cn.cloudchain.yboxclient.MyApplication;

public class PreferenceUtil {
	public static final String LOCAL_EPG = "local_epg";
	public static final String LOCAL_EPG_CREATE_TIME = "local_epg_create_time";

	public static final String PREF_CLIENT_PHONE = "client_phone";
	public static final String PREF_ACCOUNT_ID = "account_id";
	public static final String PREF_CLIENT_ID = "client_id";
	public static final String PREF_CLIENT_AUTH_TOKEN = "client_auth_token";

	public static final String PREF_LISENCE_CHECK_STATE = "lisence_check_state";
	public static final String PREF_PHONE_VERIFY_STARTTIME = "phone_verify_starttime";
	public static final String PREF_TEMP_PHONE = "temp_phone";

	private static SharedPreferences prefSetting = PreferenceManager
			.getDefaultSharedPreferences(MyApplication.getAppContext());

	public static String getString(String key, String defaultValue) {
		return prefSetting.getString(key, defaultValue);
	}

	public static void putString(String key, String value) {
		Editor editor = prefSetting.edit();
		editor.putString(key, value);
		editor.commit();
		editor = null;
	}

	public static void remove(String key) {
		Editor editor = prefSetting.edit();
		editor.remove(key);
		editor.commit();
		editor = null;
	}

	public static int getInt(String key, int defaultValue) {
		return prefSetting.getInt(key, defaultValue);
	}

	public static void putInt(String key, int value) {
		Editor editor = prefSetting.edit();
		editor.putInt(key, value);
		editor.commit();
		editor = null;
	}

	public static long getLong(String key, long defaultValue) {
		return prefSetting.getLong(key, defaultValue);
	}

	public static void putLong(String key, long value) {
		Editor editor = prefSetting.edit();
		editor.putLong(key, value);
		editor.commit();
		editor = null;
	}

	public static void putBoolean(String key, boolean value) {
		Editor editor = prefSetting.edit();
		editor.putBoolean(key, value);
		editor.commit();
		editor = null;
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		return prefSetting.getBoolean(key, defaultValue);
	}
}
