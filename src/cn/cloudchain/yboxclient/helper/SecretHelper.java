package cn.cloudchain.yboxclient.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.utils.EncryptUtil;
import cn.cloudchain.yboxclient.utils.Util;

public class SecretHelper {
	private static SecretHelper instance;
	private String privateKey = "yunmao";

	private SecretHelper() {
	}

	public static SecretHelper getInstance() {
		if (instance == null)
			instance = new SecretHelper();
		return instance;
	}

	/**
	 * 根据设备唯一标示构造MAC地址，如果位数不够使用随机数来代替
	 * 
	 * @return
	 */
	public String getMacLikeString() {
		String deviceId = Util.getIMEI(MyApplication.getAppContext());
		StringBuilder builder = new StringBuilder(18);
		int j = 0;
		for (int i = 0; i < deviceId.length() && j < 12; i++) {
			char indexChar = deviceId.charAt(i);
			boolean isValid = (indexChar >= '0' && indexChar <= '9')
					|| (indexChar >= 'a' && indexChar <= 'f')
					|| (indexChar >= 'A' && indexChar <= 'F');
			if (isValid) {
				builder.append(indexChar);
				if (j % 2 > 0 && j < 10) {
					builder.append(':');
				}
				j++;
			}
		}

		if (j >= 12) {
			return builder.toString();
		}

		String baseRandom = "0123456789abcdefABCDEF";
		int randomSize = baseRandom.length();
		Random random = new Random();
		for (; j < 12; j++) {
			int index = random.nextInt(randomSize);
			builder.append(baseRandom.charAt(index));
			if (j % 2 > 0 && j < 10) {
				builder.append(':');
			}
		}

		return builder.toString().toLowerCase(Locale.getDefault());
	}

	public String getAuthResult(String timestamp, String mac) {
		String macFilter = getFilterResult(mac);
		StringBuilder macBuilder = new StringBuilder(macFilter);
		macBuilder.reverse();
		String last = macBuilder.substring(6);
		macBuilder.delete(6, macBuilder.length());
		macBuilder.insert(0, last);

		String md5Time = EncryptUtil.getMD5String(timestamp);

		List<WeightBean> weightMapArray = new ArrayList<WeightBean>(12);
		for (int i = 0; i < 12; i++) {
			int weight = Integer.parseInt(md5Time.substring(2 * i, 2 * i + 2),
					16);
			char value = macBuilder.charAt(i);
			weightMapArray.add(new WeightBean(weight, value));
		}

		Collections.sort(weightMapArray);
		macBuilder = new StringBuilder(12);
		for (WeightBean bean : weightMapArray) {
			macBuilder.append(bean.value);
		}

		String result = EncryptUtil.hmacSha1(macBuilder.toString(), privateKey)
				.trim();
		return result;
	}

	private String getFilterResult(String mac) {
		StringBuilder builder = new StringBuilder(12);
		for (int i = 0; i < mac.length() && builder.length() < 12; i++) {
			int ascii = mac.charAt(i);
			boolean isValid = (ascii >= 48 && ascii <= 57)
					|| (ascii >= 65 && ascii <= 90)
					|| (ascii >= 97 && ascii <= 122);
			if (isValid) {
				builder.append(mac.charAt(i));
			}
		}
		return builder.toString();
	}

	private class WeightBean implements Comparable<WeightBean> {
		private int weight;
		private char value;

		private WeightBean(int weight, char value) {
			this.weight = weight;
			this.value = value;
		}

		@Override
		public int compareTo(WeightBean another) {
			if (another == null) {
				return 1;
			}
			int result = 0;
			int anotherWeight = another.weight;
			if (this.weight > anotherWeight) {
				result = 1;
			} else if (this.weight < anotherWeight) {
				result = -1;
			}
			return result;
		}
	}

}
