package cn.cloudchain.yboxclient.bean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.cloudchain.yboxclient.utils.PreferenceUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class FrequencyDeserializer implements
		JsonDeserializer<List<FrequencyBean>> {

	@Override
	public List<FrequencyBean> deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		JsonObject obj = arg0.getAsJsonObject();
		boolean result = false;
		if (obj.has("result")) {
			result = obj.get("result").getAsBoolean();
		}
		if (!result) {
			return null;
		}

		if (obj.has("epg_create_time")) {
			PreferenceUtil.putString(PreferenceUtil.LOCAL_EPG_CREATE_TIME,
					obj.get("epg_create_time").getAsString());
		}

		JsonArray epgs = obj.getAsJsonArray("epg");
		int size = epgs == null ? 0 : epgs.size();
		List<FrequencyBean> freqArray = new ArrayList<FrequencyBean>(size);
		for (int i = 0; i < size; ++i) {
			JsonObject freqObj = epgs.get(i).getAsJsonObject();
			FrequencyBean freqBean = new FrequencyBean();
			if (freqObj.has("freq")) {
				freqBean.setFreqNum(freqObj.get("freq").getAsString());
			}
			if (!freqObj.has("programs")) {
				continue;
			}

			JsonArray programs = freqObj.getAsJsonArray("programs");
			List<ProgramBean> programArray = getProgramList(programs);
			freqBean.setProgramList(programArray);
			freqArray.add(freqBean);
		}
		return freqArray;
	}

	private GuideBean getGuideBean(JsonObject obj) {
		if (obj == null)
			return null;

		GuideBean bean = new GuideBean();
		if (obj.has("start")) {
			bean.setGuideStartTime(obj.get("start").getAsString());
		}
		if (obj.has("end")) {
			bean.setGuideEndTime(obj.get("end").getAsString());
		}
		if (obj.has("name")) {
			bean.setGuideName(obj.get("name").getAsString());
		}
		return bean;
	}

	private List<GuideBean> getGuideList(JsonArray array) {
		if (array == null)
			return null;
		int size = array.size();
		List<GuideBean> guideList = new ArrayList<GuideBean>(size);
		for (int i = 0; i < size; ++i) {
			JsonObject obj = array.get(i).getAsJsonObject();
			GuideBean bean = getGuideBean(obj);
			if (bean != null)
				guideList.add(bean);
		}
		return guideList;
	}

	private ProgramBean getProgramBean(JsonObject obj) {
		if (obj == null)
			return null;
		ProgramBean programBean = new ProgramBean();
		if (obj.has("name")) {
			programBean.setName(obj.get("name").getAsString());
		}
		if (obj.has("sid")) {
			programBean.setServiceId(obj.get("sid").getAsString());
		}
		if (obj.has("guides")) {
			JsonArray array = obj.getAsJsonArray("guides");
			List<GuideBean> guideList = getGuideList(array);
			programBean.setGuideList(guideList);
		}
		return programBean;
	}

	private List<ProgramBean> getProgramList(JsonArray array) {
		if (array == null)
			return null;
		int size = array.size();
		List<ProgramBean> programList = new ArrayList<ProgramBean>(size);
		for (int i = 0; i < size; ++i) {
			JsonObject obj = array.get(i).getAsJsonObject();
			ProgramBean bean = getProgramBean(obj);
			if (bean != null)
				programList.add(bean);
		}
		return programList;
	}

}
