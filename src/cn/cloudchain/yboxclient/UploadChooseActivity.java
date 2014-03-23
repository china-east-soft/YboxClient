package cn.cloudchain.yboxclient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.adapter.CloudFileListAdapter;
import cn.cloudchain.yboxclient.bean.FileBean;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.server.UploadService;
import cn.cloudchain.yboxclient.utils.Util;

public class UploadChooseActivity extends ActionBarActivity {
	final String TAG = UploadChooseActivity.class.getSimpleName();
	public static final String BUNDLE_PATH = "path";
	private final int REQUEST_CODE_PATH = 2;

	private LinearLayout pathScrollView;
	private ListView listView;
	// 选择上传地址的按钮
	private Button uploadPathButton;
	// 上传按钮
	private Button uploadButton;
	private CloudFileListAdapter adapter;

	private String currentCloudPath = "";
	private String currentPath = "";
	private boolean isSelectAll = false;
	private int maxFileNum = 0;
	private MyHandler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.upload_choose_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		currentCloudPath = getIntent().getStringExtra(BUNDLE_PATH);
		setContentView(R.layout.layout_upload_choose);

		pathScrollView = (LinearLayout) this
				.findViewById(R.id.path_history_view);
		uploadPathButton = (Button) this.findViewById(R.id.choose_cloud_path);
		uploadButton = (Button) this.findViewById(R.id.upload);

		listView = (ListView) this.findViewById(R.id.path_list);
		adapter = new CloudFileListAdapter(this);
		adapter.showCheckBox(true);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setFastScrollEnabled(true);
		listView.setOnItemClickListener(new ListItemClick());

		uploadPathButton.setOnClickListener(new ButtonClickListener());
		uploadButton.setOnClickListener(new ButtonClickListener());
		uploadButton.setEnabled(false);
		refreshUploadPath();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Message msg = handler.obtainMessage(MyHandler.FILE_LOAD);
		msg.obj = currentPath;
		handler.sendMessage(msg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.file_upload_choose, menu);
		menu.findItem(R.id.select_all).setIcon(
				isSelectAll ? R.drawable.login_checked
						: R.drawable.login_uncheck);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.select_all:
			listViewCheckAll(!isSelectAll);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 刷新当前目录
	 */
	private void refreshHistoryView() {
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
	}

	/**
	 * 刷新上传地址
	 */
	private void refreshUploadPath() {
		if (currentCloudPath == null) {
			currentCloudPath = "";
		}
		StringTokenizer tokenizer = new StringTokenizer(currentCloudPath, "/");
		String currentFolder = "";
		while (tokenizer.hasMoreTokens()) {
			currentFolder = tokenizer.nextToken();
		}
		uploadPathButton.setText(currentFolder);
	}

	private static class MyHandler extends WeakHandler<UploadChooseActivity> {
		private static final int FILE_LOAD = 0;
		private static final int FILE_LOAD_SUCCESS = 1;
		private static final int FILE_UPLOAD = 2;

		public MyHandler(UploadChooseActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case FILE_LOAD:
				getOwner().handleFileLoad((String) msg.obj);
				break;
			case FILE_LOAD_SUCCESS: {
				String path = msg.getData().getString("path");
				ArrayList<FileBean> files = msg.getData()
						.getParcelableArrayList("files");
				getOwner().handleFileLoadSuccess(path, files);
				break;
			}
			case FILE_UPLOAD:
				getOwner().handleFileUpload();
				break;
			}
		}

	}

	/**
	 * 文件列表获取成功，刷新ListView
	 * 
	 * @param path
	 * @param files
	 */
	private void handleFileLoadSuccess(String path, List<FileBean> files) {
		currentPath = path;
		refreshHistoryView();
		listView.clearChoices();
		adapter.setFileList(files);
		maxFileNum = adapter.getTotalFileNum();
		adapter.notifyDataSetChanged();
		if (isSelectAll) {
			isSelectAll = false;
			supportInvalidateOptionsMenu();
		}
	}

	/**
	 * 加载本地文件列表
	 * 
	 * @param path
	 */
	private void handleFileLoad(String path) {
		File directory = null;
		if (TextUtils.isEmpty(path)) {
			directory = Environment.getExternalStorageDirectory();
		} else {
			directory = new File(path);
		}

		if (directory == null || !directory.exists()
				|| !directory.isDirectory()) {
			return;
		}

		final File tempDir = directory;
		new Thread(new Runnable() {

			@Override
			public void run() {
				File[] listFiles = tempDir.listFiles();
				if (listFiles == null)
					return;

				ArrayList<FileBean> files = new ArrayList<FileBean>(
						listFiles.length);
				for (File file : listFiles) {
					FileBean bean = new FileBean();
					bean.convertFrom(file);
					files.add(bean);
				}
				Message msg = handler
						.obtainMessage(MyHandler.FILE_LOAD_SUCCESS);
				Bundle data = new Bundle();
				data.putParcelableArrayList("files", files);
				data.putString("path", tempDir.getPath());
				msg.setData(data);
				handler.sendMessage(msg);
			}
		}).start();

	}

	/**
	 * 上传文件
	 */
	private void handleFileUpload() {
		SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
		for (int i = 0; i < checkedItems.size(); i++) {
			int position = checkedItems.keyAt(i);
			if (!checkedItems.get(position)) {
				continue;
			}
			Intent intent = new Intent(this, UploadService.class);
			intent.putExtra("localPath", adapter.getItem(position).absolutePath);
			intent.putExtra("remotePath", currentCloudPath);
			startService(intent);
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == REQUEST_CODE_PATH && arg1 == Activity.RESULT_OK
				&& arg2 != null) {
			currentCloudPath = arg2
					.getStringExtra(UploadPathActivity.BUNDLE_PATH);
			refreshUploadPath();
		}
	}

	/**
	 * ListView点击时间，如果是文件夹则进入该文件夹，如果点击的是文件，则更新上传
	 * 
	 * @author apple
	 * 
	 */
	private class ListItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			FileBean bean = adapter.getItem(arg2);
			if (bean == null)
				return;
			if (bean.isDirectory) {
				Message msg = handler.obtainMessage(MyHandler.FILE_LOAD);
				msg.obj = bean.absolutePath;
				handler.sendMessage(msg);
			} else {
				refreshUploadButton();
			}
		}
	}

	/**
	 * 控制ListView全选，当满足当前是文件格式时，执行全选和全不选
	 * 
	 * @param checkAll
	 *            true时全选
	 */
	private void listViewCheckAll(boolean checkAll) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItemViewType(i) == CloudFileListAdapter.TYPE_FILE) {
				listView.setItemChecked(i, checkAll);
			}
		}
		refreshUploadButton();
	}

	/**
	 * 获取当前选中的item个数
	 * 
	 * @return
	 */
	private int getCheckedItemCount() {
		int count = 0;
		SparseBooleanArray checkItems = listView.getCheckedItemPositions();
		for (int i = 0; i < checkItems.size(); i++) {
			if (checkItems.valueAt(i)) {
				++count;
			}
		}
		return count;
	}

	/**
	 * 刷新上传按钮状态，刷新全选按钮状态
	 */
	private void refreshUploadButton() {
		int checkedCount = getCheckedItemCount();
		uploadButton.setEnabled(checkedCount > 0);

		if (checkedCount > 0) {
			uploadButton.setEnabled(true);
			uploadButton.setText("上传(" + checkedCount + ")");
			if (!isSelectAll && checkedCount == maxFileNum) {
				isSelectAll = true;
				supportInvalidateOptionsMenu();
			} else if (isSelectAll && checkedCount < maxFileNum) {
				isSelectAll = false;
				supportInvalidateOptionsMenu();
			}
		} else {
			uploadButton.setEnabled(false);
			uploadButton.setText("上传");
			if (isSelectAll) {
				isSelectAll = false;
				supportInvalidateOptionsMenu();
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
					Message msg = handler.obtainMessage(MyHandler.FILE_LOAD);
					msg.obj = newPath;
					handler.sendMessage(msg);
				}
			}
		}
	}

	private class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.upload:
				handler.sendEmptyMessage(MyHandler.FILE_UPLOAD);
				break;
			case R.id.choose_cloud_path:
				Intent intent = new Intent(UploadChooseActivity.this,
						UploadPathActivity.class);
				intent.putExtra(UploadPathActivity.BUNDLE_PATH,
						currentCloudPath);
				startActivityForResult(intent, REQUEST_CODE_PATH);
				break;
			}
		}

	}

}
