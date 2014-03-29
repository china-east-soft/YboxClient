package cn.cloudchain.yboxclient.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.http.HttpHelper;
import cn.cloudchain.yboxclient.utils.LogUtil;

/**
 * 用于实时更新当前连接网络的网关地址
 * 
 * @author lazzy
 * 
 */
public class ConnectChangeReceiver extends BroadcastReceiver {
	private final String TAG = ConnectChangeReceiver.class.getSimpleName();
	private WifiManager wifiManager;

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			return;
		}
		LogUtil.i(TAG, "connecitivity change");
		// 当前没有网络可用
		if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,
				false)) {
			MyApplication.getInstance().gateway = "";
			LogUtil.i(TAG, "no connectivity");
			return;
		}

		NetworkInfo activeNetInfo = intent
				.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		// 当前无可用网络
		if (activeNetInfo == null || !activeNetInfo.isConnectedOrConnecting()) {
			MyApplication.getInstance().gateway = "";
			LogUtil.i(TAG, "no active connectivity");
			return;
		}

		if (wifiManager == null) {
			wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null) {
			MyApplication.getInstance().gateway = HttpHelper
					.getGateway(context);
			LogUtil.i(TAG,
					"has active connectivity = "
							+ MyApplication.getInstance().gateway);
		}

	}

}
