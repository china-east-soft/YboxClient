package cn.cloudchain.yboxclient;

import android.app.Application;
import android.content.Context;
import cn.cloudchain.yboxclient.http.HttpHelper;

import com.baidu.frontia.FrontiaApplication;

public class MyApplication extends Application {
	private static MyApplication instance;
	public int connType = 0;
	public long wifiClientUpdateTime = 0L;
	public int battery = -1;

	public String epgUpdateTime;
	public String gateway = "";

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		gateway = HttpHelper.getGateway(this);
		FrontiaApplication.initFrontiaApplication(this);
	}

	public static Context getAppContext() {
		return instance;
	}

	public static MyApplication getInstance() {
		return instance;
	}
}
