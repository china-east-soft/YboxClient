package cn.cloudchain.yboxclient.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxcommon.bean.Constants;

public class BroadcastService extends Service {
	final String TAG = BroadcastService.class.getSimpleName();
	public static final String ACTION_RECEIVED_RESULT = "cn.cloudchain.yboxclient.RECEIVED_RESULT";
	public static final String BUNDLE_MESSAGE = "message";

	private MulticastSocket multicastSocket;
	private MulticastLock multiLock = null;

	private boolean stopListen = false;
	private ExecutorService executor = null;
	private IBinder mBinder = new Binder();

	@Override
	public IBinder onBind(Intent intent) {
		if (mBinder == null) {
			mBinder = new Binder();
		}
		stopListen = false;
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
		}
		if (!executor.isShutdown()) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					startBroadcastListener();
				}
			});
		}

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multiLock = wifi.createMulticastLock(TAG);
		multiLock.acquire();
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogUtil.d(TAG, "---unBind---");
		stopListen = true;
		if (multiLock != null) {
			multiLock.release();
		}
		if (multicastSocket != null && !multicastSocket.isClosed()) {
			multicastSocket.close();
			multicastSocket = null;
		}
		if (executor != null && !executor.isShutdown()) {
			executor.shutdownNow();
			executor = null;
		}
		return super.onUnbind(intent);
	}

	private void handleReceiveMessage(String message) {
		if (TextUtils.isEmpty(message))
			return;

		try {
			JSONObject obj = new JSONObject(message);
			// 如果包含"url"字段，说明是下载进度信息
			if (obj.has("url")) {
				Intent intent = new Intent(ACTION_RECEIVED_RESULT);
				intent.putExtra(BUNDLE_MESSAGE, message);
				LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				return;
			}
			int oldType = MyApplication.getInstance().connType;
			int newType = obj.optInt(Constants.Udp.CONN_TYPE);
			if (oldType != newType) {
				MyApplication.getInstance().connType = newType;
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(ApStatusReceiver.ACTION_WIFI_MODE_CHANGE));
			}

			long oldTime = MyApplication.getInstance().wifiClientUpdateTime;
			long newTime = obj.getLong(Constants.Udp.CLIENTS_UPDATE_TIME);
			if (oldTime != newTime) {
				MyApplication.getInstance().wifiClientUpdateTime = newTime;
				LocalBroadcastManager
						.getInstance(this)
						.sendBroadcast(
								new Intent(
										ApStatusReceiver.ACTION_WIFI_CLIENTS_CHANGE));
			}

			int oldBattery = MyApplication.getInstance().battery;
			int newBattery = obj.optInt(Constants.Udp.BATTERY);
			if (oldBattery != newBattery) {
				MyApplication.getInstance().battery = newBattery;
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(ApStatusReceiver.ACTION_BATTERY_CHANGE));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void startBroadcastListener() {
		if (multicastSocket == null) {
			generateMulticastSocket();
		}
		if (multicastSocket != null) {
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			while (!stopListen) {
				try {
					multicastSocket.receive(packet);
					String message = new String(packet.getData(), 0,
							packet.getLength());
					LogUtil.i(TAG, "message = " + message);
					handleReceiveMessage(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void generateMulticastSocket() {
		try {
			multicastSocket = new MulticastSocket(Constants.GROUP_PORT);
			InetAddress inetAddress = InetAddress
					.getByName(Constants.GROUP_HOST);
			multicastSocket.setReuseAddress(true);
			multicastSocket.joinGroup(inetAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
