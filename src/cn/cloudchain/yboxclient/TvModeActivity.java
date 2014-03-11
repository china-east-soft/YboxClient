package cn.cloudchain.yboxclient;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import cn.cloudchain.yboxclient.adapter.TvModeListAdapter;
import cn.cloudchain.yboxclient.bean.YunmaoException;
import cn.cloudchain.yboxclient.dialog.TaskDialogFragment;
import cn.cloudchain.yboxclient.helper.ApHelper;
import cn.cloudchain.yboxclient.task.BaseFragmentTask;
import cn.cloudchain.yboxclient.utils.Util;

public class TvModeActivity extends ActionBarActivity {
	final String TAG = TvModeActivity.class.getSimpleName();
	private ListView listView;
	private TvModeListAdapter adapter;

	private String currentMode;
	private String[] allModes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setTitle(R.string.tvmodes_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.base_listview);

		currentMode = getIntent().getStringExtra("current");
		allModes = getIntent().getStringArrayExtra("modes");

		listView = (ListView) this.findViewById(R.id.base_listview);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		adapter = new TvModeListAdapter(this);
		adapter.setModes(allModes);

		listView.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (listView.getCheckedItemPosition() < 0
				&& !TextUtils.isEmpty(currentMode) && allModes != null) {
			for (int i = 0; i < allModes.length; ++i) {
				if (allModes[i].equals(currentMode)) {
					listView.setItemChecked(i, true);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.publish:
			publishClick();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tvmodes, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void publishClick() {
		int index = listView.getCheckedItemPosition();
		if (index < 0 || index >= allModes.length) {
			return;
		}
		String newMode = allModes[index];
		if (newMode.equals(currentMode)) {
			return;
		}
		TaskDialogFragment fragment = TaskDialogFragment.newLoadingFragment(
				"正在请求...", false);
		ChangeTvModeTask task = new ChangeTvModeTask(newMode);
		fragment.setTask(task);
		fragment.show(getSupportFragmentManager(), null);
	}

	private class ChangeTvModeTask extends BaseFragmentTask {
		private final static int RESULT_SUCCESS = 0;
		private final static int RESULT_FAIL = 1;
		private final static int RESULT_FAIL_NET = 2;

		private String mode;

		private ChangeTvModeTask(String mode) {
			this.mode = mode;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			int result = RESULT_FAIL;
			try {
				String response = ApHelper.getInstance().changeTVMode(mode);
				JSONObject obj = new JSONObject(response);
				if (obj.optBoolean("result")) {
					result = RESULT_SUCCESS;
				}
			} catch (YunmaoException e) {
				e.printStackTrace();
				result = RESULT_FAIL_NET;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			switch (result) {
			case RESULT_SUCCESS:
				currentMode = mode;
				Util.toaster("模式修改成功！", null);
				break;
			case RESULT_FAIL:
				Util.toaster(R.string.request_fail);
				break;
			case RESULT_FAIL_NET:
				Util.toaster(R.string.request_fail_net);
				break;
			}
		}
	}

}
