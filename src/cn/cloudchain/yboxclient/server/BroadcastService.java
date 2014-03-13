package cn.cloudchain.yboxclient.server;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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

public class BroadcastService extends Service {
	final String TAG = BroadcastService.class.getSimpleName();

	private final int PORT_LISTEN = 12345;
	private final int TIMEOUT_MS = 6000;

	private boolean stopListen = false;
	private ExecutorService executor = null;
	private MulticastLock multiLock = null;
	private DatagramSocket socket = null;
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
		if (socket != null && !socket.isClosed()) {
			socket.close();
			socket = null;
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
			int oldType = MyApplication.getInstance().connType;
			int newType = obj.optInt("conn");
			if (oldType != newType) {
				MyApplication.getInstance().connType = newType;
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(ApStatusReceiver.ACTION_WIFI_MODE_CHANGE));
			}

			long oldTime = MyApplication.getInstance().wifiClientUpdateTime;
			long newTime = obj.getLong("clients_update_time");
			if (oldTime != newTime) {
				MyApplication.getInstance().wifiClientUpdateTime = newTime;
				LocalBroadcastManager
						.getInstance(this)
						.sendBroadcast(
								new Intent(
										ApStatusReceiver.ACTION_WIFI_CLIENTS_CHANGE));
			}

			boolean oldBatteryLow = MyApplication.getInstance().batteryLow;
			boolean newBatteryLow = obj.optBoolean("battery_low");
			if (oldBatteryLow != newBatteryLow) {
				MyApplication.getInstance().batteryLow = newBatteryLow;
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(ApStatusReceiver.ACTION_BATTERY_LOW_CHANGE));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void startBroadcastListener() {
		if (socket == null)
			generateSocket();

		while (!stopListen) {
			byte[] buf = new byte[1024];
			try {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String message = new String(packet.getData(), 0,
						packet.getLength());
				LogUtil.i(TAG, "message = " + message);
				handleReceiveMessage(message);
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void generateSocket() {
		try {
			socket = new DatagramSocket(PORT_LISTEN);
			socket.setReuseAddress(true);
			socket.setBroadcast(true);
//			socket.setSoTimeout(TIMEOUT_MS);
		} catch (BindException e) {
			LogUtil.e(TAG, "has socket bind to the port");
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}
