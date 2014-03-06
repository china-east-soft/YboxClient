package cn.cloudchain.yboxclient.fragment;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.R.id;
import cn.cloudchain.yboxclient.R.layout;
import cn.cloudchain.yboxclient.bean.ProgramBean;
import cn.cloudchain.yboxclient.utils.LogUtil;

public class VideoPlayFragment extends Fragment {
	static final String TAG = VideoPlayFragment.class.getSimpleName();
	private VideoView mVideoView;
	private String mPlayUrl;
	private ProgramBean mProgramBean;

	private boolean isFragmentStopped;

	public static VideoPlayFragment newInstance(String url, ProgramBean bean) {
		VideoPlayFragment fragment = new VideoPlayFragment();
		Bundle args = new Bundle();
		args.putString("url", url);
		args.putParcelable("program", bean);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.i(TAG, "onCreate");
		Bundle data = getArguments();
		if (data != null) {
			mPlayUrl = data.getString("url");
			mProgramBean = data.getParcelable("program");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogUtil.i(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.layout_player, container, false);
		mVideoView = (VideoView) view.findViewById(R.id.surface_view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LogUtil.i(TAG, "onActivityCreated");
		if (!LibsChecker.checkVitamioLibs(getActivity()))
			return;
		if (!TextUtils.isEmpty(mPlayUrl)) {
			initPlayer();
			LogUtil.i(TAG, "mPlayUrl = " + mPlayUrl);
			mVideoView.setVideoPath(mPlayUrl);
		}
	}

	private void initPlayer() {
		mVideoView.setMediaPlayer(new MediaPlayer(getActivity()));
		mVideoView.setBufferSize(0);
		mVideoView
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer arg0) {
						if (!isFragmentStopped) {
							mVideoView.setVideoPath(mPlayUrl);
						}
					}
				});
		mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				LogUtil.i(TAG, "error-->" + arg1 + "---" + arg2);
				return true;
			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
		LogUtil.i(TAG, "onStart");
		isFragmentStopped = false;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		LogUtil.i(TAG, "onStop");
		isFragmentStopped = true;
		mVideoView.stop();
		mVideoView.release();
		super.onStop();
	}

}
