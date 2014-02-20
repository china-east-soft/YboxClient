package cn.cloudchain.yboxclient.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.face.IBlackListService;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;

public class HotspotListAdapter extends BaseAdapter {
	private Context context;
	private IBlackListService listener;
	private List<DeviceInfo> deviceInfo;

	public HotspotListAdapter(Context context, IBlackListService listener) {
		this.context = context;
		this.listener = listener;
	}

	public void setDeviceList(List<DeviceInfo> deviceList) {
		this.deviceInfo = deviceList;
	}

	@Override
	public int getCount() {
		return deviceInfo == null ? 0 : deviceInfo.size();
	}

	@Override
	public DeviceInfo getItem(int position) {
		DeviceInfo info = null;
		if (position < getCount()) {
			info = deviceInfo.get(position);
		}
		return info;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_item_hotspot, null);
			holder = new ViewHolder();
			holder.macText = (TextView) view.findViewById(R.id.mac);
			holder.ipText = (TextView) view.findViewById(R.id.ip);
			holder.action = (Button) view.findViewById(R.id.action);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final DeviceInfo item = getItem(position);
		if (item == null)
			return view;

		holder.macText.setText(item.mac);
		holder.ipText.setText(item.ip);
		holder.action.setText(item.blocked ? "解除黑名单" : "加入黑名单");
		holder.action.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.dealBlackList(item);
				}
			}
		});
		return view;
	}

	private class ViewHolder {
		TextView macText;
		TextView ipText;
		Button action;
	}

}
