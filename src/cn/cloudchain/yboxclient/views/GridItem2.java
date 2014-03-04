package cn.cloudchain.yboxclient.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import cn.cloudchain.yboxclient.R;

public class GridItem2 extends View implements OnTouchListener {
	private Drawable image;
	private String title;
	private String subDes;

	private int titleColor;
	private int subDesColor;

	private int titleSize;
	private int subDesSize;

	private TextPaint textPaint;
	private Rect bounds = new Rect();

	public GridItem2(Context context, AttributeSet attrs) {
		super(context, attrs);
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.density = getResources().getDisplayMetrics().density;

		TypedArray typeArray = context.obtainStyledAttributes(attrs,
				R.styleable.GridItem2);

		image = typeArray.getDrawable(R.styleable.GridItem2_gridImage);
		title = typeArray.getString(R.styleable.GridItem2_title);
		subDes = typeArray.getString(R.styleable.GridItem2_subDes);

		titleColor = typeArray.getColor(R.styleable.GridItem2_titleColor,
				0xFFFFFFFF);
		subDesColor = typeArray.getColor(R.styleable.GridItem2_subDesColor,
				0xFFFFFFFF);

		titleSize = typeArray.getDimensionPixelSize(
				R.styleable.GridItem2_titleSize, 18);
		subDesSize = typeArray.getDimensionPixelSize(
				R.styleable.GridItem2_subDesSize, 18);

		typeArray.recycle();
		setClickable(true);
		setOnTouchListener(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int caW = canvas.getWidth();
		int caH = canvas.getHeight();

		if (image != null) {
			canvas.save();
			int width = image.getIntrinsicWidth();
			int height = image.getIntrinsicWidth();
			image.setBounds(0, 0, width, height);
			image.setState(getDrawableState());
			float dx = caW / 3.f - width;
			float dy = (caH - height) / 2.f;
			canvas.translate(dx, dy);
			image.draw(canvas);
			canvas.restore();
		}

		if (!TextUtils.isEmpty(title)) {
			textPaint.setColor(titleColor);
			textPaint.setTextSize(titleSize);
			textPaint.getTextBounds(title, 0, title.length(), bounds);
			float y = caH / 2f - bounds.exactCenterY();
			float x = caW * 0.4f;
			canvas.drawText(title, x, y, textPaint);
		}

		if (!TextUtils.isEmpty(subDes)) {
			textPaint.setColor(subDesColor);
			textPaint.setTextSize(subDesSize);
			textPaint.getTextBounds(subDes, 0, subDes.length(), bounds);
			float x = caW - bounds.width() - 15;
			float y = caH - 15;
			canvas.drawText(subDes, x, y, textPaint);
		}
	}

	public void setSubDes(String text) {
		subDes = text;
		invalidate();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			Animation anim = AnimationUtils.loadAnimation(getContext(),
					R.anim.click_scale);
			v.startAnimation(anim);
			anim.setFillAfter(true);
			break;
		}
		case MotionEvent.ACTION_UP: {
			Animation anim = AnimationUtils.loadAnimation(getContext(),
					R.anim.click_scale_back);
			v.startAnimation(anim);
			anim.setFillAfter(true);
			break;
		}
		}
		return false;
	}

}
