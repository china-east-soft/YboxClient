package cn.cloudchain.yboxclient.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import cn.cloudchain.yboxclient.MyApplication;

/**
 * 封装一些公用的方法
 * 
 * @author lazzy
 * 
 */
public class Helper {
	private static Helper instance;
	private Context appContext;

	public static Helper getInstance() {
		if (instance == null)
			instance = new Helper();
		return instance;
	}

	private Helper() {
		appContext = MyApplication.getAppContext();
	}

	/**
	 * 获取设备网关地址
	 * 
	 * @param context
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * 获取设备MAC地址
	 * 
	 * @param context
	 * @return
	 */
	public String getDevicesMac(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * 将字节流转换成字符串
	 * 
	 * @param inStream
	 * @return 不可能返回null
	 */
	public String transStreamToString(InputStream inStream) {
		StringBuilder builder = new StringBuilder();
		if (inStream != null) {

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inStream));
				String line = "";
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return builder.toString();
	}

	/**
	 * 根据资源获取字符串
	 * 
	 * @param resId
	 *            不存在时返回""
	 * @return
	 */
	public String getString(int resId) {
		String str = "";

		if (resId > 0) {
			try {
				str = appContext.getResources().getString(resId);
			} catch (Exception e) {

			}
		}
		return str;
	}

}
