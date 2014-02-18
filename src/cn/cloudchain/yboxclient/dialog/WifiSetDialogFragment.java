package cn.cloudchain.yboxclient.dialog;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;

public class WifiSetDialogFragment extends DialogFragment implements
		OnClickListener {
	private EditText ssidText;
	private EditText passText;
	private SetHandler handler = new SetHandler(this);
	private String pass = "";
	private String ssid = "";

	public static WifiSetDialogFragment newInstance(String jsonStr) {
		WifiSetDialogFragment fragment = new WifiSetDialogFragment();
		Bundle args = new Bundle();
		args.putString("json", jsonStr);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String json = getArguments().getString("json");
		try {
			JSONObject obj = new JSONObject(json);
			if (obj.optBoolean("result")) {
				ssid = obj.optString("ssid");
				pass = obj.optString("pass");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_wifiset, container, false);
		ssidText = (EditText) view.findViewById(R.id.ssid);
		passText = (EditText) view.findViewById(R.id.pass);
		ssidText.setText(ssid);
		passText.setText(pass);
		view.findViewById(R.id.confirm).setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		handler.sendEmptyMessage(SetHandler.WIFI_SET);
	}

	private void handleWifiSet() {
		final String ssid = ssidText.getText().toString().trim();
		if (TextUtils.isEmpty(ssid)) {
			Toast.makeText(getActivity(), "SSID不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		final String pass = passText.getText().toString().trim();
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().setWifiInfo(ssid, pass);
				boolean success = false;
				try {
					JSONObject object = new JSONObject(result);
					success = object.optBoolean("result");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(success ? SetHandler.WIFI_SET_SUCCESS
						: SetHandler.WIFI_SET_FAIL);
			}
		}).start();
	}

	private static class SetHandler extends WeakHandler<WifiSetDialogFragment> {
		private static final int WIFI_SET = 0;
		private static final int WIFI_SET_SUCCESS = 1;
		private static final int WIFI_SET_FAIL = 2;

		public SetHandler(WifiSetDialogFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case WIFI_SET:
				getOwner().handleWifiSet();
				break;
			case WIFI_SET_SUCCESS:
				Toast.makeText(getOwner().getActivity(), "设置成功",
						Toast.LENGTH_SHORT).show();
				getOwner().dismiss();
				break;
			case WIFI_SET_FAIL:
				Toast.makeText(getOwner().getActivity(), "设置失败",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

	}

}
