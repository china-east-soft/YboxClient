package cn.cloudchain.yboxclient.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.bean.FrequencyBean;
import cn.cloudchain.yboxclient.bean.FrequencyDeserializer;
import cn.cloudchain.yboxclient.bean.ProgramBean;
import cn.cloudchain.yboxclient.bean.StatusBean;
import cn.cloudchain.yboxclient.bean.StatusDeserializer;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.http.MyHttpClient;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.PreferenceUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * 处理与终端直接交互的接口（不通过中间件）
 * 
 * @author lazzy
 * 
 */
public class ApHelper {
	final static String TAG = ApHelper.class.getSimpleName();
	private static final String URL_EPG = "http://%s:81/epg.json";
	private static final String URL_ASK_PLAY_URL = "http://%s:81/tv_play_url.json?freq=%s&channel=%s";
	private static final String URL_STATUS = "http://%s:81/tv_status.json";
	private static final String URL_TVINFO = "http://%s:81/tv_info.json";
	private static final String URL_TVMODE_CHANGE = "http://%s:81/change_tv_mode.json?mode=%s";
	private static final String URL_SUPPORT_MODES = "http://%s:81/tv_modes.json";
	private static final String URL_AUTOSEARCH_MODE = "http://%s:81/search_mode.json";
	private static final String URL_AUTOSEARCH_MODE_SET = "http://%s:81/change_search_mode.json?auto=%s";
	private static final String URL_FREQ_SEARCH = "http://%s:81/tv_search.json";
	private static ApHelper instance;
	private List<FrequencyBean> frequencyList;

	private StatusBean status;

	public static ApHelper getInstance() {
		if (instance == null)
			instance = new ApHelper();
		return instance;
	}

	private ApHelper() {
	}

	/**
	 * 修改电视制式
	 * 
	 * @return JSON字符串或者"" {"oper":101, "mode": mode}
	 * @throws YunmaoException
	 */
	public String changeTVMode(String mode) throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String result = getResponseJson(
				String.format(URL_TVMODE_CHANGE, address, mode),
				MyHttpClient.TIMEOUT_AP);
		return result;
	}

	/**
	 * 获取目前支持的所有模式
	 * 
	 * @return {"result":true, "modes":"dtmb cmmb", "current":"cmmb"}
	 * @throws YunmaoException
	 */
	public String getTVModes() throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String result = getResponseJson(
				String.format(URL_SUPPORT_MODES, address),
				MyHttpClient.TIMEOUT_AP);
		return result;
	}

	/**
	 * 获取是否自动搜台
	 * 
	 * @return
	 * @throws YunmaoException
	 */
	public boolean isAutoSearchMode() throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String result = getResponseJson(
				String.format(URL_AUTOSEARCH_MODE, address),
				MyHttpClient.TIMEOUT_AP);
		boolean auto = false;
		try {
			JSONObject obj = new JSONObject(result);
			auto = obj.optBoolean("auto");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return auto;
	}

	/**
	 * 设置自动搜台模式
	 * 
	 * @param auto
	 * @return
	 * @throws YunmaoException
	 */
	public boolean setAutoSearchMode(boolean auto) throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String response = getResponseJson(
				String.format(URL_AUTOSEARCH_MODE_SET, address,
						String.valueOf(auto)), MyHttpClient.TIMEOUT_AP);
		boolean result = false;
		try {
			JSONObject obj = new JSONObject(response);
			auto = obj.optBoolean("result");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 强制搜频
	 * 
	 * @return
	 * @throws YunmaoException
	 */
	public String startFreqSearch() throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String result = getResponseJson(
				String.format(URL_FREQ_SEARCH, address),
				MyHttpClient.TIMEOUT_AP);
		return result;
	}

	/**
	 * 获取电视信号相关内容
	 * 
	 * @return
	 * @throws YunmaoException
	 */
	public String getTVInfo() throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String result = getResponseJson(String.format(URL_TVINFO, address),
				MyHttpClient.TIMEOUT_AP);
		return result;
	}

	/**
	 * 获取视频播放链接
	 * 
	 * @param freq
	 * @param channel
	 * @return 播放链接或者空字符串
	 * @throws YunmaoException
	 */
	public String getPlayUrl(String freq, String channel)
			throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String response = getResponseJson(
				String.format(URL_ASK_PLAY_URL, address, freq, channel),
				MyHttpClient.TIMEOUT_AP);
		String result = "";
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean("result")) {
				result = obj.optString("url");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取心跳包
	 * 
	 * @return 返回StatusBean实例或null
	 * @throws YunmaoException
	 *             HTTP请求异常，JSON解析异常
	 */
	public StatusBean getStatus() throws YunmaoException {
		String address = MyApplication.getInstance().gateway;
		if (TextUtils.isEmpty(address)) {
			throw new YunmaoException("address is empty");
		}
		String result = getResponseJson(String.format(URL_STATUS, address),
				MyHttpClient.TIMEOUT_AP);
		StatusBean bean = null;
		try {
			Gson gson = new GsonBuilder().registerTypeAdapter(StatusBean.class,
					new StatusDeserializer()).create();
			bean = gson.fromJson(result, StatusBean.class);
		} catch (Exception e) {
			throw new YunmaoException(e);
		}
		return bean;
	}

	public static final int STATE_STATUS_NULL = 1;
	public static final int STATE_REACH_LIMIT = 2;
	public static final int STATE_SWITCH_DISABLE = 3;
	public static final int STATE_SWITCHABLE = 4;

	/**
	 * 获取节目的切频状态
	 * 
	 * @param program
	 *            不能为空
	 * @return
	 */
	public int getSwitchState(ProgramBean program) {
		if (status == null) {
			return STATE_STATUS_NULL;
		}
		if (status.isLimit()) {
			return STATE_REACH_LIMIT;
		}
		if (status.getApply() == 0) {
			return STATE_SWITCHABLE;
		}
		String curFreq = status.getFreq();
		String avaibleChannels = status.getChannels();
		if (curFreq.contains(program.getFreqValue())
				&& avaibleChannels.contains(program.getServiceId())) {
			return STATE_SWITCHABLE;
		} else {
			return STATE_SWITCH_DISABLE;
		}
	}

	/**
	 * 更新电视播放状态
	 * 
	 * @param status
	 */
	public void updateStatus(StatusBean status) {
		this.status = status;
	}

	public StatusBean getCurrentStatus() {
		return status;
	}

	/**
	 * 获取EPG信息
	 * 
	 * @param refresh
	 *            是否需刷新
	 * @return 或null
	 */
	public List<FrequencyBean> getFrequencyList(boolean refresh) {
		if (refresh) {
			String apAddress = MyApplication.getInstance().gateway;
			if (!TextUtils.isEmpty(apAddress)) {
				LogUtil.i(TAG, "download and save freq");
				String url = String.format(URL_EPG, apAddress);
				String result = "";
				try {
					result = getResponseJson(url, MyHttpClient.TIMEOUT_NET);
				} catch (YunmaoException e) {
					e.printStackTrace();
				}
				if (!TextUtils.isEmpty(result)) {
					PreferenceUtil.putString(PreferenceUtil.LOCAL_EPG, result);
					frequencyList = null;
				}
			}
		}
		if (frequencyList == null) {
			try {
				String epgStr = PreferenceUtil.getString(
						PreferenceUtil.LOCAL_EPG, "");
				TypeToken<List<FrequencyBean>> list = new TypeToken<List<FrequencyBean>>() {
				};
				Gson gson = new GsonBuilder().registerTypeAdapter(
						list.getType(), new FrequencyDeserializer()).create();

				frequencyList = gson.fromJson(epgStr, list.getType());
			} catch (Exception e) {
				e.printStackTrace();
			}
			LogUtil.i(TAG, "download and save get freq = "
					+ (frequencyList == null ? 0 : frequencyList.size()));
		}

		if (frequencyList == null) {
			PreferenceUtil.remove(PreferenceUtil.LOCAL_EPG_CREATE_TIME);
		}
		return frequencyList;
	}

	/**
	 * 处理HTTP GET请求，并返回 {@link InputStream} 类型的数据
	 * 
	 * @param urlpath
	 *            HTTP请求链接
	 * @return JSON字符串或者空字符串
	 * @throws YunmaoException
	 */
	private String getResponseJson(String urlpath, int timeType)
			throws YunmaoException {
		HttpResponse response = new MyHttpClient().setConnectionTimeout(
				timeType).getHttpResponse(MyHttpClient.GET, urlpath, null);

		if (response == null) {
			throw new YunmaoException("response is null");
		}

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			throw new YunmaoException("http status not 200", statusCode);
		}

		String result = "";
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inStream = entity.getContent();
				result = Helper.getInstance().transStreamToString(inStream);
			}
		} catch (IllegalStateException e) {
			throw new YunmaoException(e);
		} catch (IOException e) {
			throw new YunmaoException(e);
		}

		return result;
	}

}
