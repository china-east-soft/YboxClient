package cn.cloudchain.yboxclient.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.cloudchain.yboxclient.R;
import cn.cloudchain.yboxclient.bean.FileBean;

public class CloudFileListAdapter extends BaseAdapter {
	public static final int TYPE_DIR = 0;
	public static final int TYPE_FILE = 1;
	private Context context;
	private List<FileBean> fileList;
	private boolean showCheckbox = false;

	public CloudFileListAdapter(Context context) {
		this.context = context;
	}

	public void setFileList(List<FileBean> fileList) {
		this.fileList = fileList;
	}

	public void showCheckBox(boolean show) {
		this.showCheckbox = show;
	}

	/**
	 * 获取所有的文件个数，出去目录文件
	 */
	public int getTotalFileNum() {
		if (fileList == null || fileList.size() == 0) {
			return 0;
		}
		int num = 0;
		for (FileBean bean : fileList) {
			if (bean != null && !bean.isDirectory) {
				++num;
			}
		}
		return num;
	}

	@Override
	public int getCount() {
		return fileList == null ? 0 : fileList.size();
	}

	@Override
	public FileBean getItem(int position) {
		FileBean file = null;
		if (position < getCount()) {
			file = fileList.get(position);
		}
		return file;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		int type = TYPE_DIR;
		FileBean bean = getItem(position);
		if (bean != null && !bean.isDirectory) {
			type = TYPE_FILE;
		}
		return type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		if (view != null) {
			holder = (ViewHolder) view.getTag(R.id.tag_file_item);
		}
		if (holder == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_item_file, null);
			holder = new ViewHolder();
			holder.fileImageView = (ImageView) view
					.findViewById(R.id.fileimage);
			holder.fileNameTextView = (TextView) view
					.findViewById(R.id.filename);
			holder.fileDetailTextView = (TextView) view
					.findViewById(R.id.filedetail);
			holder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			view.setTag(R.id.tag_file_item, holder);
		}

		FileBean file = getItem(position);
		if (file == null)
			return view;

		holder.fileNameTextView.setText(file.name);
		StringBuilder detailBuilder = new StringBuilder();
		if (!file.isDirectory) {
			detailBuilder.append(Formatter.formatShortFileSize(context,
					file.size));
			detailBuilder.append("，");
			detailBuilder.append(getTimeByMillis(file.lastModified));
			holder.checkBox.setVisibility(showCheckbox ? View.VISIBLE
					: View.GONE);
		} else {
			holder.checkBox.setVisibility(View.GONE);
		}
		holder.fileDetailTextView.setText(detailBuilder);
		return view;
	}

	class ViewHolder {
		ImageView fileImageView;
		TextView fileNameTextView;
		TextView fileDetailTextView;
		CheckBox checkBox;
	}

	@SuppressLint("SimpleDateFormat")
	private String getTimeByMillis(long millis) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(millis);
		Date tasktime = ca.getTime();
		// 设置日期输出的格式

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(tasktime);
	}
}
