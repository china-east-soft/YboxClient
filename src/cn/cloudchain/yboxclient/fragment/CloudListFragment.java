package cn.cloudchain.yboxclient.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.adapter.FileListAdapter;
import cn.cloudchain.yboxclient.bean.FileBean;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.CloudHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;

import com.baidu.frontia.api.FrontiaPersonalStorage;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileInfoResult;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileListListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class CloudListFragment extends Fragment {
	final String TAG = CloudListFragment.class.getSimpleName();
	private PullToRefreshListView pullToRefreshListView;
	private ListView listView;

	private FileListAdapter adapter;
	private MyHandler handler = new MyHandler(this);

	private String currentFilePath = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.base_pulltorefresh_listview,
				container, false);
		pullToRefreshListView = (PullToRefreshListView) view
				.findViewById(R.id.base_pulltorefreshlist);
		pullToRefreshListView.setMode(Mode.PULL_FROM_START);
		pullToRefreshListView.setOnRefreshListener(new MyRefreshListener());

		listView = pullToRefreshListView.getRefreshableView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		adapter = new FileListAdapter(getActivity());
		listView.setAdapter(adapter);

		setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		handleFileLoad(true, currentFilePath);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// inflater.inflate(R.menu.files, menu);
		menu.add("上传");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private static class MyHandler extends WeakHandler<CloudListFragment> {
		private static final int LIST_LOAD_SUCCESS = 1;
		private static final int LIST_LOAD_FAIL = 2;

		public MyHandler(CloudListFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null) {
				return;
			}
			switch (msg.what) {
			case LIST_LOAD_SUCCESS: {
				getOwner().pullToRefreshListView.onRefreshComplete();
				Bundle data = msg.getData();
				ArrayList<FileBean> files = null;
				if (data != null) {
					files = data.getParcelableArrayList("files");
				}
				getOwner().handleFileLoadSuccess(files);
				break;
			}
			case LIST_LOAD_FAIL: {
				getOwner().pullToRefreshListView.onRefreshComplete();
				Util.toaster("加载失败", null);
				break;
			}
			}
		}
	}

	private void handleFileLoad(boolean showProgess, String path) {
		currentFilePath = path;
		if (showProgess) {
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment("正在加载...", true);
			fragment.show(getFragmentManager(), null);
		}
		CloudHelper.getInstance().list("/", FrontiaPersonalStorage.BY_NAME,
				FrontiaPersonalStorage.ORDER_ASC, new FileListListener() {

					@Override
					public void onSuccess(List<FileInfoResult> arg0) {
						ArrayList<FileBean> files = null;
						if (arg0 != null) {
							files = new ArrayList<FileBean>(arg0.size());
							for (FileInfoResult info : arg0) {
								FileBean bean = new FileBean();
								bean.convertFrom(info);
								files.add(bean);
							}
						}

						Bundle data = new Bundle();
						data.putParcelableArrayList("files", files);
						Message msg = handler
								.obtainMessage(MyHandler.LIST_LOAD_SUCCESS);
						msg.setData(data);
						handler.sendMessage(msg);
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						LogUtil.i(TAG,
								String.format("Code: %d Msg: %s", arg0, arg1));
						handler.sendEmptyMessage(MyHandler.LIST_LOAD_FAIL);
					}
				});
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

}
