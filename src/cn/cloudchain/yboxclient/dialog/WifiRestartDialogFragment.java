package cn.cloudchain.yboxclient.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.helper.SetHelper;

/**
 * 重启无线
 * 
 * @author lazzy
 * 
 */
public class WifiRestartDialogFragment extends CustomDialogFragment {

	public static DialogFragment newInstance() {
		return CustomDialogFragment.newInstance(-1,
				R.string.valid_after_wifi_restart, R.string.wifi_restart_now,
				R.string.wifi_restart_later, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDialogService(new MyDialogService());
	}

	private class MyDialogService implements IDialogService {
		@Override
		public void onClick(DialogFragment fragment, int actionId) {
			if (actionId == R.id.dialog_click_positive) {
				restartWifi();
			}
		}

		@Override
		public View getDialogView() {
			return null;
		}

		private void restartWifi() {
			new Thread(new Runnable() {

				@Override
				public void run() {
					SetHelper.getInstance().restartWifi();
				}
			}).start();
		}
	}

}
