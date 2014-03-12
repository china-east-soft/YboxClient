package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.utils.Util;

public class SignalView extends FrameLayout {
	private TextView signalStrength;

	public SignalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public SignalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public SignalView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		setBackgroundColor(getResources().getColor(R.color.status_signal));

		TextView title = new TextView(context);
		title.setText(R.string.status_signal_strength);
		title.setTextColor(Color.WHITE);
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		LayoutParams titleParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		titleParams.gravity = Gravity.TOP | Gravity.LEFT;
		titleParams.leftMargin = titleParams.topMargin = Util.convertDpToPx(5);
		addView(title, titleParams);

		ImageView icon = new ImageView(context);
		icon.setImageResource(R.drawable.status_signal_strength);
		LayoutParams iconParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		iconParams.gravity = Gravity.CENTER;
		addView(icon, iconParams);

		signalStrength = new TextView(context);
		signalStrength.setTextColor(Color.WHITE);
		signalStrength.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		LayoutParams signalParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		signalParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		signalParams.rightMargin = signalParams.bottomMargin = Util
				.convertDpToPx(10);
		addView(signalStrength, signalParams);
	}

	public void setSignalStrength(String text) {
		signalStrength.setText(text);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = getMeasuredWidth();
		setMeasuredDimension(size, size);
	}
}
