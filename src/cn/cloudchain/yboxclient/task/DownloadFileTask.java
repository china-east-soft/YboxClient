package cn.cloudchain.yboxclient.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.text.TextUtils;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.http.MyHttpClient;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;

/**
 * 负责文件下载，下载时刷新进度，继承 {@link BaseFragmentTask}
 * 
 * @author lazzy
 * 
 */
public abstract class DownloadFileTask extends BaseFragmentTask {
	private final String TAG = "DownloadFileTask";// 下载异常
	private final int DOWNLOAD_PATH_WRONG = 100;
	// 下载异常
	private final int ERROR_DOWNLOAD = 200;

	private final int DOWNLOAD_SUCCESS = 300;
	// 文件异常
	private final int DOWNLOAD_CANCELED = 600;
	private final int ERROR_FILE_EXCEPTION = 400;
	private final int SDCARD_DISMISS = 500;

	private String absoluteFilePath = null;
	private String url;

	public DownloadFileTask(Context mContext, String url) {
		this.url = url;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (url == null || url.length() == 0) {
			return DOWNLOAD_PATH_WRONG;
		}
		if (isCancelled()) {
			return DOWNLOAD_CANCELED;
		}

		int result = ERROR_DOWNLOAD;
		try {
			HttpResponse response = new MyHttpClient().setConnectionTimeout(
					MyHttpClient.TIMEOUT_NET).getHttpResponse(MyHttpClient.GET,
					url, null);

			if (response == null) {
				throw new Exception("fail to download apk file");
			}
			HttpEntity entity = response.getEntity();
			if (entity == null || entity.getContentLength() <= 0) {
				result = ERROR_FILE_EXCEPTION;
				throw new Exception("error file exception");
			}

			if (isCancelled()) {
				return DOWNLOAD_CANCELED;
			}
			long length = entity.getContentLength();
			LogUtil.i(TAG, "TotalLenth:" + length);

			InputStream is = entity.getContent();
			int bufSize = (int) (length / 10);
			if (bufSize > 40) {
				bufSize = 40;
			}
			String fileName = url.substring(url.lastIndexOf('/'), url.length());
			String downPath = Util.getDownloadPath();
			if (TextUtils.isEmpty(downPath)) {
				return SDCARD_DISMISS;
			}

			File downDir = new File(downPath);
			if (!downDir.exists()) {
				downDir.mkdir();
			}

			if (!downDir.exists()) {
				throw new Exception(
						"download path created fail! it does not exist");
			}

			File outputFile = new File(downDir, fileName);
			absoluteFilePath = outputFile.getAbsolutePath();
			LogUtil.i(TAG, "absolute path = " + absoluteFilePath);
			if (outputFile.exists()) {
				LogUtil.i(TAG, "outputFile is exists");
				outputFile.delete();
			}

			FileOutputStream fos = new FileOutputStream(outputFile);
			byte[] buffer = new byte[bufSize * 1024];
			int len = -1;
			int readTotalLen = 0;
			while ((len = is.read(buffer)) != -1 && !isCancelled()) {
				fos.write(buffer, 0, len);
				readTotalLen += len;
				publishProgress((int) (readTotalLen * 100 / length));

			}
			fos.close();
			is.close();
			if (readTotalLen < length && isCancelled()) {
				result = DOWNLOAD_CANCELED;
			} else {
				result = DOWNLOAD_SUCCESS;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (isCancelled()) {
			return;
		}
		switch (result) {
		case DOWNLOAD_PATH_WRONG:
			Util.toaster(R.string.invalid_download_path);
			break;
		case DOWNLOAD_SUCCESS:
			handleDownloadSuccess(absoluteFilePath);
			break;
		case DOWNLOAD_CANCELED:
			break;
		case ERROR_DOWNLOAD:
			Util.toaster(R.string.download_fail);
			break;
		case ERROR_FILE_EXCEPTION:
			Util.toaster(R.string.file_error);
			break;
		case SDCARD_DISMISS:
			Util.toaster(R.string.sdcard_fail);
			break;
		}
	}

	/**
	 * 进一步处理下载结果，只有下载成功后才会调用此方法
	 * 
	 * @param filePath
	 *            文件下载地址
	 */
	public abstract void handleDownloadSuccess(String filePath);

}
