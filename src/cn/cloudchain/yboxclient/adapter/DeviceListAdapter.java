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
import cn.cloudchain.yboxclient.helper.Helper;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;

public class DeviceListAdapter extends BaseAdapter {
	private Context context;
	private IBlackListService listener;
	private List<DeviceInfo> devices;
	private String myMac = "";

	public DeviceListAdapter(Context context, IBlackListService listener) {
		this.context = context;
		myMac = Helper.getInstance().getDevicesMac(context);
	}

	public void setDevices(List<DeviceInfo> devices) {
		this.devices = devices;
	}

	@Override
	public int getCount() {
		return devices == null ? 0 : devices.size();
	}

	@Override
	public DeviceInfo getItem(int position) {
		DeviceInfo item = null;
		if (devices != null && devices.size() > position) {
			item = devices.get(position);
		}
		return item;
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
			view = inflater.inflate(R.layout.list_item_devices, null);
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
		holder.action.setText(item.blocked ? R.string.device_clear_blacklist
				: R.string.device_add_blacklist);
		if (item.mac.equals(myMac)) {
			holder.action.setVisibility(View.GONE);
		} else {
			holder.action.setVisibility(View.VISIBLE);
		}
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
