package cn.cloudchain.yboxclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import cn.cloudchain.yboxclient.R;

public class TvModeListAdapter extends BaseAdapter {
	private Context context;
	private String[] modes;

	public TvModeListAdapter(Context context) {
		this.context = context;
	}

	public void setModes(String[] modes) {
		this.modes = modes;
	}

	@Override
	public int getCount() {
		return modes == null ? 0 : modes.length;
	}

	@Override
	public String getItem(int position) {
		if (position < getCount()) {
			return modes[position];
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_item_tvmode, null);
			holder = new ViewHolder();
			holder.tvModeText = (CheckedTextView) convertView
					.findViewById(R.id.tvmode);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvModeText.setText(getItem(position));

		return convertView;
	}

	public class ViewHolder {
		CheckedTextView tvModeText;
	}

}
