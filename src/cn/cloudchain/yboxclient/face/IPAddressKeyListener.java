package cn.cloudchain.yboxclient.face;

import android.text.InputType;
import android.text.method.NumberKeyListener;

/**
 * 处理IP地址相关的键盘输入，只能允许数字和.的输入
 * 
 * @author lazzy
 * 
 */
public class IPAddressKeyListener extends NumberKeyListener {
	public static final int TYPE_NUMBER_FLAG_IP = 0x00004000;
	public static final char[] CHARACTERS = new char[] { '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', '.' };

	private static IPAddressKeyListener mInstance;

	@Override
	public int getInputType() {
		return InputType.TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_IP;
	}

	@Override
	protected char[] getAcceptedChars() {
		return CHARACTERS;
	}

	public static IPAddressKeyListener getInstance() {
		if (mInstance == null) {
			mInstance = new IPAddressKeyListener();
		}
		return mInstance;
	}

}
