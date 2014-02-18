package cn.cloudchain.yboxclient.helper;

public abstract class ApStatusHandler<T> extends WeakHandler<T> {
	public static final int WIFI_MODE_CHANGE = 0;
	public static final int TV_MODE_CHANGE = 1;

	public ApStatusHandler(T owner) {
		super(owner);
	}

}
