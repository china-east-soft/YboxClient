package cn.cloudchain.yboxclient;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import cn.cloudchain.yboxclient.helper.WeakHandler;

public class TrafficDetailActivity extends ActionBarActivity {
	final String TAG = TrafficDetailActivity.class.getSimpleName();

	public static final String BUNDLE_TODAY_USED = "today_used";
	public static final String BUNDLE_MONTH_USED = "month_used";
	public static final String BUNDLE_MONTH_REMAIN = "month_remain";
	public static final String BUNDLE_LIMIT = "limit";
	public static final String BUNDLE_WARNINT = "warning";

	private TextView todayUsedView;
	private TextView monthUsedView;
	private TextView monthRemainView;
	private TextView limitView;
	private TextView warnView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.traffic_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.layout_traffic);

		todayUsedView = (TextView) this.findViewById(R.id.today_used);
		monthRemainView = (TextView) this.findViewById(R.id.month_remain);
		todayUsedView = (TextView) this.findViewById(R.id.month_used);
		limitView = (TextView) this.findViewById(R.id.limit);
		warnView = (TextView) this.findViewById(R.id.warn);

		initView(getIntent().getExtras());
	}

	private void initView(Bundle bundle) {
		String today = "";
		String month = "";
		String limit = "";
		String warn = "";
		if (bundle != null) {
			today = bundle.getString(BUNDLE_TODAY_USED);
			month = bundle.getString(BUNDLE_MONTH_USED);
			limit = bundle.getString(BUNDLE_LIMIT);
			warn = bundle.getString(BUNDLE_WARNINT);
		}
		if (TextUtils.isEmpty(today)) {
			today = "0MB";
		}
		if (TextUtils.isEmpty(month)) {
			month = "0MB";
		}
		if (TextUtils.isEmpty(limit)) {
			limit = "无限制";
		}
		if (TextUtils.isEmpty(warn)) {
			warn = "无限制";
		}

		todayUsedView.setText(getString(R.string.traffic_today_used, today));
		monthUsedView.setText(getString(R.string.traffic_month_used, month));
		limitView.setText(getString(R.string.traffic_limit, limit));
		warnView.setText(getString(R.string.traffic_warnint, warn));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private static class MyHandler extends WeakHandler<TrafficDetailActivity> {
		private static final int TRAFFIC_GET = 0;
		private static final int TRAFFIC_COMPLETE = 1;

		public MyHandler(TrafficDetailActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null)
				return;

			switch (msg.what) {
			case TRAFFIC_GET:

				break;
			case TRAFFIC_COMPLETE:

				break;
			}
		}
	}
}
