package cn.cloudchain.yboxclient.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.R;

public class Util {
	public final static String TAG = Util.class.getSimpleName();
	private static Toast toast;

	public static void toaster(Context context, int stringId, int duration) {
		if (context == null)
			return;
		if (toast != null) {
			LogUtil.i(TAG, "toast cancel");
			toast.cancel();
			toast = null;
		}
		toast = Toast.makeText(context, stringId, duration);
		toast.show();
	}

	public static void toaster(Context context, int stringId) {
		toaster(context, stringId, Toast.LENGTH_SHORT);
	}

	public static void toaster(int stringId) {
		toaster(MyApplication.getAppContext(), stringId, Toast.LENGTH_SHORT);
	}

	public static void toaster(CharSequence message, BufferType bufferType) {
		toaster(MyApplication.getAppContext(), message, bufferType);
	}

	public static void toaster(Context context, CharSequence message,
			BufferType bufferType) {
		if (context != null) {
			if (toast == null) {
				toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			}
			if (bufferType != null) {
				TextView tv = (TextView) toast.getView().findViewById(
						android.R.id.message);
				tv.setText(Html.fromHtml(message.toString()), bufferType);
			} else {
				toast.setText(message);
			}
			toast.show();
		}
	}

	public static String getString(int resId, String defaultStr) {
		String str = "";
		try {
			str = MyApplication.getAppContext().getString(resId);
		} catch (Exception e) {
			str = defaultStr;
		}
		return str;
	}

	public static File URItoFile(String URI) {
		return new File(Uri.decode(URI).replace("file://", ""));
	}

	public static String URItoFileName(String URI) {
		return URItoFile(URI).getName();
	}

	public static String readAsset(String assetName, String defaultS) {
		try {
			InputStream is = MyApplication.getAppContext().getResources()
					.getAssets().open(assetName);
			BufferedReader r = new BufferedReader(new InputStreamReader(is,
					"UTF8"));
			StringBuilder sb = new StringBuilder();
			String line = r.readLine();
			if (line != null) {
				sb.append(line);
				line = r.readLine();
				while (line != null) {
					sb.append('\n');
					sb.append(line);
					line = r.readLine();
				}
			}
			return sb.toString();
		} catch (IOException e) {
			return defaultS;
		}
	}

	/**
	 * Convert time to a string
	 * 
	 * @param millis
	 *            e.g.time/length from file
	 * @return formated string (hh:)mm:ss
	 */
	public static String millisToString(long millis) {
		boolean negative = millis < 0;
		millis = java.lang.Math.abs(millis);

		millis /= 1000;
		int sec = (int) (millis % 60);
		millis /= 60;
		int min = (int) (millis % 60);
		millis /= 60;
		int hours = (int) millis;

		String time;
		DecimalFormat format = (DecimalFormat) NumberFormat
				.getInstance(Locale.US);
		format.applyPattern("00");
		if (millis > 0) {
			time = (negative ? "-" : "") + hours + ":" + format.format(min)
					+ ":" + format.format(sec);
		} else {
			time = (negative ? "-" : "") + min + ":" + format.format(sec);
		}
		return time;
	}

	public static int convertPxToDp(int px) {
		WindowManager wm = (WindowManager) MyApplication.getAppContext()
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		float logicalDensity = metrics.density;
		int dp = Math.round(px / logicalDensity);
		return dp;
	}

	public static int convertDpToPx(int dp) {
		return Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dp, MyApplication.getAppContext()
						.getResources().getDisplayMetrics()));
	}

	public static boolean isPhone() {
		TelephonyManager manager = (TelephonyManager) MyApplication
				.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isRunning(String serviceName) {
		ActivityManager myAM = (ActivityManager) MyApplication.getAppContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningServices = (ArrayList<RunningServiceInfo>) myAM
				.getRunningServices(Integer.MAX_VALUE);

		if (runningServices != null) {
			for (int i = 0; i < runningServices.size(); i++)// 循环枚举对比
			{
				if (runningServices.get(i).service.getClassName().equals(
						serviceName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean compLocalServerVersion(String serVerName,
			int serVerCode, String curVerName, int curVerCode) {
		StringTokenizer newVerName = new StringTokenizer(serVerName, ".");
		StringTokenizer oldVerName = new StringTokenizer(curVerName, ".");

		while (newVerName.hasMoreTokens() && oldVerName.hasMoreTokens()) {
			String newStr = newVerName.nextToken();
			String oldStr = oldVerName.nextToken();
			int newVer = Integer.parseInt(newStr);
			int oldVer = Integer.parseInt(oldStr);
			if (oldVer > newVer) {
				return false;
			} else if (newVer > oldVer) {
				return true;
			}
		}

		if (serVerCode > curVerCode) {
			return true;
		} else {
			return false;
		}
	}

	public static int getVerCode(Context context) {
		int verCode = -1;

		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			if (info != null) {
				verCode = info.versionCode;
			}
		} catch (NameNotFoundException e) {
			LogUtil.e(TAG, e.getMessage());
		}
		return verCode;
	}

	public static String getVerName(Context context) {
		String verName = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			if (info != null) {
				verName = info.versionName;
			}
		} catch (NameNotFoundException e) {
			LogUtil.e(TAG, e.getMessage());
		}

		return verName;
	}

	public static String getAppName(Context context) {
		String verName = context.getResources().getText(R.string.app_name)
				.toString();

		LogUtil.i(TAG, "getAppName = " + verName);
		return verName;
	}

	/**
	 * 返回文件/目录路径，如果SD卡和Cache都不可写，或者文件/路径创建失败，则返回null
	 * 
	 * @param fileName
	 *            文件名/目录，如Log/crash，Log/
	 * @param createIfNotExist
	 *            路径不存在时是否需创建
	 * @return
	 */
	public static String getFilePath(String fileName, boolean createIfNotExist) {
		StringBuilder builder = new StringBuilder(20);
		// if (Environment.getExternalStorageState().equals(
		// Environment.MEDIA_MOUNTED)) {
		// builder.append(Environment.getExternalStorageDirectory());
		// builder.append("/ShiWangMo/");
		// builder.append(fileName);
		// } else {
		File cache = MyApplication.getAppContext().getFilesDir();
		if (cache.canWrite()) {
			builder.append(cache.getAbsolutePath());
			builder.append('/');
			builder.append(fileName);
		}
		// }

		String filePath = builder.toString();
		if (!TextUtils.isEmpty(filePath) && createIfNotExist) {
			File file = new File(filePath);
			if (!file.isDirectory()) {
				file = file.getParentFile();
			}
			if (!file.exists() && !file.mkdirs()) {
				filePath = null;
			}
		}
		LogUtil.i(TAG, "path = " + filePath);
		return filePath;
	}

	public static String getDownloadPath() {
		StringBuilder sb = new StringBuilder(20);
		// when the sdcard is not available, return null, because apk in system
		// can not always install correctly.
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			sb = new StringBuilder();
			sb.append(Environment.getExternalStorageDirectory());
			sb.append("/ShiWangMo/Apk");
			LogUtil.i(TAG, "path = " + sb.toString());
		}

		return sb.toString();
	}

	public static void installApk(Context mContext, String absoluteFilePath) {
		if (absoluteFilePath == null) {
			// Util.toaster(mContext, R.string.file_not_exist);
			return;
		}
		File file = new File(absoluteFilePath);
		LogUtil.i(TAG, "install path = " + absoluteFilePath);
		LogUtil.i(TAG, "install path = " + Uri.fromFile(file).toString());
		if (file.exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
			mContext.startActivity(intent);
		} else {
			// Util.toaster(mContext, R.string.file_not_exist);
		}
	}

	/**
	 * 获取设备号，若获取失败，则默认为1199223366
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {
		String uuid = PreferenceUtil.getString("UUID", "");
		if (!TextUtils.isEmpty(uuid)) {
			return uuid;
		}

		// 获取MAC地址
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		uuid = info.getMacAddress();

		if (!TextUtils.isEmpty(uuid)) {
			PreferenceUtil.putString("UUID", uuid);
			return uuid;
		}

		// 获取设备ID
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telManager != null) {
			uuid = telManager.getDeviceId();
		}

		if (!TextUtils.isEmpty(uuid)) {
			PreferenceUtil.putString("UUID", uuid);
			return uuid;
		}

		// 获取random
		String androidId = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		if ("9774d56d682e549c".equals(androidId)) {
			uuid = UUID.randomUUID().toString();
		} else {
			uuid = UUID.nameUUIDFromBytes(androidId.getBytes()).toString();
		}
		PreferenceUtil.putString("UUID", uuid);
		return uuid;
	}

	/**
	 * 获取某个asset文件是否存在
	 * 
	 * @param fileName
	 *            ，为空时返回false
	 * @return
	 */
	public static boolean isAssetFileExists(String fileName) {
		boolean isExist = false;
		try {
			if (!TextUtils.isEmpty(fileName)) {
				AssetManager am = MyApplication.getAppContext().getResources()
						.getAssets();
				String[] names = am.list("");
				for (String name : names) {
					if (name.equals(fileName.trim())) {
						isExist = true;
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isExist;
	}

	/**
	 * 显示碎片； 根据fragmentTag来判断碎片是否已创建，而后进行不同的操作；
	 * 防止屏幕旋转时创建多了相同的碎片，致使之后的replace操作无效或误操作；
	 * 
	 * @param fm
	 *            不能为空
	 * @param container
	 *            碎片所在的容器id，如R.id.container
	 * @param fragment
	 *            将要显示的碎片，不能为空
	 * @param fragmentTag
	 *            碎片的TAG
	 */
	public static void showFragment(FragmentManager fm, int container,
			Fragment fragment, String fragmentTag) {
		if (fm == null || fragment == null || container == 0) {
			return;
		}

		Fragment current = fm.findFragmentById(container);
		if (current != null && !current.getTag().equals(fragmentTag)) {
			return;
		}
		// 防止屏幕旋转时创建多了相同的碎片，致使之后的replace操作无效或误操作
		FragmentTransaction ft = fm.beginTransaction();
		Fragment exist = fm.findFragmentByTag(fragmentTag);
		if (exist == null || !exist.isAdded()) {
			ft.add(container, fragment, fragmentTag);
		} else {
			ft.attach(exist);
		}
		ft.commit();
	}

	/**
	 * 返回移除引号后的SSID，针对某些山寨机，获取的SSID格式不一致，有时有引号，有时没有
	 * 
	 * @param ssid
	 * @return
	 */
	public static String removeQuotOfSSID(String ssid) {
		if (TextUtils.isEmpty(ssid))
			return ssid;
		int start = 0;
		int end = ssid.length();
		if (ssid.charAt(0) == '"') {
			start = 1;
		}
		if (ssid.charAt(end - 1) == '"') {
			--end;
		}
		return ssid.substring(start, end);
	}

	/**
	 * 根据SSID判断是否为有效的AP
	 * 
	 * @param ssid
	 * @return
	 */
	public static boolean isValidApSSID(String ssid) {
		if (TextUtils.isEmpty(ssid)) {
			return false;
		}
		boolean flag = false;
		String lowcaseSSID = ssid.toLowerCase(Locale.getDefault());
		if (lowcaseSSID.contains("y-box")) {
			flag = true;
		}
		return flag;
	}

}
