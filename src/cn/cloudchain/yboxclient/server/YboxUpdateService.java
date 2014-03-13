package cn.cloudchain.yboxclient.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

public class YboxUpdateService extends Service {
	private final String TAG = YboxUpdateService.class.getSimpleName();

	public static final String ACTION_RECEIVED_RESULT = "cn.cloudchain.yboxclient.RECEIVED_RESULT";
	public static final String BUNDLE_MESSAGE = "message";

	private final IBinder binder = new YboxUpdateBinder();
	private static final String GROUP_HOST = "230.0.0.1";
	private static final int GROUP_PORT = 7777;

	private MulticastSocket multicastSocket;
	private MulticastLock multiLock = null;
	private boolean stopListen = false;

	@Override
	public IBinder onBind(Intent intent) {
		stopListen = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				startListen();
			}
		}).start();
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopListen = true;
		if (multicastSocket != null && !multicastSocket.isClosed()) {
			multicastSocket.close();
			multicastSocket = null;
		}
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		if (multiLock != null) {
			multiLock.release();
		}
		super.onDestroy();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multiLock = wifi.createMulticastLock(TAG);
		multiLock.acquire();
	}

	private void dealWithData(String message) {
		if (TextUtils.isEmpty(message)) {
			return;
		}
		Intent intent = new Intent(ACTION_RECEIVED_RESULT);
		intent.putExtra(BUNDLE_MESSAGE, message);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void startListen() {
		if (multicastSocket == null) {
			generateMulticastSocket();
		}
		if (multicastSocket != null) {
			byte[] buf = new byte[512];
			DatagramPacket pack = new DatagramPacket(buf, buf.length);
			while (!stopListen) {
				try {
					multicastSocket.receive(pack);
					String received = new String(pack.getData()).trim();
					System.out.println("received: " + received);
					dealWithData(received);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void generateMulticastSocket() {
		try {
			multicastSocket = new MulticastSocket(GROUP_PORT);
			InetAddress inetAddress = InetAddress.getByName(GROUP_HOST);
			multicastSocket.setReuseAddress(true);
			multicastSocket.joinGroup(inetAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class YboxUpdateBinder extends Binder {
		public YboxUpdateService getService() {
			return YboxUpdateService.this;
		}
	}

}
