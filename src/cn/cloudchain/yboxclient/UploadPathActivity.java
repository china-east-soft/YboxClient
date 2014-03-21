package cn.cloudchain.yboxclient;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.adapter.CloudFileListAdapter;
import cn.cloudchain.yboxclient.bean.FileBean;
import cn.cloudchain.yboxclient.dialog.CloudNewFolderDialogFragment;
import cn.cloudchain.yboxclient.helper.CloudHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.utils.Util;

import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileInfoResult;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileListListener;

/**
 * 选择上传至公众云的地址，所以目录是从云上获取的而非本地目录
 * 
 * @author lazzy
 * 
 */
public class UploadPathActivity extends ActionBarActivity {
	final String TAG = UploadPathActivity.class.getSimpleName();
	public static final String BUNDLE_PATH = "path";
	private static final String BUNDLE_FILES = "files";

	private LinearLayout pathScrollView;
	private ListView listView;
	private Button pathConfirm;

	private CloudFileListAdapter adapter;
	private String currentPath;

	private MyHandler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getSupportActionBar().setTitle(R.string.uplpad_path_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		currentPath = getIntent().getStringExtra(BUNDLE_PATH);

		setContentView(R.layout.layout_upload_path);
		pathScrollView = (LinearLayout) this
				.findViewById(R.id.path_history_view);
		listView = (ListView) this.findViewById(R.id.path_list);
		pathConfirm = (Button) this.findViewById(R.id.path_confirm);

		adapter = new CloudFileListAdapter(this);
		listView.setAdapter(adapter);

		MyClickListener clickListener = new MyClickListener();
		pathConfirm.setOnClickListener(clickListener);
		this.findViewById(R.id.path_dismiss).setOnClickListener(clickListener);

		refreshHistoryPath();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Message msg = handler.obtainMessage(MyHandler.LIST_FOLDER);
		msg.obj = currentPath;
		handler.sendMessage(msg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.file_upload, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.newfold:
			CloudNewFolderDialogFragment fragment = CloudNewFolderDialogFragment
					.newInstance(currentPath);
			fragment.setFolderNewSuccess(new CloudNewFolderDialogFragment.IFolderNewSuccess() {

				@Override
				public void onSuccess() {
					// 新建文件夹成功，则刷新页面
					Message msg = handler.obtainMessage(MyHandler.LIST_FOLDER);
					msg.obj = currentPath;
					handler.sendMessage(msg);
				}
			});
			fragment.show(getSupportFragmentManager(), null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 更新目录历史栏
	 */
	private void refreshHistoryPath() {
		pathScrollView.removeAllViews();
		StringTokenizer tokenizer = new StringTokenizer(currentPath, "/");
		String currentFolder = "";
		while (tokenizer.hasMoreTokens()) {
			currentFolder = tokenizer.nextToken();
			TextView textView = new TextView(this);
			textView.setGravity(Gravity.CENTER);
			textView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			int padding = Util.convertDpToPx(8);
			textView.setCompoundDrawablePadding(padding);
			textView.setPadding(padding, 0, 0, 0);
			textView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.folder_arrow, 0);
			textView.setText(currentFolder);
			textView.setOnClickListener(new FoldClickListener());
			pathScrollView.addView(textView);
		}

		pathConfirm.setText(currentFolder);
	}

	private static class MyHandler extends WeakHandler<UploadPathActivity> {
		private final static int LIST_FOLDER = 0;
		private final static int LIST_FOLDER_SUCCESS = 1;
		private final static int LIST_FOLDER_FAIL = 2;

		public MyHandler(UploadPathActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case LIST_FOLDER:
				getOwner().handleListFolder((String) msg.obj);
				break;
			case LIST_FOLDER_SUCCESS:
				String path = msg.getData().getString(BUNDLE_PATH);
				ArrayList<FileBean> files = msg.getData()
						.getParcelableArrayList(BUNDLE_FILES);
				getOwner().handleListFolderSuccess(path, files);
				break;
			case LIST_FOLDER_FAIL:
				getOwner().handleListFolderFail();
				break;
			}
		}
	}

	/**
	 * 获取该目录下的目录列表
	 * 
	 * @param path
	 */
	private void handleListFolder(final String path) {
		setSupportProgressBarIndeterminateVisibility(true);
		CloudHelper.getInstance().list(path, new FileListListener() {

			@Override
			public void onSuccess(List<FileInfoResult> arg0) {
				Message msg = handler
						.obtainMessage(MyHandler.LIST_FOLDER_SUCCESS);

				Bundle data = new Bundle();
				ArrayList<FileBean> files = null;
				if (arg0 != null) {
					int size = arg0.size();
					files = new ArrayList<FileBean>(size);
					for (FileInfoResult result : arg0) {
						FileBean bean = new FileBean();
						bean.convertFrom(result);
						// 只需要列出所有的文件夹
						if (bean.isDirectory) {
							files.add(bean);
						}
					}
				}
				data.putString(BUNDLE_PATH, path);
				if (files != null) {
					data.putParcelableArrayList(BUNDLE_FILES, files);
				}
				msg.setData(data);
				handler.sendMessage(msg);
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				handler.sendEmptyMessage(MyHandler.LIST_FOLDER_FAIL);
			}
		});
	}

	/**
	 * 获取目录列表成功
	 * 
	 * @param path
	 * @param files
	 */
	private void handleListFolderSuccess(String path, List<FileBean> files) {
		setSupportProgressBarIndeterminateVisibility(false);
		currentPath = path;
		refreshHistoryPath();
		adapter.setFileList(files);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 获取目录列表失败
	 */
	private void handleListFolderFail() {
		setSupportProgressBarIndeterminateVisibility(false);
		Util.toaster(R.string.request_fail);
	}

	/**
	 * 处理取消/提交两按钮的点击事件
	 * 
	 * @author lazzy
	 * 
	 */
	private class MyClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.path_dismiss:
				setResult(Activity.RESULT_CANCELED);
				finish();
				break;
			case R.id.path_confirm:
				Intent data = new Intent();
				data.putExtra(BUNDLE_PATH, currentPath);
				setResult(Activity.RESULT_OK, data);
				finish();
				break;
			}
		}
	}

	/**
	 * 处理文件浏览历史目录中的点击
	 * 
	 * @author lazzy
	 * 
	 */
	private class FoldClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v instanceof TextView) {
				TextView folderView = (TextView) v;
				String folderName = folderView.getText().toString();
				int index = currentPath.indexOf(folderName);
				if (index < 0) {
					return;
				}
				String newPath = currentPath.substring(0,
						index + folderName.length());
				if (!newPath.equals(currentPath)) {
					Message msg = handler.obtainMessage(MyHandler.LIST_FOLDER);
					msg.obj = newPath;
					handler.sendMessage(msg);
				}
			}
		}
	}
}
