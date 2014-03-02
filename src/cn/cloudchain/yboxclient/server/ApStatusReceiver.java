package cn.cloudchain.yboxclient.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.cloudchain.yboxclient.helper.ApStatusHandler;

public class ApStatusReceiver extends BroadcastReceiver {
	public static final String ACTION_WIFI_MODE_CHANGE = "cn.cloudchain.yboxclient.WIFI_MODE_CHANGE";
	public static final String ACTION_TV_MODE_CHANGE = "cn.cloudchain.yboxclient.WIFI_TV_CHANGE";
	public static final String ACTION_WIFI_CLIENTS_CHANGE = "cn.cloudchain.yboxclient.WIFI_CLIENTS_CHANGE";
	public static final String ACTION_BATTERY_LOW_CHANGE = "cn.cloudchain.yboxclient.BATTERY_LOW_CHANGE";

	private ApStatusHandler<?> handler;

	public ApStatusReceiver(ApStatusHandler<?> handler) {
		this.handler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (handler == null)
			return;

		String action = intent.getAction();
		if (ACTION_WIFI_MODE_CHANGE.equals(action)) {
			handler.sendEmptyMessage(ApStatusHandler.WIFI_MODE_CHANGE);
		} else if (ACTION_WIFI_CLIENTS_CHANGE.equals(action)) {
			handler.sendEmptyMessage(ApStatusHandler.HOTSPOT_CLIENT_CHANGE);
		} else if (ACTION_BATTERY_LOW_CHANGE.equals(action)) {
			handler.sendEmptyMessage(ApStatusHandler.BATTERY_LOW_CHANGE);
		}

	}

}
