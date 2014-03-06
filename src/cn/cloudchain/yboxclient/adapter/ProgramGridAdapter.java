package cn.cloudchain.yboxclient.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.bean.GuideBean;
import cn.cloudchain.yboxclient.bean.ProgramBean;
import cn.cloudchain.yboxclient.helper.ApHelper;
import cn.cloudchain.yboxclient.utils.CalendarUtil;

public class ProgramGridAdapter extends BaseAdapter {
	// private Context context;
	private List<ProgramBean> mProgramList;
	private LayoutInflater mInflater;

	public ProgramGridAdapter(Context context) {
		// this.context = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setProgramList(List<ProgramBean> programList) {
		this.mProgramList = programList;
	}

	@Override
	public int getCount() {
		return mProgramList == null ? 0 : mProgramList.size();
	}

	@Override
	public Object getItem(int arg0) {
		Object obj = null;
		if (mProgramList != null && mProgramList.size() > arg0) {
			obj = mProgramList.get(arg0);
		}
		return obj;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View v = convertView;

		if (v == null) {
			v = mInflater.inflate(R.layout.grid_item_program, null);
			holder = new ViewHolder();
			holder.programIcon = (ImageView) v.findViewById(R.id.program_icon);
			holder.programName = (TextView) v
					.findViewById(R.id.program_reminder);
			holder.programGuide = (TextView) v
					.findViewById(R.id.program_detail);
			holder.playBtn = (ImageButton) v.findViewById(R.id.program_state);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		final ProgramBean bean = (ProgramBean) getItem(position);

		List<GuideBean> guideList = bean.getGuideList();
		int guideSize = guideList == null ? 0 : guideList.size();

		if (guideSize > 0) {
			int currentIndex = bean.getCurrentGuideIndex();
			int nextIndex = bean.getNextGuideIndex();
			holder.programGuide.setVisibility(View.GONE);

			if (currentIndex >= 0 && currentIndex < guideSize) {
				final GuideBean guide = guideList.get(currentIndex);
				StringBuilder builder = new StringBuilder();
				builder.append(CalendarUtil.getOnlyHourMinute(guide
						.getGuideStartTime()));
				builder.append(' ');
				builder.append(guide.getGuideName());
				holder.programName.setText(R.string.guide_now_title);
				holder.programGuide.setText(builder);
			}

			if (nextIndex >= 0 && nextIndex < guideSize) {
				final GuideBean guide = guideList.get(nextIndex);
				StringBuilder builder = new StringBuilder();
				builder.append(CalendarUtil.getOnlyHourMinute(guide
						.getGuideStartTime()));
				builder.append(' ');
				builder.append(guide.getGuideName());
				holder.programName.setText(R.string.guide_next_title);
				holder.programGuide.setText(builder);
			}
		} else {
			holder.programName.setText(R.string.guides_empty);
		}

		if (ApHelper.getInstance().getSwitchState(bean) == ApHelper.STATE_SWITCHABLE) {
			holder.playBtn.setImageResource(R.drawable.program_play);
		} else {
			holder.playBtn.setImageResource(R.drawable.program_pause);
		}

		return v;
	}

	private class ViewHolder {
		ImageView programIcon;
		TextView programName;
		TextView programGuide;
		ImageButton playBtn;
	}

}
