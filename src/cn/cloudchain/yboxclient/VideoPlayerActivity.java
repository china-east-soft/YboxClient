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
import cn.cloudchain.yboxclient.utils.LogUtil;

public class VideoPlayerActivity extends FragmentActivity {
	static final String TAG = VideoPlayerActivity.class.getSimpleName();

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

		String url = getIntent().getStringExtra("url");
		ProgramBean bean = getIntent().getParcelableExtra("program");

		FragmentManager fm = getSupportFragmentManager();
		// 防止屏幕旋转时创建多了相同的碎片，致使之后的replace操作无效或误操作
		FragmentTransaction ft = fm.beginTransaction();
		Fragment exist = fm.findFragmentByTag(VideoPlayFragment.TAG);
		VideoPlayFragment fragment = VideoPlayFragment.newInstance(url, bean);
		if (exist == null || !exist.isAdded()) {
			ft.add(R.id.video_layout, fragment, VideoPlayFragment.TAG);
		} else {
			LogUtil.i(TAG, "replace");
			ft.replace(R.id.video_layout, fragment, VideoPlayFragment.TAG);
		}
		ft.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
