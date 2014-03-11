package cn.cloudchain.yboxclient.fragment;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.dialog.LisenceDialogFragment;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.ServerHelper;
import cn.cloudchain.yboxclient.helper.WeakHandler;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;
import cn.cloudchain.yboxclient.utils.LogUtil;
import cn.cloudchain.yboxclient.utils.PreferenceUtil;
import cn.cloudchain.yboxclient.utils.Util;

public class RegisterFragment extends Fragment implements OnClickListener {
	public static final String TAG = RegisterFragment.class.getSimpleName();

	private View phoneLogin;
	private Button phoneVerify;
	private EditText phoneNum;
	private EditText phoneVerifyContent;
	private EditText passEditText;

	private CheckBox lisenceCheck;
	private Button lisenceDetailButton;

	private ActionBar actionbar;

	private InputMethodManager inputManager;
	private long startTime = 0L;
	private Timer verifyTimer;
	private VerifyTimerHandler handler = new VerifyTimerHandler(this);
	private static int MAX_VERIFY_TIME = 60;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		actionbar = ((ActionBarActivity) activity).getSupportActionBar();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		inputManager = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		View v = inflater.inflate(R.layout.layout_register, container, false);
		phoneLogin = v.findViewById(R.id.register_phone);
		phoneVerify = (Button) v.findViewById(R.id.phone_verify);
		lisenceCheck = (CheckBox) v.findViewById(R.id.register_checkbox);
		lisenceDetailButton = (Button) v.findViewById(R.id.register_lisence);

		lisenceDetailButton.setPaintFlags(lisenceDetailButton.getPaintFlags()
				| Paint.UNDERLINE_TEXT_FLAG);

		phoneLogin.setOnClickListener(this);
		phoneVerify.setOnClickListener(this);
		lisenceCheck.setOnCheckedChangeListener(new LisenceCheckListener());
		lisenceDetailButton.setOnClickListener(this);

		phoneNum = (EditText) v.findViewById(R.id.phone_num_edit);
		phoneVerifyContent = (EditText) v.findViewById(R.id.verify_edit);
		passEditText = (EditText) v.findViewById(R.id.pass_edit);
		phoneNum.setOnFocusChangeListener(new LoseFocusListener());
		phoneVerifyContent.setOnFocusChangeListener(new LoseFocusListener());
		passEditText.setOnFocusChangeListener(new LoseFocusListener());
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		actionbar.setTitle(R.string.register_title);
		boolean isChecked = PreferenceUtil.getBoolean(
				PreferenceUtil.PREF_LISENCE_CHECK_STATE, false);
		lisenceCheck.setChecked(isChecked);
		phoneVerify.setEnabled(isChecked);
		phoneLogin.setEnabled(isChecked);

		phoneNum.setText(PreferenceUtil.getString(
				PreferenceUtil.PREF_TEMP_PHONE, ""));
		startTime = PreferenceUtil.getLong(
				PreferenceUtil.PREF_PHONE_VERIFY_STARTTIME, 0);
		handler.sendEmptyMessage(VerifyTimerHandler.MSG_TIMER_START);
	}

	@Override
	public void onPause() {
		super.onPause();
		PreferenceUtil.putLong(PreferenceUtil.PREF_PHONE_VERIFY_STARTTIME,
				startTime);
		if (phoneNum != null) {
			PreferenceUtil.putString(PreferenceUtil.PREF_TEMP_PHONE, phoneNum
					.getText().toString().trim());
		}
		if (verifyTimer != null) {
			verifyTimer.cancel();
			verifyTimer = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register_phone: {
			String number = phoneNum.getText().toString().trim();
			String verify = phoneVerifyContent.getText().toString().trim();
			String password = passEditText.getText().toString().trim();

			if (TextUtils.isEmpty(number)) {
				phoneNum.requestFocusFromTouch();
				showToast(R.string.phone_empty);
			} else if (!TextUtils.isDigitsOnly(number)) {
				phoneNum.requestFocusFromTouch();
				showToast(R.string.phone_wrong_format);
			} else if (number.length() != 11) {
				phoneNum.requestFocusFromTouch();
				showToast(R.string.phone_length_wrong);
			} else if (TextUtils.isEmpty(verify)) {
				phoneVerifyContent.requestFocusFromTouch();
				showToast(R.string.verify_empty);
			} else if (verify.length() < 1) {
				phoneVerifyContent.requestFocusFromTouch();
				showToast(R.string.verify_length_wrong);
			} else if (TextUtils.isEmpty(password)) {
				showToast(R.string.register_pass_empty);
			} else {
				sendForLogin(number, verify, password);
			}
			break;
		}
		case R.id.phone_verify: {
			String number = phoneNum.getText().toString().trim();
			if (TextUtils.isEmpty(number)) {
				phoneNum.requestFocusFromTouch();
				showToast(R.string.phone_empty);
			} else if (!TextUtils.isDigitsOnly(number)) {
				phoneNum.requestFocusFromTouch();
				showToast(R.string.phone_wrong_format);
			} else if (number.length() != 11) {
				phoneNum.requestFocusFromTouch();
				showToast(R.string.phone_length_wrong);
			} else {
				sendForVerfiyNumber(number);
			}
			break;
		}
		case R.id.register_lisence: {
			LisenceDialogFragment fragment = new LisenceDialogFragment();
			fragment.show(getFragmentManager(),
					LisenceDialogFragment.class.getSimpleName());
			break;
		}
		}
	}

	private void sendForVerfiyNumber(String phone_num) {
		startTime = System.currentTimeMillis();
		handler.sendEmptyMessage(VerifyTimerHandler.MSG_TIMER_START);
		TaskDialogFragment dialog = TaskDialogFragment.newLoadingFragment(
				"正在请求验证码", true);
		VerifyTask task = new VerifyTask(phone_num);
		dialog.setTask(task);
		dialog.show(getFragmentManager(), TaskDialogFragment.TAG);
	}

	private void sendForLogin(String phone_num, String verify_code,
			String password) {
		TaskDialogFragment dialog = TaskDialogFragment.newLoadingFragment(
				"登录中...", true);
		LoginTask task = new LoginTask(phone_num, verify_code, password);
		dialog.setTask(task);
		dialog.show(getFragmentManager(), TaskDialogFragment.TAG);
	}

	private void handleRegisterSuccess() {
		getActivity().onBackPressed();
	}

	private void startTimer() {
		if (verifyTimer == null) {
			verifyTimer = new Timer();
		}
		verifyTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				int diff = (int) (System.currentTimeMillis() - startTime) / 1000;
				if (diff >= MAX_VERIFY_TIME || diff < 0) {
					handler.sendEmptyMessage(VerifyTimerHandler.MSG_TIMER_CANCEL);
					return;
				}
				Message msg = handler
						.obtainMessage(VerifyTimerHandler.MSG_TIMER_UPDATE);
				msg.arg1 = MAX_VERIFY_TIME - diff;
				handler.sendMessage(msg);
			}
		}, 0, 1000);
	}

	private static class VerifyTimerHandler extends
			WeakHandler<RegisterFragment> {
		private static final int MSG_TIMER_START = 0;
		private static final int MSG_TIMER_CANCEL = 1;
		private static final int MSG_TIMER_UPDATE = 2;
		private static final int MSG_LOGIN_SUCCESS = 3;

		public VerifyTimerHandler(RegisterFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			RegisterFragment owner = getOwner();
			if (owner == null)
				return;

			switch (msg.what) {
			case MSG_TIMER_START:
				long diff = System.currentTimeMillis() - owner.startTime;
				if (diff >= MAX_VERIFY_TIME * 1000) {
					sendEmptyMessage(MSG_TIMER_CANCEL);
				} else {
					owner.phoneVerify.setClickable(false);
					owner.startTimer();
				}
				break;
			case MSG_TIMER_CANCEL:
				owner.startTime = 0;
				PreferenceUtil
						.remove(PreferenceUtil.PREF_PHONE_VERIFY_STARTTIME);
				owner.phoneVerify.setClickable(true);
				owner.phoneVerify.setText(R.string.phone_token_capture);
				if (owner.verifyTimer != null) {
					owner.verifyTimer.cancel();
					owner.verifyTimer = null;
				}
				break;
			case MSG_TIMER_UPDATE:
				owner.phoneVerify.setText(owner.getResources().getString(
						R.string.phone_token_capture_later, msg.arg1));
				break;
			case MSG_LOGIN_SUCCESS:
				owner.handleRegisterSuccess();
				break;
			}
		}

	}

	private class VerifyTask extends BaseFragmentTask {
		private static final int RESULT_SUCCESS = 2000;
		private static final int RESULT_PARAMS_WRONG = 2001;
		private static final int RESULT_FAIL = 2002;
		private static final int REQUEST_FAIL = 2003;
		private String phoneNumber;

		public VerifyTask(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			super.doInBackground(params);
			int result = RESULT_FAIL;
			try {
				ServerHelper.getInstance().postForVerify(phoneNumber);
			} catch (YunmaoException e) {
				int errorCode = e.getErrorCode();
				if (errorCode == YunmaoException.ERROR_CODE_NONE) {
					result = RESULT_SUCCESS;
				} else if (e.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
					result = RESULT_PARAMS_WRONG;
				} else if (e.getStatusCode() == YunmaoException.STATUS_CODE_REQUEST_FAIL) {
					result = REQUEST_FAIL;
				}
			}
			LogUtil.i(TAG, "VerifyTask statusCode = " + result);
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result != RESULT_SUCCESS) {
				handler.sendEmptyMessage(VerifyTimerHandler.MSG_TIMER_CANCEL);
			}
			switch (result) {
			case RESULT_SUCCESS:
				showToast(R.string.verify_request_success);
				break;
			case RESULT_PARAMS_WRONG:
				showToast(R.string.request_params_wrong);
				break;
			case RESULT_FAIL:
				showToast(R.string.verify_phone_fail);
				break;
			case REQUEST_FAIL:
				showToast(R.string.verify_request_fail);
				break;
			}
		}

	}

	private class LoginTask extends BaseFragmentTask {
		private static final int RESULT_SUCCESS = 2000;
		private static final int RESULT_PARAMS_WRONG = 2001;
		private static final int RESULT_FAIL = 2002;
		private static final int RESULT_VERIFY_WRONG = 2003;
		private static final int RESULT_VERIFY_OUTDATE = 2004;

		private String phoneNumber;
		private String verifyCode;
		private String password;

		public LoginTask(String phoneNumber, String verifyCode, String password) {
			this.phoneNumber = phoneNumber;
			this.verifyCode = verifyCode;
			this.password = password;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			super.doInBackground(params);
			int result = RESULT_FAIL;
			try {
				ServerHelper.getInstance().postForVerifyPhone(phoneNumber,
						verifyCode, password);
			} catch (YunmaoException e) {
				result = e.getStatusCode();
				int errorCode = e.getErrorCode();
				if (errorCode == YunmaoException.ERROR_CODE_NONE) {
					result = RESULT_SUCCESS;
				} else if (errorCode == 1) {
					result = RESULT_VERIFY_OUTDATE;
				} else if (errorCode == 2) {
					result = RESULT_VERIFY_WRONG;
				} else if (e.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
					result = RESULT_PARAMS_WRONG;
				}
			}
			LogUtil.i(TAG, "VerifyTask statusCode = " + result);
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			switch (result) {
			case RESULT_SUCCESS:
				showToast(R.string.login_request_success);
				if (RegisterFragment.this.isVisible()) {
					handler.sendEmptyMessage(VerifyTimerHandler.MSG_LOGIN_SUCCESS);
				}
				break;
			case RESULT_VERIFY_OUTDATE:
				showToast(R.string.login_verify_timeout);
				break;
			case RESULT_VERIFY_WRONG:
				showToast(R.string.login_verify_invalid);
				break;
			case RESULT_PARAMS_WRONG:
				showToast(R.string.request_params_wrong);
				break;
			default:
				showToast(R.string.login_request_fail);
				break;
			}
		}
	}

	private class LoseFocusListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!phoneNum.isFocused() && !phoneVerifyContent.isFocused()
					&& !passEditText.isFocused()) {
				inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	}

	private class LisenceCheckListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			LogUtil.i(TAG, "onCheckChange");
			PreferenceUtil.putBoolean(PreferenceUtil.PREF_LISENCE_CHECK_STATE,
					isChecked);
			phoneVerify.setEnabled(isChecked);
			phoneLogin.setEnabled(isChecked);
		}

	}

	private Toast toast;

	private void showToast(int resId) {
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
		toast = Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT);

		int padding = Util.convertDpToPx(12);
		TextView view = new TextView(getActivity());
		view.setPadding(padding, padding, padding, padding);
		view.setMinWidth(Util.convertDpToPx(150));
		view.setGravity(Gravity.CENTER);
		// view.setBackgroundResource(R.drawable.toast_bcg);
		view.setTextColor(Color.WHITE);
		view.setText(Html.fromHtml(Util.getString(resId, "")));
		toast.setView(view);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
