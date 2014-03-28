package cn.cloudchain.yboxclient.fragment;

import cn.cloudchain.yboxclient.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TransferFragment extends Fragment {
	public static final String BUNDLE_TYPE = "type";
	public static final int TYPE_UPLOAD = 1;
	public static final int TYPE_DOWNLOAD = 2;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.base_pulltorefresh_listview, container, false);
		return view;
	}

}
