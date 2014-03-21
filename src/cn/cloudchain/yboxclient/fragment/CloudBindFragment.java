package cn.cloudchain.yboxclient.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.CloudHelper;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.PreferenceUtil;
import cn.cloudchain.yboxclient.utils.Util;

import com.baidu.frontia.FrontiaUser;
import com.baidu.frontia.api.FrontiaAuthorizationListener.AuthorizationListener;

public class CloudBindFragment extends Fragment {
	final String TAG = CloudBindFragment.class.getSimpleName();

	private Button test;
	private boolean isLogin = !TextUtils.isEmpty(PreferenceUtil.getString(
			PreferenceUtil.BAIDU_AUTU_TOKEN, null));

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_cloud_bind, container,
				false);
		test = (Button) view.findViewById(R.id.test);
		test.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.container, new CloudListFragment());
				ft.addToBackStack(getTag());
				ft.commit();
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshView();
	}

	private void refreshView() {
		test.setText(isLogin ? "Logout" : "Login");
	}

	private void handleLogin() {
		CloudHelper.getInstance().login(getActivity(),
				new AuthorizationListener() {

					@Override
					public void onSuccess(FrontiaUser arg0) {
						isLogin = true;
						refreshView();
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						LogUtil.e(TAG,
								String.format("code = %d msg = %s", arg0, arg1));
						Util.toaster("授权失败", null);
					}

					@Override
					public void onCancel() {
						Util.toaster("取消授权", null);
					}
				});
	}

	private void handleLogout() {
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				"正在登出...", false);
		fragment.setTask(new LogoutTask());
		fragment.show(getFragmentManager(), null);
	}

	private class LogoutTask extends BaseFragmentTask {
		private final static int RESULT_SUCCESS = 0;
		private final static int RESULT_FAIL = 1;

		@Override
		protected Integer doInBackground(Void... params) {
			return CloudHelper.getInstance().logout() ? RESULT_SUCCESS
					: RESULT_FAIL;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			switch (result) {
			case RESULT_SUCCESS:
				Util.toaster("登出成功", null);
				isLogin = false;
				refreshView();
				break;
			case RESULT_FAIL:
				Util.toaster("登出失败", null);
				break;
			}
		}
	}

}
