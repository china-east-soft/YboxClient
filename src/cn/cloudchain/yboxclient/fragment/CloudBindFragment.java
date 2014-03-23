package cn.cloudchain.yboxclient.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.helper.CloudHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.Util;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaAccount;
import com.baidu.frontia.FrontiaUser;
import com.baidu.frontia.api.FrontiaAuthorizationListener.AuthorizationListener;

public class CloudBindFragment extends Fragment {
	final String TAG = CloudBindFragment.class.getSimpleName();

	private Button test;
	private boolean isLogin = false;

	private MyHandler handler = new MyHandler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrontiaAccount account = Frontia.getCurrentAccount();
		isLogin = account != null
				&& (account.getType() == FrontiaAccount.Type.USER);
	}

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
						handler.sendEmptyMessage(MyHandler.LOGIN_SUCCESS);
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						LogUtil.e(TAG,
								String.format("code = %d msg = %s", arg0, arg1));
						handler.sendEmptyMessage(MyHandler.LOGIN_FAIL);
					}

					@Override
					public void onCancel() {
					}
				});
	}

	private void handleLogout() {
		boolean result = CloudHelper.getInstance().logout();
		handler.sendEmptyMessage(result ? MyHandler.LOGOUT_SUCCESS
				: MyHandler.LOGOUT_FAIL);
	}

	private static class MyHandler extends WeakHandler<CloudBindFragment> {
		private static final int LOGIN = 0;
		private static final int LOGIN_SUCCESS = 1;
		private static final int LOGIN_FAIL = 2;
		private static final int LOGOUT = 3;
		private static final int LOGOUT_SUCCESS = 4;
		private static final int LOGOUT_FAIL = 5;

		public MyHandler(CloudBindFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case LOGIN:
				getOwner().handleLogin();
				break;
			case LOGIN_SUCCESS:
				getOwner().isLogin = true;
				getOwner().refreshView();
				break;
			case LOGIN_FAIL:
				Util.toaster("授权失败", null);
				break;
			case LOGOUT:
				getOwner().handleLogout();
				break;
			case LOGOUT_SUCCESS:
				getOwner().isLogin = false;
				getOwner().refreshView();
				break;
			case LOGOUT_FAIL:
				Util.toaster("登出失败", null);
				break;
			}
		}

	}

}
