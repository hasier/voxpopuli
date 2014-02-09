package es.deusto.open311bilbao.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public final class JSONUtils {

	private JSONUtils() {
	}

	public static JSONObject getJSONError(int code, String message)
			throws JSONException {
		JSONObject j = new JSONObject();
		j.put("code", code);
		j.put("description", message);
		return j;
	}

	public static void sendJSON(String json, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");
		resp.setHeader("Content-Type", "application/json");
		resp.getWriter().println(json);
	}

}
