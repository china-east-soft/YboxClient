package cn.cloudchain.yboxclient;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.dialog.WifiRestartDialogFragment;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;
import cn.cloudchain.yboxclient.utils.Util;

public class WifiSetActivity extends ActionBarActivity {
	final String TAG = WifiSetActivity.class.getSimpleName();
	public static final String BUNDLE_SSID = "ssid";
	public static final String BUNDLE_CHANNEL = "channel";

	private EditText ssidEditText;
	private Spinner channelSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.wlan_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_wifi);
		ssidEditText = (EditText) this.findViewById(R.id.ssid_edittext);
		channelSpinner = (Spinner) this.findViewById(R.id.channel_spinner);
		initView(getIntent().getExtras());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wifiset, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		case R.id.confirm:
			handleWifiSet();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView(Bundle bundle) {
		if (bundle == null)
			return;
		String ssid = bundle.getString(BUNDLE_SSID);
		int channel = bundle.getInt(BUNDLE_CHANNEL);
		int index = ssid.indexOf("YBOX-");
		if (index >= 0) {
			ssid = ssid.substring(5, ssid.length());
		}
		ssidEditText.setText(ssid);
		channelSpinner.setSelection(channel);
	}

	private void handleWifiSet() {
		String ssid = ssidEditText.getText().toString().trim();
		if (TextUtils.isEmpty(ssid)) {
			Util.toaster(R.string.ssid_empty);
			return;
		}
		int channel = channelSpinner.getSelectedItemPosition();

		WifiSetTask task = new WifiSetTask(this, ssid, channel);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				null, false);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), null);
	}

	private class WifiSetTask extends BaseFragmentTask {
		private final static int RESULT_SUCCESS = 0;
		private final static int RESULT_FAIL = 1;

		private Context context;
		private String ssid;
		private int channel;

		public WifiSetTask(Context context, String ssid, int channel) {
			this.context = context;
			this.ssid = ssid;
			this.channel = channel;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			super.doInBackground(params);
			int result = RESULT_FAIL;
			String response = SetHelper.getInstance()
					.setWifiInfo(ssid, channel);
			try {
				JSONObject obj = new JSONObject(response);
				if (obj.optBoolean("result")) {
					result = RESULT_SUCCESS;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (isCancelled())
				return;
			switch (result) {
			case RESULT_FAIL:
				Util.toaster(R.string.request_fail);
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
