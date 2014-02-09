package es.deusto.open311bilbao.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import es.deusto.open311bilbao.User;

public final class Utils {

	public static final SimpleDateFormat W3C_DATE_FORMAT;
	public static final Map<String, String> openIdProviders;

	static {
		W3C_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		W3C_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("Google", "https://www.google.com/accounts/o8/id");
		map.put("Yahoo", "https://me.yahoo.com");
		// openIdProviders.put("Windows Live", "");
		map.put("AOL", "https://www.aol.com/");
		// map.put("MyOpenId", "https://www.myopenid.com/");
		// map.put("Facebook", "http://facebook-openid.appspot.com/");
		openIdProviders = Collections.unmodifiableMap(map);
	}

	private Utils() {
	}

	public static void sendError(HttpServletRequest req,
			HttpServletResponse resp, String path, int error, String errorStr,
			Format format) throws IOException, ServletException {
		if (format == null) {
			if (path.endsWith(".xml")) {
				format = Format.XML;
			} else if (path.endsWith(".json")) {
				format = Format.JSON;
			}
		} else {
			switch (format) {
			case XML:
				sendXMLError(req, resp, path, error, errorStr);
				break;
			case JSON:
				sendJSONError(req, resp, path, error, errorStr);
				break;
			default:
				sendHTMLError(req, resp, path, error, errorStr);
				break;
			}
		}
	}

	private static void sendXMLError(HttpServletRequest req,
			HttpServletResponse resp, String path, int error, String errorStr)
			throws IOException {
		try {
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
			Element root = document.createElement("errors");
			root.appendChild(XMLUtils.getXMLError(document, error, errorStr));
			document.appendChild(root);

			XMLUtils.sendXML(document, resp);
		} catch (ParserConfigurationException e) {
			// TODO Error ??
			e.printStackTrace();
		}
	}

	private static void sendJSONError(HttpServletRequest req,
			HttpServletResponse resp, String path, int error, String errorStr)
			throws IOException {
		try {
			JSONArray json = new JSONArray();
			json.put(JSONUtils.getJSONError(error, errorStr));
			JSONUtils.sendJSON(json.toString(), resp);
		} catch (JSONException e) {
			// TODO Error ??
			e.printStackTrace();
		}
	}

	private static void sendHTMLError(HttpServletRequest req,
			HttpServletResponse resp, String path, int error, String errorStr)
			throws ServletException, IOException {
		req.setAttribute(Constants.ERROR_KEY, error);
		req.setAttribute(Constants.ERROR_STRING, errorStr);
		req.getRequestDispatcher("/WEB-INF/error.jsp").forward(req, resp);
	}

	public static enum Format {
		XML, JSON, HTML
	}

	public static User getOrCreateUser(
			com.google.appengine.api.users.User user, PersistenceManager pm) {
		if (user == null) {
			return null;
		}
		User dataUser = null;
		try {
			dataUser = pm.getObjectById(
					User.class,
					KeyFactory.createKey(User.class.getSimpleName(),
							user.getEmail()));
		} catch (Exception e) {
			dataUser = new User(user);
			pm.makePersistent(dataUser);
		}
		return dataUser;
	}

	public static void pushUpdates(List<User> users, String message, String url) {
		ChannelService channelService = ChannelServiceFactory
				.getChannelService();
		try {
			JSONObject json = new JSONObject();
			json.put("message", message);
			json.put("link", url);
			for (User user : users) {
				channelService.sendMessage(new ChannelMessage(KeyFactory
						.keyToString(user.getKey()), json.toString()));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static String getTimeZoneOffset(HttpServletRequest request) {
		Locale clientLocale = request.getLocale();
		Calendar calendar = Calendar.getInstance(clientLocale);
		TimeZone tz = calendar.getTimeZone();
		int h = tz.getRawOffset() / (60 * 60 * 1000);
		int m = (tz.getRawOffset() / (60 * 1000)) % 60;
		StringBuilder sb = new StringBuilder(h < 0 ? "-" : "+");
		if (h < 10) {
			sb.append(0);
		}
		sb.append(h);
		if (m < 10) {
			sb.append(0);
		}
		sb.append(m);
		return sb.toString();
	}

	private static final int R = 6371;

	public static double getDistance(GeoPoint gp1, GeoPoint gp2) {
		if (gp1 == null || gp2 == null) {
			return Double.MAX_VALUE;
		}
		// double dLat = Math.abs(gp2.getLatitude() - gp1.getLatitude());
		// double dLon = Math.abs(gp2.getLongitude() - gp1.getLongitude());
		// return Math.sqrt((dLat * dLat) + (dLon * dLon));
		double dLat = Math.toRadians(gp2.getLatitude() - gp1.getLatitude());
		double dLon = Math.toRadians(gp2.getLongitude() - gp1.getLongitude());
		double lat1 = Math.toRadians(gp1.getLatitude());
		double lat2 = Math.toRadians(gp2.getLatitude());

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
				* Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

}
