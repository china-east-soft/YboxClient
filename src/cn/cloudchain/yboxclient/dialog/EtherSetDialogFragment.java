package cn.cloudchain.yboxclient.dialog;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;

public class EtherSetDialogFragment extends DialogFragment {
	private CheckBox checkbox;
	private Button setButton;
	private View staticContainer;

	private EditText ipText;
	private EditText gwText;
	private EditText maskText;
	private EditText dns1Text;
	private EditText dns2Text;

	private MyHandler handler = new MyHandler(this);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_ether_set, container,
				false);
		checkbox = (CheckBox) view.findViewById(R.id.isdhtcp);
		checkbox.setChecked(false);
		staticContainer = view.findViewById(R.id.static_container);
		ipText = (EditText) view.findViewById(R.id.ip);
		gwText = (EditText) view.findViewById(R.id.gateway);
		maskText = (EditText) view.findViewById(R.id.mask);
		dns1Text = (EditText) view.findViewById(R.id.dns1);
		dns2Text = (EditText) view.findViewById(R.id.dns2);

		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					staticContainer.setVisibility(View.GONE);
				} else {
					staticContainer.setVisibility(View.VISIBLE);
				}
			}
		});

		setButton = (Button) view.findViewById(R.id.confirm);
		setButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						if (checkbox.isChecked()) {
							Message msg = handler
									.obtainMessage(MyHandler.SET_COMPLETE);
							msg.obj = SetHelper.getInstance().setEthernetDhcp();
							handler.sendMessage(msg);
						} else {
							handleStaticSetting();
						}
					}
				}).start();
			}
		});
		return view;
	}

	private void handleStaticSetting() {
		final String ip = ipText.getText().toString();
		final String gw = gwText.getText().toString();
		final String mask = maskText.getText().toString();
		if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(gw)
				|| TextUtils.isEmpty(mask)) {
			Toast.makeText(getActivity(), "IP 网关 子网掩码不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String dns1 = dns1Text.getText().toString();
		String dns2 = dns2Text.getText().toString();
		if (TextUtils.isEmpty(dns1) && TextUtils.isEmpty(dns2)) {
			Toast.makeText(getActivity(), "DNS不能均为空", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (TextUtils.isEmpty(dns1)) {
			dns1 = dns2;
		} else if (TextUtils.isEmpty(dns2)) {
			dns2 = dns1;
		}

		final String temDns1 = dns1;
		final String temDns2 = dns2;

		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().setEthernetStatis(ip,
						gw, mask, temDns1, temDns2);
				Message msg = handler.obtainMessage(MyHandler.SET_COMPLETE);
				msg.obj = result;
				handler.sendMessage(msg);
			}
		}).start();

	}

	private static class MyHandler extends WeakHandler<EtherSetDialogFragment> {
		final static int GET_ETH_INFO = 0;
		final static int GET_ETH_COMPLETE = 1;
		final static int SET_COMPLETE = 2;

		public MyHandler(EtherSetDialogFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case GET_ETH_INFO:
				break;
			case GET_ETH_COMPLETE:
				break;
			case SET_COMPLETE:
				Toast.makeText(getOwner().getActivity(), (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

	}
}
