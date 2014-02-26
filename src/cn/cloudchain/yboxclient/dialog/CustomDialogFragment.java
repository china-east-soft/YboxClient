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
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.face.IDialogService;
import cn.cloudchain.yboxclient.helper.Helper;

/**
 * 自定义对话框，继承 {@link DialogFragment}
 * 
 * @author lazzy
 * 
 */
public class CustomDialogFragment extends DialogFragment implements
		OnClickListener {
	public static final String TAG = CustomDialogFragment.class.getSimpleName();

	private static final String STATE_TITLE = "state_title";
	private static final String STATE_MESSAGE = "state_message";
	private static final String STATE_POSITIVE_NAME = "state_positive_name";
	private static final String STATE_NEGITIVE_NAME = "state_negitive_name";
	private static final String STATE_CANCELABLE = "state_cancelable";

	private CharSequence dialogTitle;
	private CharSequence dialogMessage;
	private CharSequence positiveName;
	private CharSequence negitiveName;

	private boolean isCancelable = true;
	private IDialogService service;

	public static CustomDialogFragment newInstance(int titleRes,
			int messageRes, int positiveRes, int negtiveRes,
			boolean isCancelable) {
		Helper helper = Helper.getInstance();
		return newInstance(helper.getString(titleRes),
				helper.getString(messageRes), helper.getString(positiveRes),
				helper.getString(negtiveRes), isCancelable);
	}

	public static CustomDialogFragment newInstance(CharSequence dialogTitle,
			CharSequence dialogMessage, CharSequence positiveName,
			CharSequence negitiveName, boolean isCancelable) {
		CustomDialogFragment fragment = new CustomDialogFragment();
		Bundle args = new Bundle();
		args.putCharSequence(STATE_TITLE, dialogTitle);
		args.putCharSequence(STATE_MESSAGE, dialogMessage);
		args.putCharSequence(STATE_POSITIVE_NAME, positiveName);
		args.putCharSequence(STATE_NEGITIVE_NAME, negitiveName);
		args.putBoolean(STATE_CANCELABLE, isCancelable);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * 设置对话框响应接口
	 * 
	 * @param service
	 */
	public void setDialogService(IDialogService service) {
		this.service = service;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		Bundle bundle = getArguments();
		if (bundle != null) {
			isCancelable = bundle.getBoolean(STATE_CANCELABLE, true);
			dialogTitle = bundle.getCharSequence(STATE_TITLE);
			dialogMessage = bundle.getCharSequence(STATE_MESSAGE);
			positiveName = bundle.getCharSequence(STATE_POSITIVE_NAME);
			negitiveName = bundle.getCharSequence(STATE_NEGITIVE_NAME);
		}

		setCancelable(isCancelable);
		setStyle(STYLE_NORMAL, R.style.CommonDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_normal, container, false);
		TextView mTitle = (TextView) v.findViewById(R.id.dialog_title);
		TextView mMessage = (TextView) v.findViewById(R.id.dialog_message);
		Button mPositive = (Button) v.findViewById(R.id.dialog_positive_button);
		Button mNegitive = (Button) v.findViewById(R.id.dialog_negtive_button);
		View mButtonLay = v.findViewById(R.id.dialog_buttons_lay);

		if (!TextUtils.isEmpty(dialogTitle)) {
			mTitle.setText(dialogTitle);
			mTitle.setVisibility(View.VISIBLE);
		}

		if (!TextUtils.isEmpty(dialogMessage)) {
			mMessage.setText(dialogMessage);
			mMessage.setVisibility(View.VISIBLE);
		}

		boolean showButtons = false;
		if (!TextUtils.isEmpty(positiveName)) {
			mPositive.setText(positiveName);
			mPositive.setVisibility(View.VISIBLE);
			showButtons = true;
		}
		if (!TextUtils.isEmpty(negitiveName)) {
			mNegitive.setText(negitiveName);
			mNegitive.setVisibility(View.VISIBLE);
			showButtons = true;
		}

		mPositive.setOnClickListener(this);
		mNegitive.setOnClickListener(this);

		if (showButtons) {
			mButtonLay.setVisibility(View.VISIBLE);
		}
		return v;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (service != null) {
			service.onClick(this, R.id.dialog_cancel);
		}
	}

	@Override
	public void onClick(View v) {
		int actionId = -1;
		switch (v.getId()) {
		case R.id.dialog_positive_button:
			actionId = R.id.dialog_click_positive;
			break;
		case R.id.dialog_negtive_button:
			actionId = R.id.dialog_click_negitive;
			break;
		}
		dismissAllowingStateLoss();
		if (service != null && actionId > 0) {
			service.onClick(this, actionId);
		}
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

}
