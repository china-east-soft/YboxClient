package cn.cloudchain.yboxclient.bean;

import java.io.File;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.baidu.frontia.api.FrontiaPersonalStorageListener.FileInfoResult;

/**
 * 方便文件管理的类
 * 
 * @author lazzy
 * 
 */
public class FileBean implements Parcelable {
	public String name;
	public String absolutePath;
	public long lastModified;
	public long size;
	public boolean isDirectory;
	public int childrenNum;

	public static final Parcelable.Creator<FileBean> CREATOR = new Parcelable.Creator<FileBean>() {

		@Override
		public FileBean createFromParcel(Parcel source) {
			FileBean bean = new FileBean();
			bean.name = source.readString();
			bean.absolutePath = source.readString();
			bean.lastModified = source.readLong();
			bean.size = source.readLong();
			bean.isDirectory = source.readInt() > 0 ? true : false;
			bean.childrenNum = source.readInt();
			return bean;
		}

		@Override
		public FileBean[] newArray(int size) {
			return new FileBean[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(absolutePath);
		dest.writeLong(lastModified);
		dest.writeLong(size);
		dest.writeInt(isDirectory ? 1 : 0);
		dest.writeInt(childrenNum);
	}

	public FileBean convertFrom(FileInfoResult info) {
		if (info == null)
			return null;
		this.lastModified = info.getModifyTime();
		this.isDirectory = info.isDir();
		this.absolutePath = info.getPath();
		this.size = info.getSize();
		this.name = getFileNameFromUrl(info.getPath());
		return this;
	}

	public FileBean convertFrom(File file) {
		if (file == null)
			return null;
		this.name = file.getName();
		this.lastModified = file.lastModified();
		this.isDirectory = file.isDirectory();
		this.absolutePath = file.getAbsolutePath();
		this.size = file.length();
		return this;
	}

	private String getFileNameFromUrl(String path) {
		if (TextUtils.isEmpty(path))
			return "";

		String filename = path.substring(path.lastIndexOf('/') + 1);
		return filename;
	}

}
