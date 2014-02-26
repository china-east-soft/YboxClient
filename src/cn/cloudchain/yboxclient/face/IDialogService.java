package cn.cloudchain.yboxclient.face;

import android.support.v4.app.DialogFragment;
import android.view.View;

public interface IDialogService {
	public void onClick(DialogFragment fragment, int actionId);

	public View getDialogView();

}
