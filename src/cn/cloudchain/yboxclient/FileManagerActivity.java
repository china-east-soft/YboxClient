package cn.cloudchain.yboxclient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import cn.cloudchain.yboxclient.adapter.FileListAdapter;
import cn.cloudchain.yboxclient.bean.FileBean;
import cn.cloudchain.yboxclient.dialog.CustomDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.helper.FilesOperationHandler;
import cn.cloudchain.yboxclient.task.FilesOperationTask;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class FileManagerActivity extends ActionBarActivity {
	final String TAG = FileManagerActivity.class.getSimpleName();
	private PullToRefreshListView pullToRefreshListView;
	private ListView listView;
	private ProgressBar progressBar;

	private FileListAdapter adapter;
	private MyHandler handler = new MyHandler(this);

	private enum ActionType {
		NONE, EDIT, DELETE
	}

	private ActionType currentType = ActionType.NONE;
	private String currentFilePath = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.tab_storage);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.base_pulltorefresh_listview);
		pullToRefreshListView = (PullToRefreshListView) this
				.findViewById(R.id.base_pulltorefreshlist);
		pullToRefreshListView.setMode(Mode.PULL_FROM_START);
		pullToRefreshListView.setOnRefreshListener(new MyRefreshListener());

		progressBar = (ProgressBar) this.findViewById(R.id.progressbar);
		listView = pullToRefreshListView.getRefreshableView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setOnItemClickListener(new MyItemClickListener());

		adapter = new FileListAdapter(this);
		listView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		switch (currentType) {
		case NONE:
			getMenuInflater().inflate(R.menu.files, menu);
			break;
		default:
			getMenuInflater().inflate(R.menu.files_edit, menu);
			break;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.edit:
			changeActionMode(ActionType.EDIT);
			break;
		case R.id.delete:
			changeActionMode(ActionType.DELETE);
			break;
		case R.id.accept:
			showConfirmDialog();
			break;
		case R.id.cancel:
			changeActionMode(ActionType.NONE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		handleFileLoad(true, currentFilePath);
	}

	private void changeActionMode(ActionType type) {
		if (currentType != type) {
			currentType = type;
			supportInvalidateOptionsMenu();
			adapter.showCheckBox(currentType != ActionType.NONE);
			listView.clearChoices();
			adapter.notifyDataSetChanged();
		}
	}

	private void showConfirmDialog() {
		CustomDialogFragment fragment = CustomDialogFragment.newInstance(-1,
				R.string.file_delete_reminder, R.string.confirm,
				R.string.cancel, true);
		fragment.setDialogService(new IDialogService() {

			@Override
			public void onClick(DialogFragment fragment, int actionId) {
				switch (actionId) {
				case R.id.dialog_click_positive:
					String[] paths = getCheckedFilePaths();
					handleFileDelete(paths);
					break;
				}
			}

			@Override
			public View getDialogView() {
				return null;
			}
		});
		fragment.show(getSupportFragmentManager(), null);
	}

	private String[] getCheckedFilePaths() {
		SparseBooleanArray checkList = listView.getCheckedItemPositions();
		String[] paths = new String[checkList.size()];
		int j = 0;
		for (int i = 0; i < checkList.size(); i++) {
			int position = checkList.keyAt(i);
			if (position > 0 && checkList.get(position)) {
				FileBean bean = adapter.getItem(position - 1);
				paths[j] = bean.absolutePath;
				++j;
			}
		}
		return paths;
	}

	private static class MyHandler extends
			FilesOperationHandler<FileManagerActivity> {

		public MyHandler(FileManagerActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null) {
				return;
			}
			switch (msg.what) {
			case REQUEST_COMPLETE:
				getOwner().pullToRefreshListView.onRefreshComplete();
				break;
			case FILE_LOAD_SUCESS: {
				Bundle data = msg.getData();
				ArrayList<FileBean> files = null;
				if (data != null) {
					files = data.getParcelableArrayList("files");
				}
				getOwner().handleFileLoadSuccess(files);
				break;
			}
			case FILE_DELETE_SUCCESS: {
				getOwner().handleFileLoad(true, getOwner().currentFilePath);
				break;
			}
			}
		}
	}

	/**
	 * 处理删除文件
	 * 
	 * @param paths
	 */
	private void handleFileDelete(String[] paths) {
		changeActionMode(ActionType.NONE);
		if (paths == null || paths.length == 0)
			return;

		FilesOperationTask task = new FilesOperationTask(handler, 1, false,
				paths);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				"正在处理中...", false);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), null);
	}

	/**
	 * 处理文件列表加载
	 * 
	 * @param showProgess
	 * @param path
	 */
	private void handleFileLoad(boolean showProgess, String path) {
		currentFilePath = path;
		FilesOperationTask task = new FilesOperationTask(handler, 0, false,
				path);
		if (showProgess) {
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment("正在加载...", true);
			fragment.setTask(task);
			fragment.show(getSupportFragmentManager(), null);
		} else {
			task.execute();
		}
	}

	private void handleFileLoadSuccess(List<FileBean> files) {
		adapter.setFileList(files);
		adapter.notifyDataSetChanged();
	}

	private class MyRefreshListener implements OnRefreshListener<ListView> {

		@Override
		public void onRefresh(PullToRefreshBase<ListView> refreshView) {
			handleFileLoad(false, currentFilePath);
		}
	}

	private class MyItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// 0位置为pullToRefreshView
			if (currentType != ActionType.NONE || arg2 == 0) {
				return;
			}
			// 点击返回按钮，
			if (arg2 == 1) {
				if (!TextUtils.isEmpty(currentFilePath)) {
					File file = new File(currentFilePath);
					String parentFile = file.getParent();
					handleFileLoad(true, parentFile);
				}
				return;
			}

			FileBean bean = adapter.getItem(arg2 - 1);
			if (bean.isDirectory) {
				handleFileLoad(true, bean.absolutePath);
			}

		}
	}

}
