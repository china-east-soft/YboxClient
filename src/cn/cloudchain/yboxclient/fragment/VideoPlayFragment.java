package cn.cloudchain.yboxclient.fragment;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.bean.ProgramBean;
import cn.cloudchain.yboxclient.utils.LogUtil;

public class VideoPlayFragment extends Fragment {
	public static final String TAG = VideoPlayFragment.class.getSimpleName();
	private VideoView mVideoView;
	private boolean isLive;
	private String mPlayUrl;
	private ProgramBean mProgramBean;

	private boolean isFragmentStopped;

	public static VideoPlayFragment newInstance(boolean isLive, String url,
			ProgramBean bean) {
		VideoPlayFragment fragment = new VideoPlayFragment();
		Bundle args = new Bundle();
		args.putString("url", url);
		args.putParcelable("program", bean);
		args.putBoolean("isLive", isLive);
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
			isLive = data.getBoolean("isLive");
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

		initPlayer();

		if (!TextUtils.isEmpty(mPlayUrl)) {
			LogUtil.i(TAG, "mPlayUrl = " + mPlayUrl);
			mVideoView.setVideoPath(mPlayUrl);
		}
	}

	private void initPlayer() {
		mVideoView.setMediaController(isLive ? new MediaController(
				getActivity()) : new MediaController(getActivity(), null));
		mVideoView.setBufferSize(0);
		mVideoView.requestFocus();
		mVideoView
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer arg0) {
						if (isLive && !isFragmentStopped
								&& !TextUtils.isEmpty(mPlayUrl)) {
							mVideoView.setVideoPath(mPlayUrl);
						}
					}
				});
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				mediaPlayer.setPlaybackSpeed(1.0f);
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
	public void onStop() {
		LogUtil.i(TAG, "onStop");
		isFragmentStopped = true;
		mVideoView.stopPlayback();
		super.onStop();
	}

}
