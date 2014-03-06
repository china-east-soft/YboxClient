package cn.cloudchain.yboxclient.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.ServerHelper;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;
import cn.cloudchain.yboxclient.utils.PreferenceUtil;

public class LoginFragment extends Fragment {
	private EditText accountEditText;
	private EditText passEditText;
	private CheckBox passRememberCheckbox;
	private CheckBox loginAutoCheckbox;
	private View loginView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_login, container, false);
		accountEditText = (EditText) view.findViewById(R.id.login_account_edit);
		passEditText = (EditText) view.findViewById(R.id.login_pass_edit);
		passRememberCheckbox = (CheckBox) view.findViewById(R.id.pass_remember);
		loginAutoCheckbox = (CheckBox) view.findViewById(R.id.login_auto);

		loginView = view.findViewById(R.id.login);
		loginView.setOnClickListener(new LoginClickListener());
		view.findViewById(R.id.register).setOnClickListener(
				new RegisterClickListener());

		accountEditText.addTextChangedListener(new MyTextChangeWatcher());
		passEditText.addTextChangedListener(new MyTextChangeWatcher());
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (PreferenceUtil.getInt(PreferenceUtil.PREF_ACCOUNT_ID, -1) > 0) {
			getActivity().onBackPressed();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(
				R.string.login_title);
		refreshLoginState();
	}

	private void refreshLoginState() {
		if (TextUtils.isEmpty(accountEditText.getText().toString().trim())
				|| TextUtils.isEmpty(passEditText.getText().toString().trim())) {
			loginView.setEnabled(false);
		} else {
			loginView.setEnabled(true);
		}
	}

	private class LoginClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String account = accountEditText.getText().toString().trim();
			String password = passEditText.getText().toString().trim();
			TaskDialogFragment fragment = TaskDialogFragment
					.newLoadingFragment("正在登录", false);
			fragment.setTask(new LoginTask(account, password));
			fragment.show(getFragmentManager(), null);
		}
	}

	private class RegisterClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.container, new RegisterFragment());
			ft.addToBackStack(null);
			ft.commit();
		}
	}

	private class LoginTask extends BaseFragmentTask {
		private String account, password;

		private LoginTask(String account, String password) {
			this.account = account;
			this.password = password;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// String response = null;
			int errorCode = YunmaoException.ERROR_CODE_DEFAULT;
			try {
				ServerHelper.getInstance().postForUserLogin(account, password);
			} catch (YunmaoException e) {
				errorCode = e.getErrorCode();
				// if(errorCode == YunmaoException.ERROR_CODE_NONE) {
				// response = e.getMessage();
				// }
			}
			// if(TextUtils.isEmpty(response))
			// return errorCode;

			return errorCode;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (isCancelled()) {
				return;
			}
			switch (result) {
			case YunmaoException.ERROR_CODE_NONE:
				// Util.toaster("", bufferType);
				break;
			}
		}
	}

	private class MyTextChangeWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			refreshLoginState();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

	}
}
