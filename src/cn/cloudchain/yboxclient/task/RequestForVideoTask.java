package cn.cloudchain.yboxclient.task;

import android.content.Context;
import android.text.TextUtils;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.VideoPlayerActivity;
import cn.cloudchain.yboxclient.bean.ProgramBean;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.helper.ApHelper;
import cn.cloudchain.yboxclient.utils.Util;

public class RequestForVideoTask extends BaseFragmentTask {
	private static final int RESULT_SUCCESS = 0;
	private static final int RESULT_FAIL = 1;

	private Context context;
	private ProgramBean program;
	private String url;

	public RequestForVideoTask(Context context, ProgramBean program) {
		this.program = program;
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		super.doInBackground(params);
		int result = RESULT_FAIL;
		if (program == null) {
			return result;
		}
		try {
			url = ApHelper.getInstance().getPlayUrl(program.getFreqValue(),
					program.getServiceId());
		} catch (YunmaoException e) {
			e.printStackTrace();
		}
		if (!TextUtils.isEmpty(url)) {
			result = RESULT_SUCCESS;
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (isCancelled()) {
			return;
		}
		switch (result) {
		case RESULT_SUCCESS:
			VideoPlayerActivity.start(context, false, url, program);
			break;
		case RESULT_FAIL:
			Util.toaster(R.string.request_video_url_fail);
			break;
		}
	}
}