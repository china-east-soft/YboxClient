package cn.cloudchain.yboxclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import cn.cloudchain.yboxclient.bean.ProgramBean;
import cn.cloudchain.yboxclient.fragment.VideoPlayFragment;

public class VideoPlayerActivity extends FragmentActivity {
	static final String TAG = VideoPlayerActivity.class.getSimpleName();
	private String url = "";
	private ProgramBean program;

	public static void start(Context context, String url, ProgramBean bean) {
		Intent intent = new Intent(context, VideoPlayerActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("program", bean);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_container);
		url = getIntent().getStringExtra("url");
		program = getIntent().getParcelableExtra("program");
	}

	@Override
	protected void onStart() {
		super.onStart();
		FragmentManager fm = getSupportFragmentManager();
		// 防止屏幕旋转时创建多了相同的碎片，致使之后的replace操作无效或误操作
		FragmentTransaction ft = fm.beginTransaction();
		VideoPlayFragment fragment = VideoPlayFragment
				.newInstance(url, program);
		ft.add(R.id.container, fragment, VideoPlayFragment.TAG);
		ft.commitAllowingStateLoss();
	}

	@Override
	protected void onStop() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.container);
		if (fragment != null && fragment.isAdded()) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.remove(fragment);
			ft.commitAllowingStateLoss();
		}

		super.onStop();
	}
}
