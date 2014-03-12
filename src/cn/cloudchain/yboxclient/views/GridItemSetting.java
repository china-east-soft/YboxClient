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
import android.view.View;
import cn.cloudchain.yboxclient.R;

public class GridItemSetting extends View {
	private Drawable image;
	private String title;
	private String subDes;

	private int titleColor;
	private int subDesColor;

	private int titleSize;
	private int subDesSize;

	private boolean withPop = false;

	private TextPaint textPaint;
	private Rect bounds = new Rect();

	public GridItemSetting(Context context) {
		this(context, null);
	}

	public GridItemSetting(Context context, AttributeSet attrs) {
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

		withPop = typeArray.getBoolean(R.styleable.GridItem2_withPop, false);

		typeArray.recycle();
		setClickable(true);
		setOnTouchListener(new ShrikTouchListener(getContext()));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int caW = canvas.getWidth();
		int caH = canvas.getHeight();

		if (image != null) {
			canvas.save();
			int width = image.getIntrinsicWidth();
			int height = image.getIntrinsicHeight();
			image.setBounds(0, 0, width, height);
			image.setState(getDrawableState());
			float dx = (caW - width) / 2.f;
			float dy = (caH - height) / 2.f;
			canvas.translate(dx, dy);
			image.draw(canvas);
			canvas.restore();
		}

		if (!TextUtils.isEmpty(title)) {
			textPaint.setColor(titleColor);
			textPaint.setTextSize(titleSize);
			textPaint.getTextBounds(title, 0, title.length(), bounds);
			float y = caH - 15;
			float x = caW / 2.f - bounds.exactCenterX();
			canvas.drawText(title, x, y, textPaint);
		}

		if (!TextUtils.isEmpty(subDes)) {
			textPaint.setTextSize(subDesSize);
			textPaint.getTextBounds(subDes, 0, subDes.length(), bounds);
			float x = caW - bounds.width() - 15;
			float y = 15 + bounds.height();

			if (withPop) {
				textPaint.setColor(0xFFFFFFFF);
				float radius = Math.max(bounds.width(), bounds.height()) * 0.75f;
				canvas.drawCircle(x + bounds.centerX(), y + bounds.centerY(),
						radius, textPaint);
			}

			textPaint.setColor(subDesColor);
			canvas.drawText(subDes, x, y, textPaint);
		}
	}

	public void setSubDes(String text) {
		subDes = text;
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = getMeasuredWidth();
		setMeasuredDimension(size, size);
	}
}
