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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import cn.cloudchain.yboxclient.R;

public class GridItem1 extends View implements OnTouchListener {
	final String TAG = GridItem1.class.getSimpleName();
	private Drawable image;
	private Drawable toggle;
	private boolean checked = false;
	private String textStr;
	private int textSize;
	private int textColor;
	private int paddingLeft;
	private int paddingBottom;
	// private float mScaleFactor = 1.f;

	private TextPaint textPaint;
	private Rect bounds = new Rect();

	private boolean mBroadcasting;
	private OnCheckedChangeListener mOnCheckedChangeListener;

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	public static interface OnCheckedChangeListener {
		public void onCheckedChanged(GridItem1 view, boolean isChecked);
	}

	public GridItem1(Context context) {
		this(context, null);
	}

	public GridItem1(Context context, AttributeSet attrs) {
		super(context, attrs);
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.density = getResources().getDisplayMetrics().density;

		TypedArray typeArray = context.obtainStyledAttributes(attrs,
				R.styleable.GridItem1);

		image = typeArray.getDrawable(R.styleable.GridItem1_gridImage);
		toggle = typeArray.getDrawable(R.styleable.GridItem1_toggleImage);
		checked = typeArray.getBoolean(R.styleable.GridItem1_checked, false);

		textStr = typeArray.getText(R.styleable.GridItem1_android_text)
				.toString();
		textSize = typeArray.getDimensionPixelSize(
				R.styleable.GridItem1_android_textSize, 14);
		textColor = typeArray.getColor(R.styleable.GridItem1_android_textColor,
				0xFF000000);

		paddingLeft = typeArray.getDimensionPixelSize(
				R.styleable.GridItem1_android_paddingLeft, 8);
		paddingBottom = typeArray.getDimensionPixelSize(
				R.styleable.GridItem1_android_paddingBottom, 8);

		typeArray.recycle();

		setClickable(true);
		setChecked(checked);
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
			image.setBounds(0, 0, width, image.getIntrinsicHeight());
			image.setState(getDrawableState());
			int dx = (caW - width) / 2;
			canvas.translate(dx, dx - 6);
			image.draw(canvas);
			canvas.restore();
		}

		if (toggle != null) {
			canvas.save();
			toggle.setBounds(0, 0, 40, 8);
			toggle.setState(getDrawableState());
			int dy = (int) (0.65 * caW);
			int dx = (caW - 40) / 2;
			canvas.translate(dx, dy);
			toggle.draw(canvas);
			canvas.restore();
		}

		if (!TextUtils.isEmpty(textStr)) {
			textPaint.setColor(textColor);
			textPaint.setTextSize(textSize);
			textPaint.getTextBounds(textStr, 0, textStr.length(), bounds);

			canvas.drawText(textStr, paddingLeft,
					caH - paddingBottom + bounds.centerY() / 2, textPaint);
		}

	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		if (toggle != null && toggle.isStateful()) {
			toggle.setState(getDrawableState());
		}

		if (image != null && image.isStateful()) {
			image.setState(getDrawableState());
		}
		super.drawableStateChanged();
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		if (toggle == null)
			return;
		if (this.checked != checked) {
			this.checked = checked;
			Log.i(TAG, "set check = " + this.checked);
			refreshDrawableState();

			if (mBroadcasting) {
				return;
			}

			mBroadcasting = true;
			if (mOnCheckedChangeListener != null) {
				mOnCheckedChangeListener.onCheckedChanged(this, checked);
			}
			mBroadcasting = false;
		}
	}

	public void toggle() {
		if (toggle == null)
			return;
		setChecked(!checked);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN: {
			Animation anim = AnimationUtils.loadAnimation(getContext(),
					R.anim.click_scale);
			v.startAnimation(anim);
			anim.setFillAfter(true);
			break;
		}
		case MotionEvent.ACTION_OUTSIDE:
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
