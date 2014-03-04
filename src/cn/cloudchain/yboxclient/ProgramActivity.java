package cn.cloudchain.yboxclient;

import cn.cloudchain.yboxclient.adapter.ProgramGridAdapter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.GridView;

public class ProgramActivity extends ActionBarActivity {
	private GridView gridView;
	private ProgramGridAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("直播");
		setContentView(R.layout.layout_program);
		gridView = (GridView) this.findViewById(R.id.gridview);
		adapter = new ProgramGridAdapter(this);
		gridView.setAdapter(adapter);
	}
	
	
}
