/*****************************************************************************
 * Util.java
 *****************************************************************************
 * Copyright © 2011-2012 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package cn.cloudchain.yboxclient.helper;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import cn.cloudchain.yboxclient.MyApplication;

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
