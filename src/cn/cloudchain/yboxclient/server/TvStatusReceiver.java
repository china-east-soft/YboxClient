package cn.cloudchain.yboxclient.server;

import cn.cloudchain.yboxclient.helper.TVStatusHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TvStatusReceiver extends BroadcastReceiver {
//	public final static String ACTION_STATUS_WRONG = "cn.cloudchain.yboxclient.STATUS_WRONG";
//	public final static String ACTION_STATUS_COMPLETE = "cn.cloudchain.yboxclient.STATUS_COMPLETE";
	public final static String ACTION_SWITCH_STATE_CHANGE = "cn.cloudchain.yboxclient.SWITCH_STATE_CHANGE";
	public final static String ACTION_REMOTE_EPG_CHANGE = "cn.cloudchain.yboxclient.REMOTE_EPG_CHANGE";

	private TVStatusHandler<?> handler;

	public TvStatusReceiver(TVStatusHandler<?> handler) {
		this.handler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ACTION_SWITCH_STATE_CHANGE.equals(action)) {
			handler.sendEmptyMessage(TVStatusHandler.SWITCH_STATE_CHANGE);
		} else if (ACTION_REMOTE_EPG_CHANGE.equals(action)) {
			handler.sendEmptyMessage(TVStatusHandler.REMOTE_EPG_CHANGE);
		}
	}

}
