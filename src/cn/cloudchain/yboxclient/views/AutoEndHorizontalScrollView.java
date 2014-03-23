package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * 自动滚动到最有段的HorizontalScrollView
 * 
 * @author lazzy
 * 
 */
public class AutoEndHorizontalScrollView extends HorizontalScrollView {

	public AutoEndHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		fullScroll(HorizontalScrollView.FOCUS_RIGHT);
	}

}
