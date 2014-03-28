package cn.cloudchain.yboxclient;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import cn.cloudchain.yboxclient.fragment.TransferFragment;

public class CloudTransferActivity extends ActionBarActivity {
	private FragmentTabHost mTabHost;

	private static final String TAB_UPLOAD = "upload";
	private static final String TAB_DOWNLOAD = "download";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.transfer_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.layout_transfer);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		Bundle data = new Bundle();
		data.putInt(TransferFragment.BUNDLE_TYPE, TransferFragment.TYPE_UPLOAD);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_UPLOAD).setIndicator("上传列表"),
				TransferFragment.class, data);
		data.putInt(TransferFragment.BUNDLE_TYPE,
				TransferFragment.TYPE_DOWNLOAD);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_DOWNLOAD).setIndicator("下载列表"),
				TransferFragment.class, data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
