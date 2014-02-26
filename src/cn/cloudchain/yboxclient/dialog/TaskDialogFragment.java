package cn.cloudchain.yboxclient.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;

/**
 * 和 {@link BaseFragmentTask} 配合使用，用于执行线程时的对话框显示，对话框显示时线程就开始运行
 * 
 * @author lazzy
 * 
 */
public class TaskDialogFragment extends DialogFragment {
	public static final String TAG = TaskDialogFragment.class.getSimpleName();
	private static final int TYPE_LOADING = 0;
	private static final int TYPE_PROGRESSBAR = 1;

	private static final String STATE_TYPE = "state_type";
	private static final String STATE_TITLE = "state_title";
	private static final String STATE_MESSAGE = "state_message";
	private static final String STATE_CANCELABLE = "state_cancelable";

	private CharSequence dialogMessage;
	private CharSequence dialogTitle;
	private boolean isCancelable = true;

	private int dialogType = TYPE_LOADING;

	private BaseFragmentTask mTask;
	private ProgressBar mProgressBar = null;
	private TextView mProgressPercent = null;
	private TextView mMessage;

	/**
	 * 为对话框绑定一个相应的线程任务
	 * 
	 * @param task
	 *            必须是 {@link BaseFragmentTask} 或者其超类
	 */
	public void setTask(BaseFragmentTask task) {
		mTask = task;
		if (mTask != null) {
			mTask.setDialogFragment(this);
		}
	}

	/**
	 * 创建一个加载对话框，该对话框只有一个ProgressBar和提示信息
	 * 
	 * @param progressMessage
	 *            提示信息，如果为null，则为默认值"加载中..."
	 * @param isCancelable
	 *            是否可取消
	 * @return
	 */
	public static TaskDialogFragment newLoadingFragment(
			CharSequence progressMessage, boolean isCancelable) {
		TaskDialogFragment fragment = new TaskDialogFragment();
		Bundle args = new Bundle();
		args.putInt(STATE_TYPE, TYPE_LOADING);
		args.putCharSequence(STATE_MESSAGE, progressMessage);
		args.putBoolean(STATE_CANCELABLE, isCancelable);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * 创建一个进度条对话框，该对话框有标题，提示信息，ProgressBar和进度信息
	 * 
	 * @param dialogTitle
	 *            标题
	 * @param dialogMessage
	 *            提示信息
	 * @param isCancelable
	 *            是否可取消
	 * @return
	 */
	public static TaskDialogFragment newProgressBarFragment(
			CharSequence dialogTitle, CharSequence dialogMessage,
			boolean isCancelable) {
		TaskDialogFragment fragment = new TaskDialogFragment();
		Bundle args = new Bundle();
		args.putInt(STATE_TYPE, TYPE_PROGRESSBAR);
		args.putCharSequence(STATE_TITLE, dialogTitle);
		args.putCharSequence(STATE_MESSAGE, dialogMessage);
		args.putBoolean(STATE_CANCELABLE, isCancelable);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		Bundle bundle = getArguments();
		if (bundle != null) {
			this.dialogType = bundle.getInt(STATE_TYPE, TYPE_LOADING);
			this.isCancelable = bundle.getBoolean(STATE_CANCELABLE, true);
			switch (this.dialogType) {
			case TYPE_LOADING:
				this.dialogMessage = bundle.getCharSequence(STATE_MESSAGE);
				if (TextUtils.isEmpty(dialogMessage)) {
					dialogMessage = "加载中...";
				}
				break;
			case TYPE_PROGRESSBAR:
				this.dialogMessage = bundle.getCharSequence(STATE_MESSAGE);
				this.dialogTitle = bundle.getCharSequence(STATE_TITLE);
				break;
			}

		}

		setCancelable(isCancelable);
		if (mTask != null) {
			mTask.execute();
		}
		setStyle(STYLE_NO_TITLE, R.style.CommonDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = null;
		if (dialogType == TYPE_LOADING) {
			v = inflater.inflate(R.layout.dialog_custom_progress, container,
					false);
			mMessage = (TextView) v.findViewById(R.id.progress_message);

			if (!TextUtils.isEmpty(dialogMessage)) {
				mMessage.setText(dialogMessage);
			}
		} else if (dialogType == TYPE_PROGRESSBAR) {
			v = inflater.inflate(R.layout.dialog_progressbar, container, false);
			TextView mTitle = (TextView) v.findViewById(R.id.dialog_title);
			TextView mMessage = (TextView) v.findViewById(R.id.dialog_message);
			Button mButton = (Button) v.findViewById(R.id.dialog_cancel);
			mProgressBar = (ProgressBar) v
					.findViewById(R.id.dialog_progressbar);
			mProgressPercent = (TextView) v
					.findViewById(R.id.dialog_progress_percent);
			mButton.setText(R.string.cancel);

			if (!TextUtils.isEmpty(dialogTitle)) {
				mTitle.setText(dialogTitle);
				mTitle.setVisibility(View.VISIBLE);
			}

			if (!TextUtils.isEmpty(dialogMessage)) {
				mMessage.setText(dialogMessage);
				mMessage.setVisibility(View.VISIBLE);
			}

			mButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					TaskDialogFragment.this.dismiss();
				}
			});

		}
		getDialog().setCanceledOnTouchOutside(false);
		return v;
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mTask != null) {
			mTask.cancel(false);
		}
		super.onDismiss(dialog);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mTask == null)
			dismiss();
	}

	/**
	 * 更新进度条提示信息/更新加载提示信息
	 * 
	 * @param percentOrResId
	 *            已进行的百分比，或者需更新的加载提示信息的resId
	 */
	public void updateProgress(int percentOrResId) {
		if (mProgressBar != null) {
			mProgressBar.setProgress(percentOrResId);
		}
		if (mProgressPercent != null) {
			StringBuilder sb = new StringBuilder("已下载 ");
			sb.append(percentOrResId);
			sb.append('%');
			mProgressPercent.setText(sb);
		}
		if (mMessage != null) {
			mMessage.setText(percentOrResId);
		}
	}

	/**
	 * 线程任务完成，dismiss对话框
	 */
	public void taskFinished() {
		if (isResumed())
			dismiss();
		mTask = null;
	}
}
