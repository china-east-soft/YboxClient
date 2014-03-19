package cn.cloudchain.yboxclient.bean;

import android.os.Parcel;
import android.os.Parcelable;

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

}
