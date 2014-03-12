package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import cn.cloudchain.yboxclient.R;

public class BatteryProgress extends View {
	private Drawable batteryContainer;
	private int progressColor = 0xAA000000;
	private int progress = 100;
	private Paint textPaint = new Paint();
	private Rect bound = new Rect();

	public BatteryProgress(Context context) {
		this(context, null);
	}

	public BatteryProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		parseAttributes(context.obtainStyledAttributes(attrs,
				R.styleable.BatteryProgress));
	}

	private void parseAttributes(TypedArray a) {
		batteryContainer = a
				.getDrawable(R.styleable.BatteryProgress_batteryContainer);
		progressColor = a.getColor(R.styleable.BatteryProgress_progressColor,
				progressColor);
		a.recycle();
		textPaint.setColor(progressColor);
		textPaint.setStyle(Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = batteryContainer.getIntrinsicWidth();
		int height = batteryContainer.getIntrinsicHeight();

		batteryContainer.setBounds(0, 0, width, height);
		batteryContainer.draw(canvas);

		int containerHeight = height - 30;
		int progressHeight = containerHeight * progress / 100;
		bound.left = 6;
		bound.right = width - 6;
		bound.bottom = height - 8;
		bound.top = 22 + containerHeight - progressHeight;
		canvas.save();
		canvas.drawRect(bound, textPaint);
		canvas.restore();
	}

	public void setProgress(int progress) {
		this.progress = progress;
		invalidate();
	}

}
