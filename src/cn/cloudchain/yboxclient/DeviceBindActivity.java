package cn.cloudchain.yboxclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.ServerHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;
import cn.cloudchain.yboxclient.utils.PreferenceUtil;
import cn.cloudchain.yboxclient.utils.Util;

public class DeviceBindActivity extends ActionBarActivity {
	public static final String BUNDLE_DEVICE_MAC = "device_mac";
	private TextView deviceMacText;
	private ImageView deviceBindView;
	private View unloginView;

	private MyHandler handler = new MyHandler(this);
	private String mac;
	private boolean isBind = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.device_bind_title);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_device_bind);

		unloginView = this.findViewById(R.id.unlogin_layout);
		deviceMacText = (TextView) this.findViewById(R.id.device_mac);
		deviceBindView = (ImageView) this.findViewById(R.id.device_bind);

		deviceBindView.setOnClickListener(new BindClickListener());
		mac = getIntent().getStringExtra(BUNDLE_DEVICE_MAC);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_bind, menu);
		boolean hasLogin = PreferenceUtil.getInt(
				PreferenceUtil.PREF_ACCOUNT_ID, -1) > 0;
		menu.findItem(R.id.login).setVisible(!hasLogin);
		menu.findItem(R.id.logout).setVisible(hasLogin);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case R.id.login:
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			break;
		case R.id.logout:
			PreferenceUtil.remove(PreferenceUtil.PREF_ACCOUNT_ID);
			PreferenceUtil.remove(PreferenceUtil.BIND_DEVICES);
			refreshViews();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshViews();
	}

	private void refreshViews() {
		boolean isLogin = PreferenceUtil.getInt(PreferenceUtil.PREF_ACCOUNT_ID,
				-1) > 0;
		unloginView.setVisibility(isLogin ? View.GONE : View.VISIBLE);
		deviceBindView.setEnabled(isLogin && !TextUtils.isEmpty(mac));

		supportInvalidateOptionsMenu();

		if (TextUtils.isEmpty(mac)) {
			deviceMacText.setText(R.string.unknow);
			deviceBindView.setImageResource(R.drawable.device_bind_uncheck);
		} else {
			deviceMacText.setText(mac);
			isBind = PreferenceUtil.hasDeviceMac(mac);
			deviceBindView
					.setImageResource(isBind ? R.drawable.device_bind_checked
							: R.drawable.device_bind_uncheck);
		}
	}

	private class BindClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			String message = isBind ? getString(R.string.device_unbinding)
					: getString(R.string.device_binding);
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment(message, true);
			DeviceBindTask task = new DeviceBindTask(mac, !isBind);
			fragment.setTask(task);
			fragment.show(getSupportFragmentManager(), null);
			deviceBindView.setClickable(false);
		}
	}

	private static class MyHandler extends WeakHandler<DeviceBindActivity> {
		private static final int DEVICE_BIND_COMPLETE = 0;
		private static final int DEVICE_UNBIND_COMPLETE = 1;

		public MyHandler(DeviceBindActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			getOwner().deviceBindView.setClickable(true);
			switch (msg.what) {
			case DEVICE_BIND_COMPLETE:
				if (msg.arg1 == YunmaoException.ERROR_CODE_NONE) {
					getOwner().deviceBindView
							.setImageResource(R.drawable.device_bind_checked);
					Util.toaster(R.string.device_bind_success);
				} else {
					Util.toaster(R.string.device_bind_fail);
				}
				break;
			case DEVICE_UNBIND_COMPLETE:
				if (msg.arg1 == YunmaoException.ERROR_CODE_NONE) {
					getOwner().deviceBindView
							.setImageResource(R.drawable.device_bind_uncheck);
					Util.toaster(R.string.device_unbind_success);
				} else {
					Util.toaster(R.string.device_unbind_fail);
				}
				break;
			}
		}
	}

	private class DeviceBindTask extends BaseFragmentTask {
		private String mac;
		private boolean bind;

		private DeviceBindTask(String mac, boolean bind) {
			this.mac = mac;
			this.bind = bind;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			int errorCode = YunmaoException.ERROR_CODE_DEFAULT;
			try {
				ServerHelper.getInstance().postForBindDevice(mac, bind);
			} catch (YunmaoException e) {
				errorCode = e.getErrorCode();
			}
			// 如果请求成功，更新本地绑定状态
			if (errorCode == YunmaoException.ERROR_CODE_NONE) {
				if (bind) {
					PreferenceUtil.appendDeviceMac(mac);
				} else {
					PreferenceUtil.removeDeviceMac(mac);
				}
			}
			Message msg = handler
					.obtainMessage(bind ? MyHandler.DEVICE_BIND_COMPLETE
							: MyHandler.DEVICE_UNBIND_COMPLETE);
			msg.arg1 = errorCode;
			handler.sendMessage(msg);
			return 0;
		}
	}

}
