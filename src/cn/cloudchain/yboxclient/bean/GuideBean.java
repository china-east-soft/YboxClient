package cn.cloudchain.yboxclient.bean;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;
import cn.cloudchain.yboxclient.utils.CalendarUtil;

public class GuideBean implements Comparable<GuideBean>, Parcelable {
	private String guideStartTime;
	private String guideEndTime;
	private String guideName;

	public String getGuideStartTime() {
		return guideStartTime;
	}

	public void setGuideStartTime(String guideStartTime) {
		this.guideStartTime = guideStartTime;
	}

	public String getGuideName() {
		return guideName;
	}

	public void setGuideName(String guideName) {
		this.guideName = guideName;
	}

	public String getGuideEndTime() {
		return guideEndTime;
	}

	public void setGuideEndTime(String guideEndTime) {
		this.guideEndTime = guideEndTime;
	}

	@Override
	public int compareTo(GuideBean another) {
		Calendar thisStartTime = CalendarUtil
				.transString2Calendar(this.guideStartTime);
		Calendar anotherStartTime = CalendarUtil.transString2Calendar(another
				.getGuideStartTime());
		return thisStartTime.compareTo(anotherStartTime);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(guideName);
		dest.writeString(guideStartTime);
		dest.writeString(guideEndTime);
	}

	public static final Parcelable.Creator<GuideBean> CREATOR = new Parcelable.Creator<GuideBean>() {

		@Override
		public GuideBean createFromParcel(Parcel source) {
			return new GuideBean(source);
		}

		@Override
		public GuideBean[] newArray(int size) {
			return new GuideBean[size];
		}
	};

	public GuideBean() {
	}

	private GuideBean(Parcel source) {
		this.guideName = source.readString();
		this.guideStartTime = source.readString();
		this.guideEndTime = source.readString();
	}

}
