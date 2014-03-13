package cn.cloudchain.yboxclient.helper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.dialog.CustomDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.task.AppUpdateCheckTask;
import cn.cloudchain.yboxclient.task.DownloadFileTask;
import cn.cloudchain.yboxclient.task.YboxUpdateCheckTask;
import cn.cloudchain.yboxclient.task.YboxUpdateJumpTask;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;

/**
 * 处理客户端，终端升级，及终端重启
 * 
 * @author lazzy
 * 
 */
public class UpdateUtil {
	final String TAG = UpdateUtil.class.getSimpleName();
	private Context mContext;
	private FragmentManager fm;

	public UpdateUtil(Context mContext) {
		this.mContext = mContext;
		if (mContext instanceof FragmentActivity) {
			fm = ((FragmentActivity) mContext).getSupportFragmentManager();
		}
	}

	private void sendActivityFinishCommand() {
		// if (mContext != null) {
		// Intent intent = new Intent();
		// intent.setAction(ApStatusInfoChangeReceiver.ACTION_FINISH_ACTIVITY);
		// mContext.sendBroadcast(intent);
		// }
	}

	/**
	 * 处理客户端升级，弹出对话框提示升级。
	 * 
	 * @param bundle
	 */
	public void handleAppUpdate(final Bundle bundle) {
		LogUtil.i(TAG, "need update android");
		String androidFilePath = null;
		String androidVersion = null;
		String androidMessage = null;
		boolean isForced = false;
		if (bundle != null && bundle.size() > 0) {
			androidFilePath = bundle
					.getString(AppUpdateCheckTask.APP_UPDATE_FILEPATH);
			androidVersion = bundle
					.getString(AppUpdateCheckTask.APP_UPDATE_VERSION);
			androidMessage = bundle
					.getString(AppUpdateCheckTask.APP_UPDATE_MESSAGE);
			isForced = bundle.getBoolean(AppUpdateCheckTask.APP_UPDATE_ENFORCE);
		}

		if (TextUtils.isEmpty(androidFilePath)
				|| TextUtils.isEmpty(androidVersion)) {
			return;
		}

		StringBuilder msgSb = new StringBuilder();
		msgSb.append(Util.getString(R.string.app_name, ""));
		if (!TextUtils.isEmpty(androidVersion)) {
			msgSb.append(androidVersion);
		}
		msgSb.append(":\n");
		if (!TextUtils.isEmpty(androidMessage)) {
			msgSb.append(androidMessage);
		}

		final String tempAndroidPath = androidFilePath;
		final StringBuilder tempMsgSb = msgSb;

		CharSequence negtiveTitle;
		CharSequence dialogTitle;
		if (isForced) {
			dialogTitle = Util.getString(R.string.update_title_hard, "");
			negtiveTitle = Util.getString(R.string.exit, "");
		} else {
			dialogTitle = Util.getString(R.string.update_title, "");
			negtiveTitle = Util.getString(R.string.update_later, "");
		}

		CustomDialogFragment fragment = CustomDialogFragment.newInstance(
				dialogTitle, Html.fromHtml(msgSb.toString()),
				Util.getString(R.string.update_now, ""), negtiveTitle,
				!isForced);
		fragment.setDialogService(new IDialogService() {

			@Override
			public void onClick(DialogFragment fragment, int actionId) {
				switch (actionId) {
				case R.id.dialog_click_positive:
					DownloadFileTask mTask = new DownloadFileTask(mContext,
							tempAndroidPath) {

						@Override
						public void handleDownloadSuccess(String filePath) {
							Util.installApk(mContext, filePath);
						}
					};
					TaskDialogFragment taskFragment = TaskDialogFragment
							.newProgressBarFragment(
									Util.getString(R.string.update, ""),
									Html.fromHtml(tempMsgSb.toString()), true);
					taskFragment.setTask(mTask);
					taskFragment.show(fragment.getFragmentManager(),
							TaskDialogFragment.TAG);
					break;
				case R.id.dialog_click_negitive:
					if (!fragment.isCancelable()) {
						sendActivityFinishCommand();
						break;
					}
				}
			}

			@Override
			public View getDialogView() {
				return null;
			}
		});

		if (fm != null) {
			fragment.show(fm, CustomDialogFragment.TAG);
		}
	}

	/**
	 * 处理终端升级
	 * 
	 * @param bundle
	 */
	public void handleYboxUpdate(final Bundle bundle) {
		if (bundle == null || bundle.isEmpty()) {
			return;
		}

		final String middleUrl = bundle
				.getString(YboxUpdateCheckTask.MIDDLE_UPDATE_FILEPATH);
		final String imageUrl = bundle
				.getString(YboxUpdateCheckTask.IMAGE_UPDATE_FILEPATH);
		if (TextUtils.isEmpty(middleUrl) && TextUtils.isEmpty(imageUrl)) {
			return;
		}

		CharSequence negtiveTitle;
		CharSequence dialogTitle;
		final boolean isForced = bundle
				.getBoolean(YboxUpdateCheckTask.IMAGE_UPDATE_ENFORCE)
				|| bundle.getBoolean(YboxUpdateCheckTask.MIDDLE_UPDATE_ENFORCE);
		if (isForced) {
			dialogTitle = Util.getString(R.string.update_title_hard, "");
			negtiveTitle = Util.getString(R.string.exit, "");
		} else {
			dialogTitle = Util.getString(R.string.update_title, "");
			negtiveTitle = Util.getString(R.string.update_later, "");
		}

		StringBuilder msgSb = new StringBuilder();
		String imageLog = bundle
				.getString(YboxUpdateCheckTask.IMAGE_UPDATE_MESSAGE);
		String middleLog = bundle
				.getString(YboxUpdateCheckTask.MIDDLE_UPDATE_MESSAGE);
		msgSb.append("存在更新\n").append(imageLog).append('\n').append(middleLog);

		CustomDialogFragment fragment = CustomDialogFragment.newInstance(
				dialogTitle, Html.fromHtml(msgSb.toString()),
				Util.getString(R.string.update_now, ""), negtiveTitle,
				!isForced);
		fragment.setDialogService(new IDialogService() {

			@Override
			public void onClick(DialogFragment fragment, int actionId) {
				switch (actionId) {
				case R.id.dialog_click_positive:
					YboxUpdateJumpTask task = new YboxUpdateJumpTask(fragment
							.getFragmentManager(), middleUrl, imageUrl);
					TaskDialogFragment taskFragment = TaskDialogFragment
							.newLoadingFragment("正在请求下载升级包...",
									fragment.isCancelable());
					taskFragment.setTask(task);
					taskFragment.show(fragment.getFragmentManager(), null);
					break;
				case R.id.dialog_click_negitive:
					if (!fragment.isCancelable()) {
						sendActivityFinishCommand();
						break;
					}
				}
			}

			@Override
			public View getDialogView() {
				return null;
			}
		});
		if (fm != null) {
			fragment.show(fm, CustomDialogFragment.TAG);
		}
	}
}
