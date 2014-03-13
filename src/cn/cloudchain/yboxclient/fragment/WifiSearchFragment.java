package cn.cloudchain.yboxclient.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;
import cn.cloudchain.yboxclient.MainGridActivity;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.dialog.WifiChooseDialogFragment;
import cn.cloudchain.yboxclient.dialog.WifiChooseDialogFragment.IWifiChooseService;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;

public class WifiSearchFragment extends Fragment {
	private final String TAG = WifiSearchFragment.class.getSimpleName();

	private ViewFlipper imageFlipper;
	private ViewFlipper textFlipper;
	private View wifiSearch;

	private WifiManager wifiManager;
	private ScanHandler handler = new ScanHandler(this);
	private ScanWifiResultBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_wifi_search, container,
				false);
		imageFlipper = (ViewFlipper) view
				.findViewById(R.id.wifi_search_flipper);
		textFlipper = (ViewFlipper) view
				.findViewById(R.id.wifi_search_reminder);
		imageFlipper.startFlipping();
		textFlipper.startFlipping();

		wifiSearch = view.findViewById(R.id.wifi_search_icon);
		wifiSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				handler.sendEmptyMessage(ScanHandler.MSG_START_SCAN);
				imageFlipper.startFlipping();
				textFlipper.startFlipping();
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		wifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		register();
	}

	@Override
	public void onStop() {
		unregister();
		super.onStop();
	}

	private void connectSSID(String ssid) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + ssid + "\"";
		config.status = WifiConfiguration.Status.ENABLED;
		config.allowedKeyManagement.set(KeyMgmt.NONE);
		int netId = wifiManager.addNetwork(config);

		boolean result = false;
		if (wifiManager.enableNetwork(netId, true)) {
			result = true;
		}
		LogUtil.d(getTag(),
				String.format("netId = %d result = %b", netId, result));
	}

	@SuppressWarnings("unused")
	private void disconnectSSID(String ssid) {
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null || wifiInfo.getSSID() == null)
			return;
		if (Util.removeQuotOfSSID(wifiInfo.getSSID()).equals(ssid)) {
			boolean result = wifiManager
					.disableNetwork(wifiInfo.getNetworkId());
			LogUtil.d(getTag(), String.format(
					"ssid = %s disconnect result = %b", ssid, result));
		}
	}

	private void showChooseDialog(List<ScanResult> results) {
		if (results == null || results.size() < 2) {
			return;
		}
		WifiChooseDialogFragment fragment = WifiChooseDialogFragment
				.newInstance((ArrayList<ScanResult>) results);
		fragment.setWifiChooseService(new IWifiChooseService() {

			@Override
			public void onItemClick(ScanResult result) {
				if (result != null) {
					connectSSID(Util.removeQuotOfSSID(result.SSID));
				}
			}
		});
		fragment.show(getFragmentManager(), null);
	}

	/**
	 * 处理扫描可用SSID结果
	 */
	private void handleScanComplete() {
		imageFlipper.stopFlipping();
		textFlipper.stopFlipping();
		wifiSearch.setEnabled(true);

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null && Util.isValidApSSID(wifiInfo.getSSID())) {
			return;
		}

		List<ScanResult> results = wifiManager.getScanResults();
		List<ScanResult> newArray = new ArrayList<ScanResult>();
		for (ScanResult result : results) {
			if (Util.isValidApSSID(result.SSID)) {
				newArray.add(result);
			}
		}
		if (newArray.size() == 1) {
			connectSSID(newArray.get(0).SSID);
		} else {
			showChooseDialog(newArray);
		}
	}

	/**
	 * 处理无线Enable状态变化
	 * 
	 * @param bundle
	 */
	private void handleWifiStateChange(Bundle bundle) {
		int state = bundle.getInt(WifiManager.EXTRA_WIFI_STATE);
		if (state == WifiManager.WIFI_STATE_DISABLED) {

		} else if (state == WifiManager.WIFI_STATE_ENABLED) {
			handler.sendEmptyMessage(ScanHandler.MSG_START_SCAN);
		}
	}

	/**
	 * 
	 * @param bundle
	 */
	private void handleConnectionChange(Bundle bundle) {
		LogUtil.d(TAG, "connectivity change");
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null && Util.isValidApSSID(wifiInfo.getSSID())) {
			Intent intent = new Intent(getActivity(), MainGridActivity.class);
			getActivity().startActivity(intent);
			getActivity().finish();
		}
	}

	private static class ScanHandler extends WeakHandler<WifiSearchFragment> {
		private static final int MSG_START_SCAN = 0;
		private static final int MSG_SCAN_COMPLETE = 1;
		private static final int MSG_WIFI_STATE_CHANGE = 3;
		private static final int MSG_CONNECTION_CHANGE = 4;

		public ScanHandler(WifiSearchFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			WifiSearchFragment owner = getOwner();
			if (owner == null)
				return;

			switch (msg.what) {
			case MSG_START_SCAN:
				LogUtil.i("ScanHandler", "start scan");
				if (owner.wifiManager.startScan()) {
					getOwner().wifiSearch.setEnabled(false);
				}

				break;
			case MSG_SCAN_COMPLETE:
				owner.handleScanComplete();
				break;
			case MSG_WIFI_STATE_CHANGE:
				owner.handleWifiStateChange(msg.getData());
				break;
			case MSG_CONNECTION_CHANGE:
				owner.handleConnectionChange(msg.getData());
				break;
			}
		}
	}

	private void register() {
		if (receiver == null) {
			receiver = new ScanWifiResultBroadcastReceiver();
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(receiver, filter);
	}

	private void unregister() {
		if (receiver != null) {
			getActivity().unregisterReceiver(receiver);
		}
	}

	private class ScanWifiResultBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				handler.sendEmptyMessage(ScanHandler.MSG_SCAN_COMPLETE);
			} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				Message msg = handler
						.obtainMessage(ScanHandler.MSG_WIFI_STATE_CHANGE);
				msg.setData(intent.getExtras());
				handler.sendMessage(msg);
			} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				Message msg = handler
						.obtainMessage(ScanHandler.MSG_CONNECTION_CHANGE);
				msg.setData(intent.getExtras());
				handler.sendMessage(msg);
			}
		}

	}

}
