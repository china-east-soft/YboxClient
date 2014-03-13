package cn.cloudchain.yboxclient.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import cn.cloudchain.yboxclient.bean.StatusBean;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.helper.ApHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.PreferenceUtil;

public class TvStatusService extends Service {
	private static final String TAG = TvStatusService.class.getSimpleName();
	private ScheduledExecutorService scheduleTaskExecutor = null;
	private StatusResultHandler mHandler = new StatusResultHandler(this);

	private final int MAX_EXCEPTION_TIME = 2;
	private int currentExceptionTime = 0;

	private void runStatus(boolean needShutDown) {
		if (scheduleTaskExecutor != null && !scheduleTaskExecutor.isShutdown()
				&& needShutDown) {
			LogUtil.d(TAG, "thread pool shut down");
			scheduleTaskExecutor.shutdown();
			scheduleTaskExecutor = null;
			return;
		}
		boolean flag = false;
		if (scheduleTaskExecutor == null) {
			scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
			flag = true;
		} else {
			flag = scheduleTaskExecutor.isShutdown();
		}

		if (flag) {
			scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					StatusBean oldBean = ApHelper.getInstance()
							.getCurrentStatus();
					StatusBean bean = null;
					try {
						bean = ApHelper.getInstance().getStatus();
					} catch (YunmaoException e) {
						e.printStackTrace();
						++currentExceptionTime;
					}

					if (bean != null
							|| currentExceptionTime > MAX_EXCEPTION_TIME) {
						currentExceptionTime = 0;
						ApHelper.getInstance().updateStatus(bean);
						boolean switchStateEqual = (bean != null && bean
								.equals(oldBean))
								|| (bean == null && oldBean == null);
						if (!switchStateEqual) {
							mHandler.sendEmptyMessage(StatusResultHandler.MSG_SWITCH_STATE_CHANGE);
						}

						if (bean != null) {
							String remoteEpgUpdateTime = bean
									.getEpgUpdateTime();
							String localEpgUpdateTime = PreferenceUtil
									.getString(
											PreferenceUtil.LOCAL_EPG_CREATE_TIME,
											"");
							if (!remoteEpgUpdateTime.equals(localEpgUpdateTime)) {
								mHandler.sendEmptyMessage(StatusResultHandler.MSG_REMOTE_EPG_CHANGE);
							}
						}
					}
				}
			}, 0, 3, TimeUnit.SECONDS);
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.d(TAG, "service onBind");
		runStatus(false);
		return new Binder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		runStatus(true);
		return super.onUnbind(intent);
	}

	private static class StatusResultHandler extends
			WeakHandler<TvStatusService> {
		// private static final int MSG_REQUEST_FAIL = 1;
		// private static final int MSG_COMPLETE = 0;
		private static final int MSG_SWITCH_STATE_CHANGE = 2;
		private static final int MSG_REMOTE_EPG_CHANGE = 3;

		public StatusResultHandler(TvStatusService owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			TvStatusService owner = getOwner();
			if (owner == null)
				return;

			Intent intent = new Intent();
			switch (msg.what) {
			// case MSG_REQUEST_FAIL:
			// intent.setAction(TvStatusReceiver.ACTION_STATUS_WRONG);
			// break;
			// case MSG_COMPLETE:
			// intent.setAction(TvStatusReceiver.ACTION_STATUS_COMPLETE);
			// intent.putExtras(msg.getData());
			// break;
			case MSG_SWITCH_STATE_CHANGE:
				intent.setAction(TvStatusReceiver.ACTION_SWITCH_STATE_CHANGE);
				break;
			case MSG_REMOTE_EPG_CHANGE:
				intent.setAction(TvStatusReceiver.ACTION_REMOTE_EPG_CHANGE);
				break;
			}
			LocalBroadcastManager.getInstance(owner).sendBroadcast(intent);
		}

	}
}
