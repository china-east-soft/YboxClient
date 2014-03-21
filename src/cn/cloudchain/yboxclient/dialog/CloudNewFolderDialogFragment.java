package cn.cloudchain.yboxclient.dialog;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.CloudHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.utils.Util;

import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileInfoListener;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileInfoResult;

/**
 * 公众云创建文件夹对话框
 * 
 * @author lazzy
 * 
 */
public class CloudNewFolderDialogFragment extends DialogFragment {
	private String parentPath;
	private View progressView;
	private Button negitiveButton;
	private Button positiveButton;
	private EditText folderNameText;

	private MyHandler handler = new MyHandler(this);
	private IFolderNewSuccess listener;

	public static interface IFolderNewSuccess {
		public void onSuccess();
	}

	public static CloudNewFolderDialogFragment newInstance(String parentPath) {
		CloudNewFolderDialogFragment fragment = new CloudNewFolderDialogFragment();
		Bundle args = new Bundle();
		args.putString("parent", parentPath);
		fragment.setArguments(args);
		return fragment;
	}

	public void setFolderNewSuccess(IFolderNewSuccess listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		parentPath = getArguments().getString("parent");
		setStyle(STYLE_NORMAL, R.style.CommonDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_new_folder, container,
				false);
		progressView = view.findViewById(R.id.progress_lay);
		negitiveButton = (Button) view.findViewById(R.id.dialog_negtive_button);
		positiveButton = (Button) view
				.findViewById(R.id.dialog_positive_button);
		folderNameText = (EditText) view.findViewById(R.id.folder_name);
		negitiveButton.setOnClickListener(new MyClickListener());
		positiveButton.setOnClickListener(new MyClickListener());
		return view;
	}

	private class MyClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialog_positive_button:
				handler.sendEmptyMessage(MyHandler.NEW_FOLDER);
				break;
			case R.id.dialog_negtive_button:
				dismiss();
				break;
			}
		}
	}

	private static class MyHandler extends
			WeakHandler<CloudNewFolderDialogFragment> {
		private static final int NEW_FOLDER = 0;
		private static final int NEW_FOLDER_SUCCESS = 1;
		private static final int NEW_FOLDER_FAIL = 2;

		public MyHandler(CloudNewFolderDialogFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case NEW_FOLDER:
				getOwner().handleNewFolder();
				break;
			case NEW_FOLDER_SUCCESS:
				getOwner().handleNewFolderComplete(true);
				break;
			case NEW_FOLDER_FAIL:
				getOwner().handleNewFolderComplete(false);
				break;
			}
		}
	}

	private void handleNewFolderComplete(boolean success) {
		if (success) {
			dismiss();
			if (listener != null) {
				listener.onSuccess();
			}
		} else {
			Util.toaster("创建目录失败", null);
			positiveButton.setEnabled(true);
			negitiveButton.setEnabled(true);
			folderNameText.setEnabled(true);
			setCancelable(true);
			progressView.setVisibility(View.GONE);
		}
	}

	private void handleNewFolder() {
		String folderName = folderNameText.getText().toString().trim();
		if (TextUtils.isEmpty(folderName)) {
			folderName = folderNameText.getHint().toString();
		}
		positiveButton.setEnabled(false);
		negitiveButton.setEnabled(false);
		folderNameText.setEnabled(false);
		setCancelable(false);
		progressView.setVisibility(View.VISIBLE);

		CloudHelper.getInstance().createDir(
				String.format("%s/%s", parentPath, folderName),
				new FileInfoListener() {

					@Override
					public void onSuccess(FileInfoResult arg0) {
						handler.sendEmptyMessage(MyHandler.NEW_FOLDER_SUCCESS);
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						handler.sendEmptyMessage(MyHandler.NEW_FOLDER_FAIL);
					}
				});
	}

}
