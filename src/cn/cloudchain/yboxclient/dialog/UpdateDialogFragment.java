package cn.cloudchain.yboxclient.dialog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.BroadcastService;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;

public class UpdateDialogFragment extends DialogFragment implements
		OnClickListener {
	private final String TAG = UpdateDialogFragment.class.getSimpleName();

	private TextView mMessage;
	private Button mPositive;
	private Button mNegitive;
	private ProgressBar progressBar;

	private MyHandler handler = new MyHandler(this);

	private boolean isBound = false;
	private YboxUpdateBroadcast broadcast;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			isBound = true;
		}
	};

	private String imageUrl;
	private String middleUrl;

	public static UpdateDialogFragment newInstance(String middleUrl,
			String imageUrl) {
		UpdateDialogFragment fragment = new UpdateDialogFragment();
		Bundle args = new Bundle();
		if (!TextUtils.isEmpty(imageUrl)) {
			args.putString("imageUrl", imageUrl);
		}
		if (!TextUtils.isEmpty(middleUrl)) {
			args.putString("middleUrl", middleUrl);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageUrl = getArguments().getString("imageUrl");
		middleUrl = getArguments().getString("middleUrl");
		setRetainInstance(true);
		setCancelable(true);
		setStyle(STYLE_NORMAL, R.style.CommonDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogUtil.i(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.dialog_update, container, false);

		mMessage = (TextView) v.findViewById(R.id.dialog_message);
		mPositive = (Button) v.findViewById(R.id.dialog_positive_button);
		mNegitive = (Button) v.findViewById(R.id.dialog_negtive_button);
		progressBar = (ProgressBar) v.findViewById(R.id.dialog_progressbar);
		mPositive.setText("取消");
		mNegitive.setText("立即升级");
		mNegitive.setVisibility(View.GONE);

		mPositive.setOnClickListener(this);
		mNegitive.setOnClickListener(this);

		if (!isBound) {
			Intent service = new Intent(getActivity(), BroadcastService.class);
			getActivity().bindService(service, serviceConnection,
					Context.BIND_AUTO_CREATE);
		}
		if (broadcast == null) {
			broadcast = new YboxUpdateBroadcast();
		}
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				broadcast,
				new IntentFilter(BroadcastService.ACTION_RECEIVED_RESULT));
		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_positive_button:
			dismiss();
			break;
		case R.id.dialog_negtive_button:
			handleUpdateSend();
			break;
		default:
			break;
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		LogUtil.i(TAG, "onDismiss");
		if (isBound) {
			getActivity().unbindService(serviceConnection);
			isBound = false;
		}
		if (broadcast != null) {
			LocalBroadcastManager.getInstance(getActivity())
					.unregisterReceiver(broadcast);
			broadcast = null;
		}
		super.onDismiss(dialog);
	}

	private static class MyHandler extends WeakHandler<UpdateDialogFragment> {
		private final static int RECEIVED_RESULT = 2;

		public MyHandler(UpdateDialogFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;
			switch (msg.what) {
			case RECEIVED_RESULT:
				String message = "";
				if (msg.getData() != null) {
					message = msg.getData().getString(
							BroadcastService.BUNDLE_MESSAGE);
				}
				if (!TextUtils.isEmpty(message)) {
					getOwner().handleReceiveMessage(message);
				}
				break;
			}
		}
	}

	private Map<String, Integer> progressMap = new HashMap<String, Integer>();

	/**
	 * 处理终端下载进度显示
	 * 
	 * @param message
	 */
	private void handleReceiveMessage(String message) {
		LogUtil.i(TAG, message);
		try {
			JSONObject obj = new JSONObject(message);
			String url = obj.getString("url");

			if (obj.has("error")) {
				int errorCode = obj.getInt("error");
				dealWithDownloadError(errorCode);
				return;
			}

			if (obj.optBoolean("complete")) {
				progressMap.put(url, 100);
			}

			if (obj.has("progress")) {
				int progress = obj.getInt("progress");
				progressMap.put(url, progress);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// 刷新下载进度条
		int totalProgress = 0;
		int count = 0;
		Iterator<Entry<String, Integer>> inter = progressMap.entrySet()
				.iterator();
		while (inter.hasNext()) {
			Entry<String, Integer> entry = inter.next();
			totalProgress += entry.getValue();
			++count;
		}
		if (count > 0) {
			progressBar.setProgress(totalProgress / count);
		}

		// enable升级按钮
		if (progressBar.getProgress() >= 100) {
			mNegitive.setVisibility(View.VISIBLE);
			mMessage.setText("下载完成，点击升级！");
		}
	}

	private void dealWithDownloadError(int errorCode) {
		dismiss();
		switch (errorCode) {
		case Errors.MEMORY_LOW:
			Util.toaster("终端SD卡空间不足", null);
			break;
		case Errors.SD_NOT_PRESENT:
			Util.toaster("终端未找到SD卡", null);
			break;
		case Errors.SD_NOT_WRITABLE:
			Util.toaster("终端SD卡不可写", null);
			break;
		case Errors.NETWORK_BLOCK:
			Util.toaster("请检查终端网络连接", null);
			break;
		case Errors.IO_ERROR:
			Util.toaster("下载更新失败", null);
			break;
		}
	}

	/**
	 * 升级终端请求
	 */
	private void handleUpdateSend() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				LogUtil.i(TAG, "update send");
				if (!TextUtils.isEmpty(middleUrl)
						|| !TextUtils.isEmpty(imageUrl)) {
					SetHelper.getInstance().yboxUpdate(imageUrl, middleUrl);
				}
			}
		}).start();

	}

	private class YboxUpdateBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					BroadcastService.ACTION_RECEIVED_RESULT)) {
				Message msg = handler.obtainMessage(MyHandler.RECEIVED_RESULT);
				msg.setData(intent.getExtras());
				handler.sendMessage(msg);
			}
		}

	}

	private class Errors {
		public static final int SD_NOT_PRESENT = 0;
		public static final int SD_NOT_WRITABLE = 1;

		public static final int NETWORK_BLOCK = 3;
		public static final int MEMORY_LOW = 5;
		public static final int IO_ERROR = 6;
	}

}
