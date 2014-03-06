package cn.cloudchain.yboxclient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import cn.cloudchain.yboxclient.adapter.ProgramGridAdapter;
import cn.cloudchain.yboxclient.bean.FrequencyBean;
import cn.cloudchain.yboxclient.bean.GuideBean;
import cn.cloudchain.yboxclient.bean.ProgramBean;
import cn.cloudchain.yboxclient.dialog.CustomDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.ApHelper;
import cn.cloudchain.yboxclient.helper.TVStatusHandler;
import cn.cloudchain.yboxclient.server.TvStatusReceiver;
import cn.cloudchain.yboxclient.server.TvStatusService;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;
import cn.cloudchain.yboxclient.task.RequestForVideoTask;
import cn.cloudchain.yboxclient.utils.CalendarUtil;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

public class ProgramActivity extends ActionBarActivity {
	final static String TAG = ProgramActivity.class.getSimpleName();
	private PullToRefreshGridView pullToRefreshGridView;
	private GridView gridView;
	private ProgramGridAdapter adapter;

	private boolean needRefreshEpg = false;
	private boolean isActivityStopped = false;
	private boolean isLoadingDirectoryList = false;

	private List<ProgramBean> mProgramList;
	private TVStatusServiceConnection tvStatusConn = new TVStatusServiceConnection();
	private MyTvStatusHandler tvStatusHandler = new MyTvStatusHandler(this);
	private TvStatusReceiver tvStatusReceiver;
	private boolean isBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.live_program_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_program);
		pullToRefreshGridView = (PullToRefreshGridView) this
				.findViewById(R.id.gridview);
		pullToRefreshGridView
				.setOnRefreshListener(new ProgramPullRefreshListener());
		initPullToRefresh();
		gridView = pullToRefreshGridView.getRefreshableView();
		gridView.setOnItemClickListener(new ProgramItemClickListener());
		adapter = new ProgramGridAdapter(this);
		gridView.setAdapter(adapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		isActivityStopped = true;
		Intent intent = new Intent(this, TvStatusService.class);
		bindService(intent, tvStatusConn, Context.BIND_AUTO_CREATE);

		if (tvStatusReceiver == null) {
			tvStatusReceiver = new TvStatusReceiver(tvStatusHandler);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(TvStatusReceiver.ACTION_REMOTE_EPG_CHANGE);
		filter.addAction(TvStatusReceiver.ACTION_SWITCH_STATE_CHANGE);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				tvStatusReceiver, filter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadingDirectoryList(true);
		tvStatusHandler.sendEmptyMessage(MyTvStatusHandler.SWITCH_STATE_CHANGE);
		refreshGuideInfo();
	}

	@Override
	protected void onStop() {
		isActivityStopped = true;
		pullToRefreshGridView.onRefreshComplete();
		if (isBound) {
			unbindService(tvStatusConn);
			isBound = false;
		}
		if (tvStatusReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					tvStatusReceiver);
			tvStatusReceiver = null;
		}
		super.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initPullToRefresh() {
		pullToRefreshGridView.setMode(Mode.PULL_FROM_START);
		ILoadingLayout loadingLayout = pullToRefreshGridView
				.getLoadingLayoutProxy(true, false);
		loadingLayout
				.setPullLabel(getString(R.string.program_pullrefresh_pull));
		loadingLayout
				.setRefreshingLabel(getString(R.string.program_pullrefresh_loading));
	}

	/**
	 * 刷新节目菜单显示
	 * 
	 */
	private void refreshGuideInfo() {
		if (adapter != null) {
			new LoadGuideInfoTask().execute();
		}

	}

	/**
	 * 
	 * @param isAutoLoad
	 *            true if need to load data again when data is empty, false then
	 *            must refresh data
	 */
	private void loadingDirectoryList(boolean isAutoLoad) {
		if (isActivityStopped || isLoadingDirectoryList) {
			return;
		}
		LogUtil.i(TAG, String.format("auto = %b loading=%b ", isAutoLoad,
				isLoadingDirectoryList));
		LoadFreqTask task = new LoadFreqTask();
		if (!isAutoLoad) {
			TaskDialogFragment taskFragment = TaskDialogFragment
					.newLoadingFragment("加载中...", true);
			taskFragment.setTask(task);
			taskFragment.show(getSupportFragmentManager(),
					TaskDialogFragment.TAG);
		} else if (isAutoLoad && checkNeedLoadDirectory()) {
			task.execute();
		}
	}

	/**
	 * 
	 * 用于刷新节目菜单
	 */
	private class LoadGuideInfoTask extends AsyncTask<Void, Void, Integer> {
		private static final int MSG_RELOAD_ADAPTER = 1;
		private static final int MSG_PROGRAMS_EMPTY = 2;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// if (mProgressBar != null && !mProgressBar.isShown()) {
			// mProgressBar.setVisibility(View.VISIBLE);
			// }
		}

		@Override
		protected Integer doInBackground(Void... params) {
			if (mProgramList == null || mProgramList.size() == 0)
				return MSG_PROGRAMS_EMPTY;

			mProgramList = refreshProgramListGuides();
			return MSG_RELOAD_ADAPTER;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			pullToRefreshGridView.onRefreshComplete();
			// if (mProgressBar != null && mProgressBar.isShown()) {
			// mProgressBar.setVisibility(View.INVISIBLE);
			// }
			LogUtil.i(TAG, "loadGuideTask result = " + result);
			switch (result) {
			case MSG_RELOAD_ADAPTER:
				adapter.setProgramList(mProgramList);
				adapter.notifyDataSetChanged();
				break;
			}
		}

		private List<ProgramBean> refreshProgramListGuides() {
			Calendar currentTime = CalendarUtil.getCurrentTime();
			List<ProgramBean> result = new ArrayList<ProgramBean>();
			List<ProgramBean> tempProgramList = new ArrayList<ProgramBean>(
					mProgramList);
			for (ProgramBean programBean : tempProgramList) {
				result.add(refreshProgramGuideByTime(programBean, currentTime));
			}
			return result;
		}

		private ProgramBean refreshProgramGuideByTime(ProgramBean program,
				Calendar currentTime) {
			ProgramBean resultBean = program;
			resultBean.setCurrentGuideIndex(-1);
			resultBean.setNextGuideIndex(-1);

			List<GuideBean> tempGuideList = program.getGuideList();

			if (tempGuideList == null) {
				return resultBean;
			}

			int currentGuideIndex = -1;
			int nextGuideIndex = -1;
			int guideSize = tempGuideList.size();
			for (int j = 0; j < guideSize; j++) {
				GuideBean bean = tempGuideList.get(j);
				String startStr = bean.getGuideStartTime();
				String endStr = bean.getGuideEndTime();
				if (TextUtils.isEmpty(startStr) || TextUtils.isEmpty(endStr)) {
					continue;
				}

				Calendar startTime = CalendarUtil
						.transString2Calendar(startStr);
				Calendar endTime = CalendarUtil.transString2Calendar(endStr);
				if (!currentTime.before(startTime)
						&& !currentTime.after(endTime)) {
					currentGuideIndex = j;
					if (j < guideSize - 1) {
						nextGuideIndex = j + 1;
					}
					break;
				} else {
					GuideBean nextBean = (j < (guideSize - 1) ? tempGuideList
							.get(j + 1) : null);
					if (nextBean == null) {
						break;
					}
					String nextStartStr = nextBean.getGuideStartTime();
					if (TextUtils.isEmpty(nextStartStr)) {
						continue;
					}
					Calendar nextStartTime = CalendarUtil
							.transString2Calendar(nextStartStr);
					if (currentTime.after(endTime)
							&& currentTime.before(nextStartTime)) {
						nextGuideIndex = j + 1;
						break;
					}

				}
			}

			resultBean.setCurrentGuideIndex(currentGuideIndex);
			resultBean.setNextGuideIndex(nextGuideIndex);

			return resultBean;
		}
	}

	private class LoadFreqTask extends BaseFragmentTask {
		private final int MSG_LOAD_DATA_SUCCESS = 0;
		private final int MSG_LOAD_DATA_FAIL = 1;

		@Override
		protected Integer doInBackground(Void... params) {
			int result = MSG_LOAD_DATA_FAIL;
			isLoadingDirectoryList = true;

			if (checkNeedLoadDirectory()) {
				LogUtil.i(TAG, "start load frequency");
				List<FrequencyBean> mDirectoryList = ApHelper.getInstance()
						.getFrequencyList(needRefreshEpg);
				if (mDirectoryList != null && mDirectoryList.size() > 0) {
					mProgramList = mDirectoryList.get(0).getProgramList();
					LogUtil.i(TAG, "start load frequency count = "
							+ mProgramList.size());
					result = MSG_LOAD_DATA_SUCCESS;
				}
				needRefreshEpg = false;
			} else {
				result = MSG_LOAD_DATA_SUCCESS;
			}

			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			isLoadingDirectoryList = false;
			switch (result) {
			case MSG_LOAD_DATA_FAIL:
				Util.toaster(R.string.program_load_fail);
				break;
			case MSG_LOAD_DATA_SUCCESS:
				refreshGuideInfo();
				break;
			}
		}

	}

	private boolean checkNeedLoadDirectory() {
		LogUtil.i(TAG, "madapter count = " + adapter.getCount());
		return needRefreshEpg || adapter.getCount() == 0;
	}

	private class ProgramPullRefreshListener implements
			OnRefreshListener<GridView> {

		@Override
		public void onRefresh(PullToRefreshBase<GridView> refreshView) {
			new LoadGuideInfoTask().execute();
		}

	}

	private class ProgramItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			int position = arg2 - 1;
			if (adapter == null || adapter.getCount() <= position) {
				return;
			}
			LogUtil.d(TAG, "item click position = " + position);
			ProgramBean bean = (ProgramBean) adapter.getItem(position);
			if (bean == null)
				return;
			int state = ApHelper.getInstance().getSwitchState(bean);

			int reminderRes = -1;
			switch (state) {
			case ApHelper.STATE_REACH_LIMIT:
				reminderRes = R.string.reach_limit;
				break;
			case ApHelper.STATE_STATUS_NULL:
				reminderRes = R.string.status_unknow;
				break;
			case ApHelper.STATE_SWITCH_DISABLE:
				reminderRes = R.string.program_not_availabe;
				break;
			}
			if (reminderRes > 0) {
				CustomDialogFragment fragment = CustomDialogFragment
						.newInstance(-1, reminderRes, R.string.iknow, -1, true);
				fragment.show(getSupportFragmentManager(),
						CustomDialogFragment.TAG);
			} else {
				RequestForVideoTask task = new RequestForVideoTask(
						ProgramActivity.this, bean);
				TaskDialogFragment fragment = TaskDialogFragment
						.newLoadingFragment("请求中...", true);
				fragment.setTask(task);
				fragment.show(getSupportFragmentManager(),
						TaskDialogFragment.TAG);
			}
		}
	}

	private static class MyTvStatusHandler extends
			TVStatusHandler<ProgramActivity> {

		public MyTvStatusHandler(ProgramActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null) {
				return;
			}
			switch (msg.what) {
			case SWITCH_STATE_CHANGE:
				if (getOwner().adapter != null) {
					getOwner().adapter.notifyDataSetChanged();
				}
				break;
			case REMOTE_EPG_CHANGE:
				getOwner().loadingDirectoryList(false);
				break;
			}
		}

	}

	private class TVStatusServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			isBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBound = false;
		}

	}

}
