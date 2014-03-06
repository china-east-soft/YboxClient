package cn.cloudchain.yboxclient.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusBean implements Parcelable {
	private boolean lock;
	private boolean limit;
	private int apply;
	private String freq;
	private String channels;
	private String mode;
	private String epgUpdateTime;

	public boolean isLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

	public boolean isLimit() {
		return limit;
	}

	public void setLimit(boolean limit) {
		this.limit = limit;
	}

	public int getApply() {
		return apply;
	}

	public void setApply(int apply) {
		this.apply = apply;
	}

	public String getFreq() {
		return freq;
	}

	public void setFreq(String freq) {
		this.freq = freq;
	}

	public String getChannels() {
		return channels;
	}

	public void setChannels(String channels) {
		this.channels = channels;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getEpgUpdateTime() {
		return epgUpdateTime;
	}

	public void setEpgUpdateTime(String epgUpdateTime) {
		this.epgUpdateTime = epgUpdateTime;
	}

	public static final Parcelable.Creator<StatusBean> CREATOR = new Parcelable.Creator<StatusBean>() {

		@Override
		public StatusBean createFromParcel(Parcel source) {
			return new StatusBean(source);
		}

		@Override
		public StatusBean[] newArray(int size) {
			return new StatusBean[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.lock ? 1 : 0);
		dest.writeInt(this.limit ? 1 : 0);
		dest.writeInt(this.apply);
		dest.writeString(this.freq);
		dest.writeString(this.channels);
		dest.writeString(this.mode);
		dest.writeString(this.epgUpdateTime);
	}

	private StatusBean(Parcel source) {
		this.lock = source.readInt() > 0 ? true : false;
		this.limit = source.readInt() > 0 ? true : false;
		this.apply = source.readInt();
		this.freq = source.readString();
		this.channels = source.readString();
		this.mode = source.readString();
		this.epgUpdateTime = source.readString();
	}

	/**
	 * 字符串初始值均为""
	 */
	public StatusBean() {
		this.freq = "";
		this.channels = "";
		this.mode = "";
		this.epgUpdateTime = "";
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o != null && o instanceof StatusBean) {
			StatusBean bean = (StatusBean) o;
			boolean limitEqual = this.limit == bean.isLimit();
			boolean switchEqual = (this.apply > 0 && bean.getApply() > 0)
					|| (this.apply == 0 && bean.getApply() == 0);
			boolean freqEqual = this.freq.equals(bean.getFreq());
			boolean channelEqual = this.channels.equals(bean.getChannels());
			result = limitEqual && switchEqual && freqEqual && channelEqual;
		}
		return result;
	}

}
