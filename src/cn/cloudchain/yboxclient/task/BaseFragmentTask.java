package cn.cloudchain.yboxclient.task;

import android.os.AsyncTask;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;

/**
 * 主要和 {@link TaskDialogFragment}
 * 配合使用，解决在线程中dismiss对话框导致异常的情况。一般不直接调用execute开启线程，而在 {@link TaskDialogFragment}
 * 中开启，
 * 
 * @author lazzy
 * 
 */
public class BaseFragmentTask extends AsyncTask<Void, Integer, Integer> {
	private TaskDialogFragment mFragment;

	/**
	 * 设置与该线程绑定的对话框
	 * 
	 * @param fragment
	 *            对话框
	 */
	public void setDialogFragment(TaskDialogFragment fragment) {
		mFragment = fragment;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mFragment == null) {
			return;
		}
		mFragment.updateProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (mFragment == null) {
			return;
		}
		mFragment.taskFinished();
	}

}
