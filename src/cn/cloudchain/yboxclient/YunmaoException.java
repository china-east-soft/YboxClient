package cn.cloudchain.yboxclient;

public class YunmaoException extends Exception {
	private static final long serialVersionUID = -5149384441948266075L;
	public static final int ERROR_CODE_NONE = -2;
	public static final int ERROR_CODE_DEFAULT = -1;
	public static final int ERROR_CODE_AUTH_FAIL = 3;

	public static final int STATUS_CODE_REQUEST_FAIL = -1;
	private int statusCode = STATUS_CODE_REQUEST_FAIL;
	private int errorCode = ERROR_CODE_DEFAULT;

	public YunmaoException(Exception cause) {
		super(cause);
	}

	public YunmaoException(String msg, int statusCode, int errorCode) {
		super(msg);
		this.statusCode = statusCode;
		this.errorCode = errorCode;
	}

	public YunmaoException(String msg, int statusCode) {
		super(msg);
		this.statusCode = statusCode;
	}

	public YunmaoException(String msg) {
		super(msg);
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public int getErrorCode() {
		return this.errorCode;
	}
}