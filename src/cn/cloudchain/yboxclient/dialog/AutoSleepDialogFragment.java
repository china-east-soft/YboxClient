package cn.cloudchain.yboxclient.dialog;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.Util;
import cn.cloudchain.yboxclient.helper.WeakHandler;

public class AutoSleepDialogFragment extends DialogFragment {
	private int oldType;
	private int newType;

	private MyHandler handler = new MyHandler(this);

	public static AutoSleepDialogFragment newInstance(int currentType) {
		AutoSleepDialogFragment fragment = new AutoSleepDialogFragment();
		Bundle args = new Bundle();
		args.putInt("type", currentType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		oldType = getArguments().getInt("type");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.setting_auto_sleep);
		builder.setSingleChoiceItems(R.array.auto_sleep_types, oldType,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						newType = which;
					}
				});
		builder.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (oldType != newType) {
							setAutoSleepType(newType);
						}
					}
				});
		builder.setCancelable(true);
		return builder.show();
	}

	private void setAutoSleepType(final int type) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = SetHelper.getInstance().setAutoSleepType(type);
				int what = MyHandler.RESULT_FAIL;
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.optBoolean("result")) {
						what = MyHandler.RESULT_SUCCESS;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Log.i("ss", "whta = " + what);
				handler.sendEmptyMessage(what);
			}
		}).start();
	}

	private static class MyHandler extends WeakHandler<AutoSleepDialogFragment> {
		private static final int RESULT_SUCCESS = 0;
		private static final int RESULT_FAIL = 1;

		public MyHandler(AutoSleepDialogFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null) {
				return;
			}

			switch (msg.what) {
			case RESULT_FAIL:
				Util.toaster("设置失败", null);
				break;
			case RESULT_SUCCESS:
				Util.toaster("设置成功", null);
				break;
			}
		}

	}

}
