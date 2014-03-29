package cn.cloudchain.yboxclient.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.MyApplication;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.VideoPlayerActivity;
import cn.cloudchain.yboxclient.adapter.FileListAdapter;
import cn.cloudchain.yboxclient.bean.FileBean;
import cn.cloudchain.yboxclient.dialog.CustomDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.face.IMenuItemListener;
import cn.cloudchain.yboxclient.helper.FilesOperationHandler;
import cn.cloudchain.yboxclient.task.FilesOperationTask;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxcommon.bean.Types;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class FileManagerFragment extends Fragment implements IMenuItemListener {
	public static final String BUNDLE_TYPE = "type";
	private PullToRefreshListView pullToRefreshListView;
	private ListView listView;

	private FileListAdapter adapter;
	private MyHandler handler = new MyHandler(this);
	private int type = Types.FILE_ALL;

	private String currentFilePath = "";
	private ActionType currentType = ActionType.NONE;
	private IMenuItemListener parentMenuListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof IMenuItemListener) {
			parentMenuListener = (IMenuItemListener) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getInt(BUNDLE_TYPE);
			LogUtil.i(getTag(), "type = " + type);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.base_pulltorefresh_listview,
				container, false);
		pullToRefreshListView = (PullToRefreshListView) view
				.findViewById(R.id.base_pulltorefreshlist);
		pullToRefreshListView.setMode(Mode.PULL_FROM_START);
		pullToRefreshListView.setOnRefreshListener(new MyRefreshListener());
		pullToRefreshListView.setEnabled(true);

		listView = pullToRefreshListView.getRefreshableView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setOnItemClickListener(new MyItemClickListener());

		adapter = new FileListAdapter(getActivity());
		listView.setAdapter(adapter);
		TextView emptyView = new TextView(getActivity());
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		emptyView.setText("文件为空");
		emptyView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		emptyView.setPadding(0, 30, 0, 30);
		listView.setEmptyView(emptyView);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		handleFileLoad(true, currentFilePath);
	}

	private void showConfirmDialog(final String... paths) {
		CustomDialogFragment fragment = CustomDialogFragment.newInstance(-1,
				R.string.file_delete_reminder, R.string.confirm,
				R.string.cancel, true);
		fragment.setDialogService(new IDialogService() {

			@Override
			public void onClick(DialogFragment fragment, int actionId) {
				switch (actionId) {
				case R.id.dialog_click_positive:
					handleFileDelete(paths);
					break;
				}
			}

			@Override
			public View getDialogView() {
				return null;
			}
		});
		fragment.show(getFragmentManager(), null);
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
			FilesOperationHandler<FileManagerFragment> {

		public MyHandler(FileManagerFragment owner) {
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
					files = data.getParcelableArrayList(BUNDLE_FILES);
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
		if (parentMenuListener != null) {
			parentMenuListener.changeActionMode(ActionType.NONE);
		}
		if (paths == null || paths.length == 0)
			return;

		FilesOperationTask task = new FilesOperationTask(handler, false, paths);
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				"正在处理中...", false);
		fragment.setTask(task);
		fragment.show(getFragmentManager(), null);
	}

	/**
	 * 处理文件列表加载
	 * 
	 * @param showProgess
	 * @param path
	 */
	private void handleFileLoad(boolean showProgess, String path) {
		currentFilePath = path;
		FilesOperationTask task = new FilesOperationTask(handler, type, false,
				path);
		if (showProgess) {
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment("正在加载...", true);
			fragment.setTask(task);
			fragment.show(getFragmentManager(), null);
		} else {
			task.execute();
		}
	}

	private void handleFileLoadSuccess(List<FileBean> files) {
		LogUtil.i(getTag(), "files size = "
				+ (files == null ? 0 : files.size()));
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
			FileBean bean = adapter.getItem(arg2 - 1);
			if (bean.isDirectory) {
				handleFileLoad(true, bean.absolutePath);
			} else {
				String gateway = MyApplication.getInstance().gateway;
				if (!TextUtils.isEmpty(gateway)) {
					String url = String.format("http://%s:%s/%s", gateway,
							Constants.MIDDLE_HTTP_PORT, bean.absolutePath);
					VideoPlayerActivity.start(getActivity(), url);
				}
			}

		}
	}

	@Override
	public void changeActionMode(ActionType type) {
		currentType = type;
		adapter.showCheckBox(type != ActionType.NONE);
		listView.clearChoices();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onMenuItemClick(ActionType type) {
		String[] paths = getCheckedFilePaths();
		if (paths != null && paths.length > 0) {
			showConfirmDialog(paths);
		}
	}
}
