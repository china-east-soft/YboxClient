package cn.cloudchain.yboxclient;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
	private static MyApplication instance;
	public int connType = 0;
	public long wifiClientUpdateTime = 0L;
	public boolean batteryLow = false;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public static Context getAppContext() {
		return instance;
	}

	public static MyApplication getInstance() {
		return instance;
	}
}
