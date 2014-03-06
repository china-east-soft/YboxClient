package cn.cloudchain.yboxclient.helper;

public abstract class TVStatusHandler<T> extends WeakHandler<T> {
	public static final int SWITCH_STATE_CHANGE = 0;
	public static final int REMOTE_EPG_CHANGE = 1;

	public TVStatusHandler(T owner) {
		super(owner);
	}

}
