package cn.cloudchain.yboxclient.views;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

public class MainGridLayout extends GridLayout {
	public int[] size;
	public int columns;
	public int rows;
	public int view_width = -1;
	public int view_height = -1;
	public boolean isVertical = true;
	public ArrayList<View> views;

	public MainGridLayout(Context context) {
		super(context);
		if (view_width == -1) {
			DisplayMetrics metrics = this.getResources().getDisplayMetrics();
			int width = metrics.widthPixels;
			int height = metrics.heightPixels - 120;
			view_width = width - this.getPaddingLeft() - this.getPaddingRight();
			view_height = height - this.getPaddingTop()
					- this.getPaddingBottom();
		}
		views = new ArrayList<View>();
		setupVertical();
	}

	public void setupVertical() {
		size = getBaseSizeHorizontal();
		this.setRowCount(rows);
		this.setColumnCount(-1);
		this.setOrientation(this.VERTICAL);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		this.setLayoutParams(params);
	}

	public int[] getBaseSizeHorizontal() {
		int[] size = new int[2];

		float width_height_ratio = (4.0f / 3.0f);

		int base_height = getBaseHeight();
		int base_width = (int) (base_height * width_height_ratio);

		size[0] = base_width; // width
		size[1] = base_height; // height
		return size;
	}

	public int getBaseHeight() {
		if (view_height < 350) {
			rows = 2;
		} else if (view_height < 650) {
			rows = 3;
		} else if (view_height < 1050) {
			rows = 4;
		} else if (view_height < 1250) {
			rows = 5;
		} else {
			rows = 6;
		}
		return (view_height / rows);
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		view_width = xNew;
		view_height = yNew;
	}
}
