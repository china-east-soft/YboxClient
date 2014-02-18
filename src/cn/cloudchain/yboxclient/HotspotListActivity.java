package cn.cloudchain.yboxclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class HotspotListActivity extends Activity {
	final String TAG = HotspotListActivity.class.getSimpleName();
	private ListView listView;
	private String jsonContent;
	private List<String> array;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_hotspot_list);
		jsonContent = getIntent().getStringExtra("content");
		array = getArray();
		listView = (ListView) this.findViewById(R.id.listview);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, array));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 >= array.size()) {
					return;
				}
			}
		});

		this.findViewById(R.id.clear).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});
	}

	private List<String> getArray() {
		List<String> array = new ArrayList<String>();
		try {
			JSONObject object = new JSONObject(jsonContent);
			boolean result = object.optBoolean("result", false);
			if (!result) {
				Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show();
				return array;
			}
			JSONArray devices = object.optJSONArray("devices");
			for (int i = 0; i < devices.length(); i++) {
				JSONObject device = devices.getJSONObject(i);
				String mac = device.optString("mac");
				if (!TextUtils.isEmpty(mac))
					array.add(mac);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array;
	}

}
