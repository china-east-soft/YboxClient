package cn.cloudchain.yboxclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.dialog.WifiRestartDialogFragment;
import cn.cloudchain.yboxclient.face.IPAddressKeyListener;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;

public class WlanSetActivity extends ActionBarActivity implements
		OnClickListener {
	/**
	 * 模式大于0时为以太网
	 */
	public static final String BUNDLE_MODE = "mode";
	public static final String BUNDLE_IPADDRESS = "ipaddress";
	public static final String BUNDLE_GATEWAY = "gateway";
	public static final String BUNDLE_MASK = "mask";
	public static final String BUNDLE_DNS1 = "dns1";
	public static final String BUNDLE_DNS2 = "dns2";

	private RadioGroup modeRadioGroup;
	private EditText addressEditText;
	private EditText gatewayEditText;
	private EditText maskEditText;
	private EditText dns1EditText;
	private EditText dns2EditText;

	private Button setButton;

	private IPAddressKeyListener keyListener = new IPAddressKeyListener();
	private MyHandler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.wlan_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_wlan);
		modeRadioGroup = (RadioGroup) this.findViewById(R.id.ip_mode);
		addressEditText = (EditText) this.findViewById(R.id.address_edittext);
		gatewayEditText = (EditText) this.findViewById(R.id.gateway_edittext);
		maskEditText = (EditText) this.findViewById(R.id.mask_edittext);
		dns1EditText = (EditText) this.findViewById(R.id.dns1_edittext);
		dns2EditText = (EditText) this.findViewById(R.id.dns2_edittext);

		addressEditText.setKeyListener(keyListener);
		gatewayEditText.setKeyListener(keyListener);
		maskEditText.setKeyListener(keyListener);
		dns1EditText.setKeyListener(keyListener);
		dns2EditText.setKeyListener(keyListener);

		setButton = (Button) this.findViewById(R.id.set);
		setButton.setOnClickListener(this);

		modeRadioGroup
				.setOnCheckedChangeListener(new ModeCheckChangeListener());
		modeRadioGroup.check(R.id.ip_dhcp);
		enableEditText(false);
		initView(getIntent().getExtras());

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set:
			handler.sendEmptyMessage(modeRadioGroup.getCheckedRadioButtonId() == R.id.ip_dhcp ? MyHandler.WLAN_DHCP_SET
					: MyHandler.WLAN_STATIC_SET);
			break;
		}
	}

	private void initView(Bundle bundle) {
		if (bundle == null)
			return;

		String address = bundle.getString(BUNDLE_IPADDRESS);
		String gateway = bundle.getString(BUNDLE_GATEWAY);
		String mask = bundle.getString(BUNDLE_MASK);
		String dns1 = bundle.getString(BUNDLE_DNS1);
		String dns2 = bundle.getString(BUNDLE_DNS2);
		int mode = bundle.getInt(BUNDLE_MODE);

		addressEditText.setText(address);
		gatewayEditText.setText(gateway);
		maskEditText.setText(mask);
		dns1EditText.setText(dns1);
		dns2EditText.setText(dns2);

		// 为以太网连接
		if (mode > 0) {
			modeRadioGroup.setVisibility(View.VISIBLE);
			modeRadioGroup.check(mode == 1 ? R.id.ip_static : R.id.ip_dhcp);
			setButton.setVisibility(View.VISIBLE);
			enableEditText(true);
		}

	}

	private void enableEditText(boolean enable) {
		addressEditText.setEnabled(enable);
		gatewayEditText.setEnabled(enable);
		maskEditText.setEnabled(enable);
		dns1EditText.setEnabled(enable);
		dns2EditText.setEnabled(enable);
	}

	private class ModeCheckChangeListener implements
			RadioGroup.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			enableEditText(checkedId == R.id.ip_static);
		}
	}

	private static class MyHandler extends WeakHandler<WlanSetActivity> {
		private static final int WLAN_STATIC_SET = 1;
		private static final int WLAN_DHCP_SET = 2;
		private static final int WLAN_SET_COMPLETE = 3;

		public MyHandler(WlanSetActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case WLAN_STATIC_SET:
				getOwner().handleStaticSet();
				break;
			case WLAN_DHCP_SET:
				getOwner().handleDhcpSet();
				break;
			case WLAN_SET_COMPLETE:
				break;
			}
		}
	}

	private void handleDhcpSet() {
		WlanSetTask task = new WlanSetTask(this);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				"正在设置中", false);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), TaskDialogFragment.TAG);
	}

	private void handleStaticSet() {
		String ip = addressEditText.getText().toString().trim();
		if (!isValidAddress(ip)) {
			Util.toaster(R.string.address_invalid);
			return;
		}
		String gateway = gatewayEditText.getText().toString().trim();
		if (!isValidAddress(gateway)) {
			Util.toaster(R.string.gateway_invalid);
			return;
		}
		String mask = maskEditText.getText().toString().trim();
		if (!isValidAddress(mask)) {
			Util.toaster(R.string.mask_invalid);
			return;
		}

		String dns1 = dns1EditText.getText().toString().trim();
		String dns2 = dns2EditText.getText().toString().trim();
		if (TextUtils.isEmpty(dns1) && TextUtils.isEmpty(dns2)) {
			Util.toaster(R.string.dns_empty_all);
			return;
		}

		if (TextUtils.isEmpty(dns1)) {
			dns1 = dns2;
		} else if (TextUtils.isEmpty(dns2)) {
			dns2 = dns1;
		}

		if (!isValidAddress(dns1) || !isValidAddress(dns2)) {
			Util.toaster(R.string.dns_invalid);
			return;
		}

		WlanSetTask task = new WlanSetTask(this, ip, gateway, mask, dns1, dns2);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				"正在设置中", false);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), TaskDialogFragment.TAG);
	}

	private boolean isValidAddress(String ipAddress) {
		String ip = "^(0|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(0|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(0|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(0|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	private class WlanSetTask extends BaseFragmentTask {
		private static final int RESULT_SUCCESS = 0;
		private static final int RESULT_FAIL = 1;

		private static final int MODE_STATIC = 1;
		private static final int MODE_DHCP = 2;

		private Context context;
		private int mode = -1;
		private String ip;
		private String gateway;
		private String mask;
		private String dns1;
		private String dns2;

		/**
		 * 默认使用DHCP
		 */
		public WlanSetTask(Context context) {
			this.mode = MODE_DHCP;
			this.context = context;
		}

		public WlanSetTask(Context context, String ip, String gateway,
				String mask, String dns1, String dns2) {
			this.context = context;
			this.mode = MODE_STATIC;
			this.ip = ip;
			this.gateway = gateway;
			this.mask = mask;
			this.dns1 = dns1;
			this.dns2 = dns2;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			super.doInBackground(params);
			int result = RESULT_FAIL;
			String response = null;
			if (mode == MODE_DHCP) {
				response = SetHelper.getInstance().setEthernetDhcp();
			} else if (mode == MODE_STATIC) {
				response = SetHelper.getInstance().setEthernetStatis(ip,
						gateway, mask, dns1, dns2);
			}
			if (TextUtils.isEmpty(response)) {
				return result;
			}
			try {
				JSONObject obj = new JSONObject(response);
				result = obj.optBoolean("result") ? RESULT_SUCCESS
						: RESULT_FAIL;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (isCancelled()) {
				return;
			}
			switch (result) {
			case RESULT_FAIL:
				Util.toaster(R.string.set_fail);
				break;
			case RESULT_SUCCESS:
				if (context instanceof FragmentActivity) {
					FragmentManager fm = ((FragmentActivity) context)
							.getSupportFragmentManager();
					DialogFragment fragment = WifiRestartDialogFragment
							.newInstance();
					fragment.show(fm, null);
				}
				break;
			}
		}
	}
}
