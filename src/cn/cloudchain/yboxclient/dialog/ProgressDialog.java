package cn.cloudchain.yboxclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;

/**
 * 普通的加载对话框，继承 Dialog
 * 
 * @author lazzy
 * 
 */
public class ProgressDialog extends Dialog {
	private CharSequence message;
	private boolean cancelable;
	private TextView mMessage;

	public ProgressDialog(Context context, CharSequence message,
			boolean cancelable) {
		super(context, R.style.CommonDialog);
		this.message = message;
		this.cancelable = cancelable;
	}

	public void changeMessage(CharSequence message) {
		this.message = message;
		if (mMessage != null) {
			mMessage.setText(message);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_custom_progress);
		mMessage = (TextView) this.findViewById(R.id.progress_message);
		mMessage.setText(message);
		setCancelable(cancelable);
		setCanceledOnTouchOutside(false);
	}
}