package cn.cloudchain.yboxclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import cn.cloudchain.yboxclient.face.IMenuItemListener;
import cn.cloudchain.yboxclient.fragment.FileManagerFragment;
import cn.cloudchain.yboxcommon.bean.Types;

public class FileManagerActivity extends ActionBarActivity implements
		IMenuItemListener {
	private FragmentTabHost mTabHost;
	private static final String TAB_VIDEO = "video";
	private static final String TAB_AUDIO = "audio";

	private ActionType currentType = ActionType.NONE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.tab_storage);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_transfer);
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		Bundle videoData = new Bundle();
		videoData.putInt(FileManagerFragment.BUNDLE_TYPE, Types.FILE_ONLY_VIDEO);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_VIDEO).setIndicator("视频"),
				FileManagerFragment.class, videoData);
		Bundle audioData = new Bundle();
		audioData.putInt(FileManagerFragment.BUNDLE_TYPE, Types.FILE_ONLY_AUDIO);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_AUDIO).setIndicator("音频"),
				FileManagerFragment.class, audioData);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		switch (currentType) {
		case NONE:
			getMenuInflater().inflate(R.menu.files, menu);
			menu.findItem(R.id.edit).setVisible(false);
			break;
		default:
			getMenuInflater().inflate(R.menu.files_edit, menu);
			break;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.delete:
			changeActionMode(ActionType.DELETE);
			break;
		case R.id.accept:
			onMenuItemClick(ActionType.ACCEPT);
			break;
		case R.id.cancel:
			changeActionMode(ActionType.NONE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private IMenuItemListener getCurrentListener() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.realtabcontent);
		IMenuItemListener listener = null;
		if (fragment != null && fragment instanceof IMenuItemListener) {
			listener = (IMenuItemListener) fragment;
		}
		return listener;
	}

	@Override
	public void onMenuItemClick(ActionType type) {
		IMenuItemListener listener = getCurrentListener();
		if (listener != null) {
			listener.onMenuItemClick(type);
		}
	}

	@Override
	public void changeActionMode(ActionType type) {
		if (currentType != type) {
			currentType = type;
			supportInvalidateOptionsMenu();
			IMenuItemListener listener = getCurrentListener();
			if (listener != null) {
				listener.changeActionMode(type);
			}
		}
	}
}
