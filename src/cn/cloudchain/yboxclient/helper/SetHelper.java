package cn.cloudchain.yboxclient.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.text.TextUtils;
import android.util.Log;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.http.HttpHelper;
import cn.cloudchain.yboxcommon.bean.ErrorBean;
import cn.cloudchain.yboxcommon.bean.OperType;
import cn.cloudchain.yboxcommon.bean.Types;

import com.google.gson.stream.JsonWriter;

/**
 * 处理与中间件交互的接口
 * 
 * @author lazzy
 * 
 */
public class SetHelper {
	private final String TAG = SetHelper.class.getSimpleName();
	private static SetHelper instance;
	private final int PORT = 8888;
	private final int CONN_TIMEOUT = 3000; // socket连接超时
	private final int SO_TIMEOUT = 3000;// socket读取超时
	private final String OPER_KEY = "oper";
	private final String PARAMS_KEY = "params";

	public static SetHelper getInstance() {
		if (instance == null)
			instance = new SetHelper();
		return instance;
	}

	private SetHelper() {
	}

	/**
	 * 获取设备信息，如MAC地址
	 * 
	 * @return
	 */
	public String getDeviceInfo() {
		return baseGetRequest(OperType.device_info);
	}

	/**
	 * 获取自动休眠类型
	 * 
	 * @return
	 */
	public String getAutoSleepType() {
		return baseGetRequest(OperType.auto_sleep_info);
	}

	/**
	 * 设置自动休眠类型
	 * 
	 * @param type
	 *            具体类型见{@link Types}
	 * @return
	 */
	public String setAutoSleepType(int type) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.auto_sleep_set.getValue()).name(PARAMS_KEY)
					.beginObject().name("type").value(type).endObject()
					.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 关机
	 * 
	 * @param restart
	 *            true时重启
	 * @return 请求结果
	 */
	public String shutdown(boolean restart) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.shutdown.getValue()).name(PARAMS_KEY)
					.beginObject().name("restart").value(restart).endObject()
					.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 获取电量值
	 * 
	 * @return
	 */
	public String getBattery() {
		return baseGetRequest(OperType.battery);
	}

	/**
	 * 获取3G卡信号质量
	 * 
	 * @return
	 */
	public String getSignalQuality() {
		return baseGetRequest(OperType.signal_quality);
	}

	/**
	 * 设置手机数据
	 * 
	 * @param enable
	 *            true时允许数据流量
	 * @return
	 */
	public String setMobileDataEnable(boolean enable) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.mobile_data_control.getValue())
					.name(PARAMS_KEY).beginObject().name("enable")
					.value(enable).endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 设置无线参数
	 * 
	 * @param ssid
	 * @param pass
	 * @param keyMgmt
	 * @param maxClient
	 * @return
	 */
	public String setWifiInfo(String ssid, String pass, int keyMgmt,
			int maxClient) {
		if (TextUtils.isEmpty(ssid)) {
			return "";
		}
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_info_set.getValue()).name(PARAMS_KEY)
					.beginObject().name("ssid").value(ssid).name("pass")
					.value(pass);
			if (keyMgmt >= 0) {
				jWriter.name("keymgmt").value(keyMgmt);
			}
			if (maxClient >= 0) {
				jWriter.name("maxclient").value(maxClient);
			}
			jWriter.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 返回无线参数
	 * 
	 * @return
	 */
	public String getWifiInfo() {
		return baseGetRequest(OperType.wifi_info);
	}

	/**
	 * 获取连接终端的所有设备信息
	 * 
	 * @return
	 */
	public String getDevices(int type) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_devices.getValue());
			jWriter.name(PARAMS_KEY).beginObject();
			jWriter.name("type").value(type);
			jWriter.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 将设备加入黑名单
	 * 
	 * @param mac
	 *            设备号
	 * @return
	 */
	public String addToBlackList(String mac) {
		if (TextUtils.isEmpty(mac)) {
			return getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
		}
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_blacklist_add.getValue())
					.name(PARAMS_KEY).beginObject().name("mac").value(mac)
					.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return baseSocketRequest(sw.toString());
	}

	/**
	 * 清除黑名单
	 * 
	 * @param mac
	 *            设备号，为""时清除所有的黑名单
	 * @return
	 */
	public String clearBlackList(String mac) {
		if (TextUtils.isEmpty(mac)) {
			return getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
		}
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_blacklist_clear.getValue())
					.name(PARAMS_KEY).beginObject().name("mac").value(mac)
					.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 获取以太网信息
	 * 
	 * @return
	 */
	public String getEthernetInfo() {
		return baseGetRequest(OperType.ethernet_info);
	}

	/**
	 * 设置以太网静态模式，值不能为空
	 * 
	 * @param ip
	 * @param gateway
	 * @param mask
	 * @param dns1
	 * @param dns2
	 * @return
	 */
	public String setEthernetStatis(String ip, String gateway, String mask,
			String dns1, String dns2) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.ethernet_static_set.getValue());
			jWriter.name(PARAMS_KEY).beginObject();
			jWriter.name("ip").value(ip);
			jWriter.name("gateway").value(gateway);
			jWriter.name("mask").value(mask);
			jWriter.name("dns1").value(dns1);
			jWriter.name("dns2").value(dns2);
			jWriter.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 设置以太网DHCP模式
	 * 
	 * @return
	 */
	public String setEthernetDhcp() {
		return baseGetRequest(OperType.ethernet_dhcp_set);
	}

	/**
	 * 获取SIM相关网络配置信息
	 * 
	 * @return
	 */
	public String getMobileNetInfo() {
		return baseGetRequest(OperType.mobile_net_info);
	}

	/**
	 * 使终端休眠
	 * 
	 * @return
	 */
	public String sleepAp() {
		return baseGetRequest(OperType.sleep);
	}

	/**
	 * 重启终端
	 * 
	 * @return
	 */
	public String restartWifi() {
		return baseGetRequest(OperType.wifi_restart);
	}

	/**
	 * 获取热点自动关闭的时长
	 * 
	 * @return
	 */
	public String getWifiAutoDisable() {
		return baseGetRequest(OperType.wifi_auto_disable_info);
	}

	/**
	 * 设置热点自动关闭时长
	 * 
	 * @param time
	 * @return
	 */
	public String setWifiAutoDisable(int time) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_auto_disable_set.getValue())
					.name(PARAMS_KEY).beginObject().name("time").value(time)
					.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 获取手机流量信息
	 * 
	 * @return
	 */
	public String getMobileTrafficInfo() {
		return baseGetRequest(OperType.mobile_traffic_info);
	}

	public String updateRootImage(String path) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.update_root_image.getValue());
			jWriter.name(PARAMS_KEY).beginObject();
			jWriter.name("path").value(path);
			jWriter.name("mode").value(1);
			jWriter.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	/**
	 * 升级中间件
	 * 
	 * @param path
	 * @return
	 */
	public String updateMiddleApk(String path) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.update_middle.getValue());
			jWriter.name(PARAMS_KEY).beginObject();
			jWriter.name("path").value(path);
			jWriter.name("mode").value(1);
			jWriter.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	private String baseGetRequest(OperType type) {
		StringWriter sw = new StringWriter(20);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY).value(type.getValue())
					.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baseSocketRequest(sw.toString());
	}

	private String baseSocketRequest(String jsonStr) {
		String result = "";
		Socket socket = null;
		OutputStream os = null;
		InputStream is = null;
		try {
			socket = new Socket();
			SocketAddress remoteAddr = new InetSocketAddress(
					HttpHelper.getGateway(MyApplication.getAppContext()), PORT);
			socket.connect(remoteAddr, CONN_TIMEOUT);
			socket.setReuseAddress(true);
			socket.setSoTimeout(SO_TIMEOUT);

			os = socket.getOutputStream();
			is = socket.getInputStream();

			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write(jsonStr);
			// 这个换行符必须加，避免服务端在readline由于获取不到'\r'或者'\n'或者'\r\n'时一直阻塞
			osw.write('\n');
			osw.flush();

			Log.i(TAG, "message has send: " + jsonStr);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Log.i(TAG, "message wait for recieve: ");
			result = br.readLine();
			Log.i(TAG, "message has recieve: " + result);
		} catch (Exception e) {
			e.printStackTrace();
			result = getErrorJson();
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (TextUtils.isEmpty(result)) {
			result = getErrorJson();
		}
		return result;
	}

	private String getErrorJson() {
		return getErrorJson(ErrorBean.REQUEST_TIMEOUT);
	}

	private String getErrorJson(int code) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(false);
			if (code > 0) {
				jWriter.name("error_code").value(code);
			}
			String msg = ErrorBean.getInstance().getErrorMsg(code);
			if (!TextUtils.isEmpty(msg)) {
				jWriter.name("error_msg").value(msg);
			}
			jWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

}
