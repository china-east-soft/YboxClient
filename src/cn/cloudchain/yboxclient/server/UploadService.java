package cn.cloudchain.yboxclient.server;

import cn.cloudchain.yboxclient.helper.CloudHelper;
import cn.cloudchain.yboxclient.utils.LogUtil;

import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileInfoResult;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileProgressListener;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileUploadListener;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class UploadService extends Service {
	private final String TAG = UploadService.class.getSimpleName();
	private MyProgressListener progressListener = new MyProgressListener();
	private MyUploadListener uploadListener = new MyUploadListener();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String localPath = intent.getStringExtra("localPath");
		String remotePath = intent.getStringExtra("remotePath");
		LogUtil.i(TAG,
				String.format("local = %s remote = %s", localPath, remotePath));

		CloudHelper.getInstance().uploadFile(localPath, remotePath,
				progressListener, uploadListener);
		return super.onStartCommand(intent, flags, startId);
	}

	private class MyProgressListener implements FileProgressListener {

		@Override
		public void onProgress(String arg0, long arg1, long arg2) {
			Intent intent = new Intent();
			intent.setAction("cn.cloudchain.ybox.UploadService");
			intent.putExtra("source", arg0);
			intent.putExtra("bytes", arg1);
			intent.putExtra("total", arg2);
			LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
		}

	}

	private class MyUploadListener implements FileUploadListener {

		@Override
		public void onFailure(String arg0, int arg1, String arg2) {
			LogUtil.e(TAG, String.format(
					"error: source = %s code = %d msg = %s", arg0, arg1, arg2));
		}

		@Override
		public void onSuccess(String arg0, FileInfoResult arg1) {

		}

	}
}
