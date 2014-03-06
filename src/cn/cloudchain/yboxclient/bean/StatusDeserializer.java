package cn.cloudchain.yboxclient.bean;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class StatusDeserializer implements JsonDeserializer<StatusBean> {

	@Override
	public StatusBean deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		JsonObject obj = arg0.getAsJsonObject();
		boolean result = false;
		if (obj.has("result")) {
			result = obj.get("result").getAsBoolean();
		}
		if (!result) {
			return null;
		}
		JsonObject status = null;
		if (obj.has("status")) {
			status = obj.getAsJsonObject("status");
		}
		if (status == null) {
			return null;
		}
		StatusBean bean = new StatusBean();
		if (status.has("lock")) {
			bean.setLock(status.get("lock").getAsBoolean());
		}
		if (status.has("limit")) {
			bean.setLimit(status.get("limit").getAsBoolean());
		}
		if (status.has("apply")) {
			bean.setApply(status.get("apply").getAsInt());
		}
		if (status.has("freq")) {
			bean.setFreq(status.get("freq").getAsString());
		}
		if (status.has("mode")) {
			bean.setMode(status.get("mode").getAsString());
		}
		if (status.has("epg_create_time")) {
			bean.setEpgUpdateTime(status.get("epg_create_time").getAsString());
		}
		if (status.has("channels")) {
			bean.setChannels(status.get("channels").getAsString());
		}
		return bean;
	}

}
