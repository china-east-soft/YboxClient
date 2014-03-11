package cn.cloudchain.yboxclient.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.http.MyHttpClient;
import cn.cloudchain.yboxclient.utils.EncryptUtil;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.PreferenceUtil;
import cn.cloudchain.yboxclient.utils.Util;

/**
 * 处理数据相关的内容
 * 
 * @author lazzy
 * 
 */
public class ServerHelper {
	public static final String TAG = ServerHelper.class.getSimpleName();

	static {
		int level = 1;
		switch (level) {
		case 0:
			HOST = "cloudchain.cn";
			break;
		case 1:
			HOST = "cloudchain.co";
			break;
		case 2:
			HOST = "cloudchain.com.cn";
			break;
		}
	}
	public static String HOST;
	private static final String URL_BASE_API = "http://" + HOST + "/api/v3/";
	private static final String PATH_POST_VERIFY = "messages/verify_code.json";
	private static final String PATH_POST_VERIFY_PHONE = "users/signup.json";
	private static final String PATH_POST_AUTHTOKEN = "auth_tokens.json";
	private static final String PATH_POST_LOGS = "client_logs.json";
	private static final String PATH_GET_APP_VERSION = "apps/version.json";
	private static final String PATH_POST_DEVICE_BIND = "users/bind.json";
	private static final String PATH_POST_LOGIN = "users/login.json";

	private static final String BASIC_USERNAME = "yunmao";
	private static final String BASIC_PASSWORD = "china-east";

	private static ServerHelper instance;
	private static int MAX_RETRY_COUNT = 3;

	// private Lock authLock = new ReentrantLock();
	// private static boolean isAuthRunning = false;

	/**
	 * mContext is null
	 * 
	 * @param mContext
	 * @return
	 */
	public static ServerHelper getInstance() {
		if (instance == null)
			instance = new ServerHelper();
		return instance;
	}

	private ServerHelper() {
	}

	/**
	 * 发送登录请求
	 * @param account
	 * @param pass
	 * @throws YunmaoException
	 */
	public void postForUserLogin(String account, String pass)
			throws YunmaoException {
		if (TextUtils.isEmpty(account) || TextUtils.isEmpty(pass)) {
			throw new YunmaoException("account or password can not be empty");
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile_number", account));
		params.add(new BasicNameValuePair("password", pass));
		retryOperation(params, PATH_POST_LOGIN, MyHttpClient.POST);
	}

	/**
	 * 设备绑定
	 * 
	 * @param deviceMac
	 *            设备MAC地址
	 * @param bind
	 *            true为绑定，false为解绑
	 * @throws YunmaoException
	 */
	public void postForBindDevice(String deviceMac, boolean bind)
			throws YunmaoException {
		if (TextUtils.isEmpty(deviceMac)) {
			throw new YunmaoException("mac can not nu empty");
		}
		int account = PreferenceUtil.getInt(PreferenceUtil.PREF_ACCOUNT_ID, -1);
		if (account < 0) {
			throw new YunmaoException("account not exist");
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mac", deviceMac.toLowerCase()));
		params.add(new BasicNameValuePair("bind", String.valueOf(bind)));
		params.add(new BasicNameValuePair("account_id", String.valueOf(account)));
		retryOperation(params, PATH_POST_DEVICE_BIND, MyHttpClient.POST);
	}

	/**
	 * 向服务端POST日志文件
	 * 
	 * @param logType
	 *            日志类型，方便筛选
	 * @param filePath
	 *            日志文件路径
	 * @throws YunmaoException
	 */
	public void postLogInfo(String logType, String filePath)
			throws YunmaoException {
		if (TextUtils.isEmpty(filePath)) {
			throw new YunmaoException("post file path is null");
		}
		File file = new File(filePath);
		if (!file.exists()) {
			throw new YunmaoException("post file is not exist");
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("client_type", "android"));
		params.add(new BasicNameValuePair("log_file", filePath));
		if (!TextUtils.isEmpty(logType)) {
			params.add(new BasicNameValuePair("log_type", logType));
		}

		retryOperation(params, PATH_POST_LOGS, MyHttpClient.POST_WITH_FILE);
	}

	/**
	 * 检查客户端是否存在更新
	 * 
	 * @throws YunmaoException
	 */
	public void getAppVersions() throws YunmaoException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("current_version", Util
				.getVerName(MyApplication.getAppContext())));
		params.add(new BasicNameValuePair("app_type", "android"));
		retryOperation(params, PATH_GET_APP_VERSION, MyHttpClient.GET);

	}

	/**
	 * 检查终端是否有升级
	 * 
	 * @param isRootImage
	 *            true时检测ROOT IMAGE的升级，false检测中间件的升级
	 * @param version
	 *            版本号
	 * @throws YunmaoException
	 */
	public void getYboxUpdate(final boolean isRootImage, final String version)
			throws YunmaoException {
		// List<NameValuePair> params = new ArrayList<NameValuePair>();
		// params.add(new BasicNameValuePair("current_version", Util
		// .getVerName(MyApplication.getAppContext())));
		// params.add(new BasicNameValuePair("app_type", "android"));
		// retryOperation(params, PATH_GET_APP_VERSION, MyHttpClient.GET);
		throw new YunmaoException(
				"{\"is_latest\":false, \"release_version\":\"2.3.1\", \"link\":\"http://a.hiphotos.baidu.com/image/w%3D2048/sign=e9e1e60e5b82b2b7a79f3ec40595caef/b58f8c5494eef01f728c66cfe2fe9925bc317d1b.jpg\"}",
				-1, YunmaoException.ERROR_CODE_NONE);
	}

	/**
	 * 向服务端请求获取手机验证码，只有在请求失败/手机号码不正确的情况下才会抛出异常
	 * 
	 * @param phone_num
	 *            手机号码
	 * @throws YunmaoException
	 *             statusCode范围为HttpStatus中的返回码范围，常见错误401，400，-1 errorCode
	 */
	public void postForVerify(String phone_num) throws YunmaoException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile_number", phone_num));

		retryOperation(params, PATH_POST_VERIFY, MyHttpClient.POST);
	}

	/**
	 * 验证手机号码与验证码是否匹配，只有不匹配或请求失败的情况下才会抛出异常
	 * 
	 * @param phone_num
	 *            手机号码
	 * @param verify_code
	 *            验证码
	 * @param password
	 *            可选，密码
	 * @throws YunmaoException
	 *             返回的statusCode范围：标准HttpStatus，error_code
	 */
	public void postForVerifyPhone(String phone_num, String verify_code,
			String password) throws YunmaoException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile_number", phone_num));
		params.add(new BasicNameValuePair("verify_code", verify_code));
		if (!TextUtils.isEmpty(password)) {
			params.add(new BasicNameValuePair("password", password));
		}

		YunmaoException httpResult = new YunmaoException("");
		try {
			retryOperation(params, PATH_POST_VERIFY_PHONE, MyHttpClient.POST);
		} catch (YunmaoException e) {
			httpResult = e;
		}

		if (httpResult.getErrorCode() == YunmaoException.ERROR_CODE_NONE) {
			try {
				JSONObject obj = new JSONObject(httpResult.getMessage());
				PreferenceUtil.putString(PreferenceUtil.PREF_CLIENT_PHONE,
						phone_num);
				if (obj.has("account_id")) {
					int accountId = obj.getInt("account_id");
					PreferenceUtil.putInt(PreferenceUtil.PREF_ACCOUNT_ID,
							accountId);
				}
			} catch (JSONException e) {
				throw new YunmaoException(e);
			}
		}

		throw httpResult;
	}

	/**
	 * 获取Token，该方法只有在请求数据返回401（未授权）时才会执行
	 * 
	 */
	private void postForAuthToken() {
		int tryCount = MAX_RETRY_COUNT;
		String timestamp = null;
		while (tryCount >= 0) {
			YunmaoException result = null;
			try {
				postForAuthToken(timestamp);
				timestamp = null;
			} catch (YunmaoException e) {
				result = e;
			}

			if (result == null) {
				break;
			} else if (result.getErrorCode() == 1) {
				timestamp = getServerTimestamp(result.getMessage());
			}
			tryCount--;
		}
	}

	/**
	 * 获取Token，该方法只有在请求数据返回401（未授权）时才会执行
	 * 
	 * @param tempTimestamp
	 *            服务器获取的时间戳，防止部分手机手机时间不准可能会导致时间戳过期，一直请求失败
	 * @throws YunmaoException
	 *             当返回结果中result不为true，则抛出异常
	 */
	private void postForAuthToken(String tempTimestamp) throws YunmaoException {
		LogUtil.i(TAG, "post for token = "
				+ (TextUtils.isEmpty(tempTimestamp) ? "" : tempTimestamp));
		String timestamp = TextUtils.isEmpty(tempTimestamp) ? String
				.valueOf(System.currentTimeMillis() / 1000) : tempTimestamp;
		String mac = SecretHelper.getInstance().getMacLikeString();
		String result = SecretHelper.getInstance()
				.getAuthResult(timestamp, mac);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mac", mac));
		params.add(new BasicNameValuePair("timestamp", String
				.valueOf(timestamp)));
		params.add(new BasicNameValuePair("result", result));
		params.add(new BasicNameValuePair("client_type", "app"));

		YunmaoException httpResult = null;
		try {
			baseSyncOpeation(params, null, PATH_POST_AUTHTOKEN,
					MyHttpClient.POST);
		} catch (YunmaoException e) {
			httpResult = e;
		}

		// 当返回结果不正确，则抛出异常
		if (httpResult == null
				|| httpResult.getErrorCode() != YunmaoException.ERROR_CODE_NONE) {
			throw httpResult;
		}

		try {
			JSONObject object = new JSONObject(httpResult.getMessage());
			String client_id = object.optString("client_id");
			String auth_token = object.optString("auth_token");
			if (!TextUtils.isEmpty(client_id) && !TextUtils.isEmpty(auth_token)) {
				PreferenceUtil.putString(PreferenceUtil.PREF_CLIENT_ID,
						client_id);
				PreferenceUtil.putString(PreferenceUtil.PREF_CLIENT_AUTH_TOKEN,
						auth_token);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void retryOperation(List<NameValuePair> params, String path,
			int httpmethod) throws YunmaoException {
		int tryCount = MAX_RETRY_COUNT;
		String timestamp = null;
		YunmaoException result = null;
		while (tryCount >= 0) {
			result = null;
			try {
				baseSyncOpeation(params, timestamp, path, httpmethod);
				timestamp = null;
			} catch (YunmaoException e) {
				result = e;
			}

			if (result == null) {
				tryCount--;
				continue;
			}

			if (result.getStatusCode() == HttpStatus.SC_UNAUTHORIZED
					&& result.getErrorCode() == YunmaoException.ERROR_CODE_AUTH_FAIL) {
				postForAuthToken();
				tryCount--;
				continue;
			}
			// 普通请求中时间戳过期的错误码是2
			else if (result.getStatusCode() == HttpStatus.SC_UNAUTHORIZED
					&& result.getErrorCode() == 2) {
				timestamp = getServerTimestamp(result.getMessage());
				tryCount--;
				continue;
			} else {
				break;
			}
		}

		if (result == null) {
			throw new YunmaoException("YunmaoException is null");
		} else {
			throw result;
		}
	}

	/**
	 * 处理与服务端交互的标准HTTP请求
	 * 
	 * @param params
	 *            请求时的原始参数
	 * @param tempTimestamp
	 *            服务器获取的时间戳，防止部分手机手机时间不准可能会导致时间戳过期，一直请求失败
	 * @param path
	 *            请求的PATH
	 * @param httpmethod
	 *            请求method
	 * @throws YunmaoException
	 */
	private void baseSyncOpeation(List<NameValuePair> params,
			String tempTimestamp, String path, int httpmethod)
			throws YunmaoException {
		List<NameValuePair> newParams;
		if (path.equals(PATH_POST_AUTHTOKEN)) {
			newParams = params;
		} else {
			newParams = generateParamStrByCombine(params, tempTimestamp, path);
			if (newParams == null) {
				throw new YunmaoException(null, HttpStatus.SC_UNAUTHORIZED,
						YunmaoException.ERROR_CODE_AUTH_FAIL);
			}
		}
		HttpResponse response = new MyHttpClient()
				.setConnectionTimeout(MyHttpClient.TIMEOUT_NET)
				.setBasicCredentials(BASIC_USERNAME, BASIC_PASSWORD)
				.getHttpResponse(httpmethod,
						String.format("%s%s", URL_BASE_API, path), newParams);

		int statusCode = response.getStatusLine().getStatusCode();
		LogUtil.i(TAG, "base statusCode = " + statusCode);
		if (statusCode != HttpStatus.SC_OK
				&& statusCode != HttpStatus.SC_CREATED
				&& statusCode != HttpStatus.SC_UNAUTHORIZED
				&& statusCode != HttpStatus.SC_BAD_REQUEST) {
			throw new YunmaoException("", statusCode);
		}

		try {
			String responseMsg = transStreamToString(response.getEntity()
					.getContent());
			LogUtil.i(TAG, String.format("code = %d result = %s", statusCode,
					responseMsg));
			int errorCode = YunmaoException.ERROR_CODE_DEFAULT;
			JSONObject object = new JSONObject(responseMsg);
			if (object.has("result")) {
				boolean result = object.getBoolean("result");
				errorCode = result ? YunmaoException.ERROR_CODE_NONE
						: errorCode;
			}

			if (object.has("error_code")) {
				errorCode = object.getInt("error_code");
			}

			throw new YunmaoException(responseMsg, statusCode, errorCode);
		} catch (ClientProtocolException e) {
			throw new YunmaoException(e);
		} catch (IOException e) {
			throw new YunmaoException(e);
		} catch (JSONException e) {
			throw new YunmaoException(e);
		}
	}

	/**
	 * 根据原始参数及请求路径获取带有签名的参数
	 * 
	 * @param params
	 *            POST请求时的原始参数
	 * @param tempTimestamp
	 *            部分手机手机时间不准可能会导致时间戳过期，一直请求失败
	 * @param path
	 *            POST请求的PATH
	 * @return 如果没有Token，则返回null
	 */
	private List<NameValuePair> generateParamStrByCombine(
			List<NameValuePair> params, String tempTimestamp, String path) {
		LogUtil.i(TAG, "generate param "
				+ (TextUtils.isEmpty(tempTimestamp) ? "null" : tempTimestamp));
		String clientId = PreferenceUtil.getString(
				PreferenceUtil.PREF_CLIENT_ID, "");
		String authToken = PreferenceUtil.getString(
				PreferenceUtil.PREF_CLIENT_AUTH_TOKEN, "");
		if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(authToken)) {
			return null;
		} else if (TextUtils.isEmpty(path)) {
			return params;
		}

		if (params != null) {
			Collections.sort(params, new Comparator<NameValuePair>() {

				@Override
				public int compare(NameValuePair lhs, NameValuePair rhs) {
					StringBuilder leftBuilder = new StringBuilder();
					StringBuilder rightBuilder = new StringBuilder();

					try {
						leftBuilder.append(URLEncoder.encode(lhs.getName(),
								"UTF-8"));
						leftBuilder.append('=');
						leftBuilder.append(URLEncoder.encode(lhs.getValue(),
								"UTF-8"));

						rightBuilder.append(URLEncoder.encode(rhs.getName(),
								"UTF-8"));
						rightBuilder.append('=');
						rightBuilder.append(URLEncoder.encode(rhs.getValue(),
								"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					return nullSafeStringComparator(leftBuilder.toString(),
							rightBuilder.toString());
				}
			});
		} else {
			params = new ArrayList<NameValuePair>();
		}

		String timestamp = TextUtils.isEmpty(tempTimestamp) ? String
				.valueOf(System.currentTimeMillis() / 1000) : tempTimestamp;
		StringBuilder tokenBuilder = new StringBuilder(80);
		tokenBuilder.append(URL_BASE_API);
		tokenBuilder.append(path);
		if (params != null && params.size() > 0) {
			tokenBuilder.append("?");
			try {
				for (NameValuePair pair : params) {
					String name = pair.getName();
					// 如果是附件，则不作为参数
					if (name.contains("file"))
						continue;

					// 否则UrlEncode参数
					tokenBuilder.append(TextUtils.isEmpty(name) ? ""
							: URLEncoder.encode(name, "UTF-8"));
					tokenBuilder.append("=");
					tokenBuilder.append(TextUtils.isEmpty(pair.getValue()) ? ""
							: URLEncoder.encode(pair.getValue(), "UTF-8"));
					tokenBuilder.append("&");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (tokenBuilder.toString().endsWith("&")
					|| tokenBuilder.toString().endsWith("?")) {
				tokenBuilder.deleteCharAt(tokenBuilder.length() - 1);
			}
		}
		tokenBuilder.append(clientId);
		tokenBuilder.append(timestamp);
		LogUtil.i(TAG, "generateParam url = " + tokenBuilder);

		String salt = EncryptUtil.getSHA1(tokenBuilder.toString()).trim();
		if (salt.length() > 20) {
			salt = salt.substring(0, 20);
		}
		// 取消Base64最后的空格符
		String signature = EncryptUtil.hmacSha1(salt, authToken).trim();

		LogUtil.i(
				TAG,
				String.format(
						"clientId = %s, authToken = %s, timestamp = %s, salt = %s, signature = %s",
						clientId, authToken, timestamp, salt, signature));

		List<NameValuePair> resultParams = new ArrayList<NameValuePair>(params);
		resultParams.add(new BasicNameValuePair("client_id", clientId));
		resultParams.add(new BasicNameValuePair("timestamp", timestamp));
		resultParams.add(new BasicNameValuePair("signature", signature));

		return resultParams;
	}

	private String getServerTimestamp(String responseStr) {
		if (TextUtils.isEmpty(responseStr)) {
			return null;
		}

		String timestamp = null;
		try {
			JSONObject object = new JSONObject(responseStr);
			timestamp = object.optString("timestamp");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return timestamp;
	}

	/**
	 * 当字符串参数为null时也安全的比较方法
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	private int nullSafeStringComparator(String left, String right) {
		int result = 0;
		if (left == null && right == null) {
			result = 0;
		} else if (left == null) {
			result = -1;
		} else if (right == null) {
			result = 1;
		} else {
			result = left.compareTo(right);
		}
		return result;
	}

	/**
	 * 将字节流转换成字符串
	 * 
	 * @param inStream
	 * @return 不可能返回null
	 */
	private String transStreamToString(InputStream inStream) {
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

}
