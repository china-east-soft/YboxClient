package cn.cloudchain.yboxclient;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
	private static MyApplication instance;
	public String wifiMode = "";

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
