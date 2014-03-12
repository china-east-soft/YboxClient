package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import cn.cloudchain.yboxclient.R;

public class ShrikTouchListener implements OnTouchListener {
	private boolean shrink = false;
	private Context context;

	public ShrikTouchListener(Context context) {
		this.context = context;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!shrink) {
				shrink = true;
				Animation anim = AnimationUtils.loadAnimation(context,
						R.anim.click_scale);
				v.startAnimation(anim);
				anim.setFillAfter(true);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			if (shrink) {
				shrink = false;
				Animation anim = AnimationUtils.loadAnimation(context,
						R.anim.click_scale_back);
				v.startAnimation(anim);
				anim.setFillAfter(true);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (shrink) {
				shrink = false;
				Animation anim = AnimationUtils.loadAnimation(context,
						R.anim.click_scale_back);
				v.startAnimation(anim);
				anim.setFillAfter(true);
			}
			break;
		}

		return false;
	}

}