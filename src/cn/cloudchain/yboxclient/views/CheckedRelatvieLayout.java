package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckedRelatvieLayout extends RelativeLayout implements Checkable {
	private CheckBox mCheckbox;

	public CheckedRelatvieLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCheckbox = null;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View v = getChildAt(i);
			if (v instanceof CheckBox) {
				mCheckbox = (CheckBox) v;
			}
		}
	}

	@Override
	public boolean isChecked() {
		return mCheckbox == null ? false : mCheckbox.isChecked();
	}

	@Override
	public void setChecked(boolean checked) {
		if (mCheckbox != null) {
			mCheckbox.setChecked(checked);
		}
	}

	@Override
	public void toggle() {
		if (mCheckbox != null) {
			mCheckbox.toggle();
		}
	}

}
