package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;

public class TrafficView extends FrameLayout {
	private TextView trafficDetail;
	private ProgressWheel trafficProgress;

	public TrafficView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public TrafficView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public TrafficView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		View view = inflate(context, R.layout.grid_item_traffic, null);
		trafficDetail = (TextView) view.findViewById(R.id.traffic_detail);
		trafficProgress = (ProgressWheel) view
				.findViewById(R.id.traffic_progress);
		addView(view);
		setOnTouchListener(new ShrikTouchListener(getContext()));
	}

	public void setTrafficDetail(long used, long total) {
		String usedText = "";
		String totalText = "";
		String progressText = "";

		if (used < 0) {
			usedText = "?";
			totalText = "?";
		} else if (total < 0) {
			totalText = "不限";
			usedText = Formatter.formatShortFileSize(getContext(), used);
		} else {
			int percent = (int) (100 * used / total);
			trafficProgress.setProgress(percent > 100 ? 100 : percent);
			long remain = total - used;
			progressText = remain <= 0 ? "已用完" : Formatter.formatShortFileSize(
					getContext(), remain);
			usedText = Formatter.formatShortFileSize(getContext(), used);
			totalText = Formatter.formatShortFileSize(getContext(), total);
		}
		trafficProgress.setText(progressText);
		trafficDetail.setText(getResources().getString(
				R.string.status_traffic_month, usedText, totalText));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = getMeasuredWidth();
		setMeasuredDimension(size, size);
	}
}
