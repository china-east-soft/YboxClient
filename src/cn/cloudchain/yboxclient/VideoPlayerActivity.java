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
	private boolean isLive;
	private boolean isFromMain;

	/**
	 * 启动电视直播
	 * 
	 * @param context
	 * @param fromMain
	 *            true时从 {@link MainGridActivity}中跳转过来，需做特殊处理
	 * @param url
	 *            直播链接地址
	 * @param bean
	 *            直播对象
	 */
	public static void start(Context context, boolean fromMain, String url,
			ProgramBean bean) {
		Intent intent = new Intent(context, VideoPlayerActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("program", bean);
		intent.putExtra("fromMain", fromMain);
		intent.putExtra("isLive", true);
		context.startActivity(intent);
	}

	/**
	 * 启动普通音/视频
	 * 
	 * @param context
	 * @param url
	 *            音/视频链接地址
	 */
	public static void start(Context context, String url) {
		Intent intent = new Intent(context, VideoPlayerActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("isLive", false);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_container);
		url = getIntent().getStringExtra("url");
		program = getIntent().getParcelableExtra("program");
		isLive = getIntent().getBooleanExtra("isLive", false);
		isFromMain = getIntent().getBooleanExtra("fromMain", false);
	}

	@Override
	protected void onStart() {
		super.onStart();
		FragmentManager fm = getSupportFragmentManager();
		// 防止屏幕旋转时创建多了相同的碎片，致使之后的replace操作无效或误操作
		FragmentTransaction ft = fm.beginTransaction();
		VideoPlayFragment fragment = VideoPlayFragment.newInstance(isLive, url,
				program);
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (isFromMain) {
			Intent intent = new Intent(this, MainGridActivity.class);
			startActivity(intent);
			finish();
		}
	}
}
