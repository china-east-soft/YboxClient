package cn.cloudchain.yboxclient.bean;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ProgramBean implements Parcelable {
	private String serviceId;
	private String freqValue;
	private String name;
	private String logo;
	private int currentGuide;
	private int nextGuide;
	private List<GuideBean> guideList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String sid) {
		this.serviceId = sid;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getFreqValue() {
		return freqValue;
	}

	public void setFreqValue(String freqValue) {
		this.freqValue = freqValue;
	}

	public int getCurrentGuideIndex() {
		return currentGuide;
	}

	public void setCurrentGuideIndex(int currentGuide) {
		this.currentGuide = currentGuide;
	}

	public int getNextGuideIndex() {
		return nextGuide;
	}

	public void setNextGuideIndex(int nextGuide) {
		this.nextGuide = nextGuide;
	}

	public List<GuideBean> getGuideList() {
		return guideList;
	}

	public void setGuideList(List<GuideBean> guideList) {
		this.guideList = guideList;
	}

	public ProgramBean() {
	}

	public static final Parcelable.Creator<ProgramBean> CREATOR = new Parcelable.Creator<ProgramBean>() {

		@Override
		public ProgramBean createFromParcel(Parcel source) {
			return new ProgramBean(source);
		}

		@Override
		public ProgramBean[] newArray(int size) {
			return new ProgramBean[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(serviceId);
		dest.writeString(freqValue);
		dest.writeString(name);
		dest.writeString(logo);
		dest.writeInt(currentGuide);
		dest.writeInt(nextGuide);
		dest.writeTypedList(guideList);
	}

	private ProgramBean(Parcel source) {
		this.serviceId = source.readString();
		this.freqValue = source.readString();
		this.name = source.readString();
		this.logo = source.readString();
		this.currentGuide = source.readInt();
		this.nextGuide = source.readInt();
		guideList = new ArrayList<GuideBean>();
		source.readTypedList(guideList, GuideBean.CREATOR);

	}
}
