package cn.cloudchain.yboxclient.helper;

import java.util.ArrayList;

import android.app.Activity;
import cn.cloudchain.yboxclient.MyApplication;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaUser;
import com.baidu.frontia.api.FrontiaAuthorization;
import com.baidu.frontia.api.FrontiaAuthorization.MediaType;
import com.baidu.frontia.api.FrontiaAuthorizationListener.AuthorizationListener;
import com.baidu.frontia.api.FrontiaAuthorizationListener.UserInfoListener;
import com.baidu.frontia.api.FrontiaPersonalStorage;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileInfoListener;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileListListener;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileOperationListener;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileProgressListener;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileTransferListener;
import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileUploadListener;

public class CloudHelper {
	final String TAG = CloudHelper.class.getSimpleName();
	private static CloudHelper instance;
	// ID: 2381235
	// API KEY: WLmPcEo8f06KE8XwYIcEkDwN
	// secret key: QRl8ObF6nmlg0BiGxBEE7rPgDHKfKx60
	private final static String APIKEY = "WLmPcEo8f06KE8XwYIcEkDwN";

	private FrontiaAuthorization mAuthorization;
	private FrontiaPersonalStorage mCloudStorage;

	private final static String Scope_Basic = "basic";
	private final static String Scope_Netdisk = "netdisk";

	public static CloudHelper getInstance() {
		if (instance == null)
			instance = new CloudHelper();
		return instance;
	}

	private CloudHelper() {
		Frontia.init(MyApplication.getAppContext(), APIKEY);
		mAuthorization = Frontia.getAuthorization();
		mCloudStorage = Frontia.getPersonalStorage();
	}

	public enum ListType {
		Audio, Video
	}

	/**
	 * 百度云授权
	 * 
	 * @param activity
	 * @param listener
	 */
	public void login(final Activity activity,
			final AuthorizationListener listener) {
		ArrayList<String> scope = new ArrayList<String>();
		scope.add(Scope_Basic);
		scope.add(Scope_Netdisk);
		mAuthorization.bindBaiduOAuth(activity, scope,
				new AuthorizationListener() {

					@Override
					public void onSuccess(FrontiaUser result) {
						Frontia.setCurrentAccount(result);
						if (listener != null) {
							listener.onSuccess(result);
						}
					}

					@Override
					public void onFailure(int errCode, String errMsg) {
						if (listener != null) {
							listener.onFailure(errCode, errMsg);
						}
					}

					@Override
					public void onCancel() {
						if (listener != null) {
							listener.onCancel();
						}
					}

				});
	}

	/**
	 * 登出
	 * 
	 * @return
	 */
	public boolean logout() {
		boolean result = mAuthorization
				.clearAuthorizationInfo(FrontiaAuthorization.MediaType.BAIDU
						.toString());
		if (result) {
			Frontia.setCurrentAccount(null);
		}
		return result;
	}

	/**
	 * 获取用户信息
	 */
	public void userinfo() {
		mAuthorization.getUserInfo(MediaType.BAIDU.toString(),
				new UserInfoListener() {

					@Override
					public void onSuccess(FrontiaUser.FrontiaUserDetail result) {

					}

					@Override
					public void onFailure(int errCode, String errMsg) {

					}

				});
	}

	/**
	 * 获取文件列表，采用默认排序，运行在主线程中
	 * 
	 * @param path
	 * @param listener
	 */
	public void list(final String path, final FileListListener listener) {
		if (listener == null)
			return;
		mCloudStorage.list(path, null, null, listener);
	}

	/**
	 * 获取文件列表，运行在主线程中
	 * 
	 * @param path
	 *            云文件目录地址
	 * @param by
	 *            指定返回列表的排序方式，例如 {@link FrontiaPersonalStorage#BY_NAME}
	 * @param order
	 *            指定返回列表的排序方式，例如 {@link FrontiaPersonalStorage#ORDER_ASC}
	 * @param listener
	 *            不能为空
	 * @return
	 */
	public void list(final String path, final String by, final String order,
			final FileListListener listener) {
		if (listener == null)
			return;
		mCloudStorage.list(path, by, order, listener);
	}

	/**
	 * 获取某个类型的文件列表，运行在主线程中
	 * 
	 * @param type
	 * @param listener
	 */
	public void list(final ListType type, final FileListListener listener) {
		if (listener == null)
			return;
		switch (type) {
		case Audio:
			mCloudStorage.audioStream(listener);
			break;
		case Video:
			mCloudStorage.videoStream(listener);
			break;
		}
	}

	/**
	 * 创建目录
	 * 
	 * @param path
	 *            云文件地址
	 * @param listener
	 */
	public void createDir(final String path, final FileInfoListener listener) {
		mCloudStorage.makeDir(path, listener);
	}

	/**
	 * 删除某个文件
	 * 
	 * @param path
	 *            云文件目录
	 * @param listener
	 */
	public void deleteFile(final String path,
			final FileOperationListener listener) {
		mCloudStorage.deleteFile(path, listener);
	}

	/**
	 * 上传本地文件到云
	 * 
	 * @param localPath
	 *            本地文件路径
	 * @param cloudPath
	 *            云文件目的地址
	 * @param progressListener
	 *            上传进度
	 * @param uploadListener
	 *            上传完成状态
	 */
	public void uploadFile(final String localPath, final String cloudPath,
			final FileProgressListener progressListener,
			final FileUploadListener uploadListener) {
		mCloudStorage.uploadFile(localPath, cloudPath, progressListener,
				uploadListener);
	}

	/**
	 * 停止正在上传或者下载的文件
	 * 
	 * @param source
	 *            源文件路径。如停止上传，该参数为本地文件路径，反之为云端文件路径
	 * @param target
	 *            目标文件路径。如停止上传，该参数为云端文件路径，反之为本地文件路径
	 */
	public void stopTransferringFile(final String source, final String target) {
		mCloudStorage.stopTransferring(source, target);
	}

	/**
	 * 从云上下载文件到本地
	 * 
	 * @param cloudPath
	 *            云文件路径
	 * @param localPath
	 *            本地文件目的地址
	 * @param progressListener
	 * @param tranferListener
	 */
	public void downloadFile(final String cloudPath, final String localPath,
			final FileProgressListener progressListener,
			final FileTransferListener tranferListener) {
		mCloudStorage.downloadFileFromStream(cloudPath, localPath,
				progressListener, tranferListener);
	}
}
