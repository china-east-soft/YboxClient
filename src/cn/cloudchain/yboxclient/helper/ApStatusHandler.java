package cn.cloudchain.yboxclient.helper;

public abstract class ApStatusHandler<T> extends WeakHandler<T> {
	public static final int WIFI_MODE_CHANGE = 0;
	public static final int TV_MODE_CHANGE = 1;
	public static final int HOTSPOT_CLIENT_CHANGE = 2;
	public static final int BATTERY_LOW_CHANGE = 3;

	public ApStatusHandler(T owner) {
		super(owner);
	}

}
