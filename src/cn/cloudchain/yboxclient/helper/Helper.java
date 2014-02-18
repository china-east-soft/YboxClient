package cn.cloudchain.yboxclient.helper;

import java.io.IOException;
import java.net.InetAddress;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class Helper {
	private static Helper instance;

	public static Helper getInstance() {
		if (instance == null)
			instance = new Helper();
		return instance;
	}

	private Helper() {
	}

	public String getGateway(Context mContext) {
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dInfo = wifi.getDhcpInfo();
		String gateway = long2ip(dInfo.gateway);
		return gateway;
	}

	private String long2ip(int ip) {
		int[] b = new int[4];
		b[0] = (int) ((ip >> 24) & 0xff);
		b[1] = (int) ((ip >> 16) & 0xff);
		b[2] = (int) ((ip >> 8) & 0xff);
		b[3] = (int) (ip & 0xff);
		String x;
		x = Integer.toString(b[3]) + "." + Integer.toString(b[2]) + "."
				+ Integer.toString(b[1]) + "." + Integer.toString(b[0]);
		return x;
	}

	public InetAddress getBroadcastAddress(Context context) throws IOException {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

}
