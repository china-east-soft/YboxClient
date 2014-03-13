package cn.cloudchain.yboxclient.dialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.utils.Util;

public class WifiChooseDialogFragment extends DialogFragment {
	private List<ScanResult> resultArray;
	private IWifiChooseService listener;

	public interface IWifiChooseService {
		public void onItemClick(ScanResult result);
	}

	public static WifiChooseDialogFragment newInstance(
			ArrayList<ScanResult> list) {
		WifiChooseDialogFragment fragment = new WifiChooseDialogFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList("list", list);
		fragment.setArguments(args);
		return fragment;
	}

	public void setWifiChooseService(IWifiChooseService listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resultArray = getArguments().getParcelableArrayList("list");
		setCancelable(true);
		setStyle(STYLE_NORMAL, R.style.CommonDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_normal, container, false);
		TextView title = (TextView) view.findViewById(R.id.dialog_title);
		title.setText(R.string.wifi_choose_title);
		title.setVisibility(View.VISIBLE);

		Button positive = (Button) view
				.findViewById(R.id.dialog_positive_button);
		positive.setVisibility(View.VISIBLE);
		positive.setText(R.string.cancel);
		view.findViewById(R.id.dialog_buttons_lay).setVisibility(View.VISIBLE);

		ListView listView = (ListView) view.findViewById(R.id.dialog_listview);
		listView.setVisibility(View.VISIBLE);
		listView.setAdapter(new MyAdapter(getActivity(),
				android.R.layout.simple_list_item_1, resultArray));

		positive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (resultArray != null && resultArray.size() > arg2
						&& listener != null) {
					listener.onItemClick(resultArray.get(arg2));
				}
				dismiss();
			}
		});
		return view;
	}

	private class MyAdapter extends ArrayAdapter<String> {
		private List<ScanResult> array;

		public MyAdapter(Context context, int resource, List<ScanResult> array) {
			super(context, resource);
			this.array = array;
		}

		@Override
		public String getItem(int position) {
			if (array != null && array.size() > position) {
				return Util.removeQuotOfSSID(array.get(position).SSID);
			}
			return "";
		}

		@Override
		public int getCount() {
			return array == null ? 0 : array.size();
		}

	}
}
