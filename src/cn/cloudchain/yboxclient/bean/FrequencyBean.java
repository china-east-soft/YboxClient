package cn.cloudchain.yboxclient.bean;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class FrequencyBean implements Parcelable {
	private String freqNum;
	private List<ProgramBean> programList;

	public String getFreqNum() {
		return freqNum;
	}

	public void setFreqNum(String freqNum) {
		this.freqNum = freqNum;
	}

	public List<ProgramBean> getProgramList() {
		return programList;
	}

	public void setProgramList(List<ProgramBean> mProgramList) {
		this.programList = mProgramList;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(freqNum);
		dest.writeTypedList(programList);
	}

	public static final Parcelable.Creator<FrequencyBean> CREATOR = new Parcelable.Creator<FrequencyBean>() {

		@Override
		public FrequencyBean createFromParcel(Parcel source) {
			return new FrequencyBean(source);
		}

		@Override
		public FrequencyBean[] newArray(int size) {
			return new FrequencyBean[size];
		}
	};

	private FrequencyBean(Parcel source) {
		this.freqNum = source.readString();
		programList = new ArrayList<ProgramBean>();
		source.readTypedList(programList, ProgramBean.CREATOR);
	}

	public FrequencyBean() {
	}

}
