package cn.cloudchain.yboxclient.task;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.bean.FileBean;
import cn.cloudchain.yboxclient.helper.FilesOperationHandler;
import cn.cloudchain.yboxclient.helper.SetHelper;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxcommon.bean.ErrorBean;
import cn.cloudchain.yboxcommon.bean.Types;

public class FilesOperationTask extends BaseFragmentTask {
	final String TAG = FilesOperationTask.class.getSimpleName();
	private final static int RESULT_SUCCESS = 0;
	private final static int RESULT_FAIL = 1;

	private enum OperType {
		LIST, DELETE
	}

	private String[] paths;
	private int fileType = Types.FILE_ALL;
	private OperType type;
	private boolean cancel = false;
	private FilesOperationHandler<?> handler;
	private ArrayList<FileBean> files = null;

	/**
	 * 删除文件列表
	 * 
	 * @param cancel
	 *            当task cancel后是否需要取消下一步操作
	 * @param params
	 *            文件地址
	 */
	public FilesOperationTask(FilesOperationHandler<?> handler, boolean cancel,
			String... params) {
		this.handler = handler;
		this.type = OperType.DELETE;
		this.cancel = cancel;
		this.paths = params;
	}

	/**
	 * 获取文件列表
	 * 
	 * @param handler
	 * @param fileType
	 *            获取文件列表类型 {@link Types#FILE_ALL} {@link Types#FILE_ONLY_AUDIO}
	 *            {@link Types#FILE_ONLY_VIDEO}
	 * @param path
	 *            需打开的目录路径
	 * @param cancel
	 *            当task cancel后是否需要取消下一步操作
	 */
	public FilesOperationTask(FilesOperationHandler<?> handler, int fileType,
			boolean cancel, String... paths) {
		this.handler = handler;
		this.type = OperType.LIST;
		this.fileType = fileType;
		this.cancel = cancel;
		this.paths = paths;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int result = RESULT_FAIL;
		switch (type) {
		case LIST:
			String path = (paths == null || paths.length == 0) ? "" : paths[0];
			result = dealFileList(fileType, path);
			break;
		case DELETE:
			result = dealFileDelete(paths);
			break;
		}

		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (cancel && isCancelled()) {
			return;
		}

		if (handler == null || handler.getOwner() == null)
			return;

		handler.sendEmptyMessage(FilesOperationHandler.REQUEST_COMPLETE);

		LogUtil.i("", "result = " + result);
		switch (result) {
		case RESULT_SUCCESS:
			if (type == OperType.LIST) {
				Message msg = handler
						.obtainMessage(FilesOperationHandler.FILE_LOAD_SUCESS);
				Bundle data = new Bundle();
				if (files != null) {
					data.putParcelableArrayList(
							FilesOperationHandler.BUNDLE_FILES, files);
				}
				msg.setData(data);
				handler.sendMessage(msg);
			} else if (type == OperType.DELETE) {
				handler.sendEmptyMessage(FilesOperationHandler.FILE_DELETE_SUCCESS);
			}
			break;
		case ErrorBean.REQUEST_TIMEOUT:
			Util.toaster(R.string.request_fail_net);
			break;
		case ErrorBean.SD_NOT_READY:
			Util.toaster(R.string.sdcard_unavailable);
			break;
		case ErrorBean.FILE_NOT_DIRECTORY:
			Util.toaster(R.string.file_not_directory);
			break;
		case ErrorBean.FILE_NOT_EXIST:
			Util.toaster(R.string.file_not_exist);
			break;
		default:
			Util.toaster(R.string.request_fail);
			break;
		}
	}

	/**
	 * 处理获取文件列表
	 * 
	 * @param path
	 * @return
	 */
	private int dealFileList(int fileType, String path) {
		int result = RESULT_FAIL;
		String response = SetHelper.getInstance().getFilesInDirectory(fileType,
				path);
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean(Constants.RESULT)) {
				result = RESULT_SUCCESS;
				JSONArray fileArray = obj.optJSONArray(Constants.File.FILES);
				int size = fileArray.length();
				files = new ArrayList<FileBean>(size);
				for (int i = 0; i < size; ++i) {
					JSONObject fileObj = fileArray.getJSONObject(i);
					FileBean file = new FileBean();
					file.name = fileObj.optString(Constants.File.NAME);
					file.absolutePath = fileObj
							.optString(Constants.File.PATH_ABSOLUTE);
					file.isDirectory = fileObj
							.optBoolean(Constants.File.IS_DIRECTORY);
					file.childrenNum = fileObj
							.optInt(Constants.File.CHILDREN_NUM);
					file.lastModified = fileObj
							.optLong(Constants.File.LAST_MODIFY_TIME);
					file.size = fileObj.optLong(Constants.File.SIZE);
					files.add(file);
				}
			} else {
				result = obj.optInt(Constants.ERROR_CODE, RESULT_FAIL);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 处理删除文件
	 * 
	 * @param paths
	 * @return
	 */
	private int dealFileDelete(String[] paths) {
		int result = RESULT_FAIL;
		String response = SetHelper.getInstance().deleteFiles(paths);
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.optBoolean(Constants.RESULT)) {
				result = RESULT_SUCCESS;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

}