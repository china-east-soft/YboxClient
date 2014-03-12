package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;

public class BatteryView extends FrameLayout {
	private BatteryProgress batteryProgress;
	private TextView batteryProgressText;
	private TextView batteryTime;

	public BatteryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public BatteryView(Context context) {
		super(context);
		initView(context);
	}

	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		View view = inflate(context, R.layout.grid_item_battery, null);
		batteryProgress = (BatteryProgress) view
				.findViewById(R.id.batteryProgress);
		batteryProgressText = (TextView) view
				.findViewById(R.id.batteryProgressText);
		batteryTime = (TextView) view.findViewById(R.id.batteryTime);
		addView(view);
		
		setOnTouchListener(new ShrikTouchListener(getContext()));
	}

	public void setBatteryRemain(int percent) {
		if (percent >= 0) {
			batteryProgress.setProgress(percent);
			batteryProgressText.setText(String.format("%d%%", percent));
		} else {
			batteryProgress.setProgress(100);
			batteryProgressText.setText("未知");
		}
	}

	public void setBatteryRemainTime(String time) {
		batteryTime.setText(time);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = getMeasuredWidth();
		setMeasuredDimension(size, size);
	}
}
