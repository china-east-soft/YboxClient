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
import cn.cloudchain.yboxcommon.bean.OperType;

import com.google.gson.stream.JsonWriter;

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
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.battery.getValue()).endObject();
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
	 * 获取3G卡信号质量
	 * 
	 * @return
	 */
	public String getSignalQuality() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.signal_quality.getValue()).endObject();
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
	 * @return
	 */
	public String setWifiInfo(String ssid, String pass) {
		if (TextUtils.isEmpty(ssid)) {
			return "";
		}
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_info_set.getValue()).name(PARAMS_KEY)
					.beginObject().name("ssid").value(ssid).name("pass")
					.value(pass).endObject().endObject();
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
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_info.getValue()).endObject();
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
	 * 获取连接终端的所有设备信息
	 * 
	 * @return
	 */
	public String getDevices() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_devices.getValue()).endObject();
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
	 * @param once
	 *            true是为暂时拒绝该设备连接，false时永久加入黑名单
	 * @return
	 */
	public String addToBlackList(String mac, boolean once) {
		if (TextUtils.isEmpty(mac)) {
			return "";
		}
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(OPER_KEY)
					.value(OperType.wifi_blacklist_add.getValue())
					.name(PARAMS_KEY).beginObject().name("mac").value(mac)
					.name("once").value(once).endObject().endObject();
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

	private String baseSocketRequest(String jsonStr) {
		String result = "";
		Socket socket = null;
		OutputStream os = null;
		InputStream is = null;
		try {
			socket = new Socket();
			SocketAddress remoteAddr = new InetSocketAddress(Helper
					.getInstance().getGateway(MyApplication.getAppContext()),
					PORT);
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
		return result;
	}

	private String getErrorJson() {
		return getErrorJson(100, null);
	}

	private String getErrorJson(int code, String msg) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(false);
			if (code > 0) {
				jWriter.name("error_code").value(code);
			}
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
