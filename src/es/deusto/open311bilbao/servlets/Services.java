package es.deusto.open311bilbao.servlets;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import es.deusto.open311bilbao.PMF;
import es.deusto.open311bilbao.Service;
import es.deusto.open311bilbao.utils.Constants;
import es.deusto.open311bilbao.utils.JSONUtils;
import es.deusto.open311bilbao.utils.Utils;
import es.deusto.open311bilbao.utils.Utils.Format;
import es.deusto.open311bilbao.utils.XMLUtils;

public class Services extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setCharacterEncoding("UTF-8");
		String path = req.getRequestURI();
		if (!path.startsWith("/services")) { // Invalid URL error
			Logger.getGlobal().info("Invalid URL " + path);
			int error = 400;
			String errorStr = "The URL path " + path + " is not valid";
			Utils.sendError(req, resp, path, error, errorStr, null);
			return;
		} // End URL error

		boolean single = true;
		path = req.getPathInfo();
		if (path == null) {
			path = req.getServletPath();
			single = false;
		}

		String[] split = path.split("\\.");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			if (split.length > 2) { // If split larger than 2, invalid arguments
				Logger.getGlobal().info("URL arguments larger than 2: " + path);
				int error = 400;
				String errorStr = "The URL path " + path + " is not valid";
				Utils.sendError(req, resp, path, error, errorStr, null);
			} else if (split.length == 2 && !split[1].equals("json")
					&& !split[1].equals("xml")) {
				// If format specified and different from json and xml, error

				Logger.getGlobal().info("Invalid format " + split[1]);
				Utils.sendError(req, resp, path, 400, "Format " + split[1]
						+ " is not a valid format", null);
			} else if (single) { // If one service queried
				Service service = null;
				try {
					service = pm.getObjectById(Service.class, KeyFactory
							.createKey(Service.class.getSimpleName(),
									Long.parseLong(split[0].substring(1))));
				} catch (Exception ex) {
					Logger.getGlobal().log(Level.SEVERE, ex.getMessage(), ex);
					// ex.printStackTrace();
				}
				if (service == null || service.getDefinition() == null) {
					// If service not found, error

					Logger.getGlobal().info("Service not found");
					int code = 404;
					String message = "Service definition with code "
							+ split[0].substring(1) + " was not found";
					if (split.length == 1) { // HTML
						// resp.getWriter().println(
						// "This is HTML ERROR for a service");
						Utils.sendError(req, resp, path, code, message,
								Format.HTML);
					} else if (split[1].equals("xml")) { // XML
						Utils.sendError(req, resp, path, code, message,
								Format.XML);
					} else { // JSON
						Utils.sendError(req, resp, path, code, message,
								Format.JSON);
					}
				} else if (split.length == 1) { // If no format specified, HTML
					// resp.getWriter().println("This is HTML for a service");
					List<Service> services = getAllServices(pm);
					req.setAttribute(Constants.PAGE_KEY, Constants.SERVICES_KEY);
					req.setAttribute(Constants.SERVICES_KEY, services);
					req.setAttribute(Constants.SERVICE_KEY, service);
					req.getRequestDispatcher("/WEB-INF/services.jsp").forward(
							req, resp);
				} else {
					if (split[1].equals("xml")) { // If XML queried
						try {
							Document document = DocumentBuilderFactory
									.newInstance().newDocumentBuilder()
									.newDocument();
							document.appendChild(service.getDefinition()
									.serializeToXML(document));
							XMLUtils.sendXML(document, resp);
						} catch (ParserConfigurationException e) {
							// TODO Error ??
							e.printStackTrace();
						}
					} else { // If JSON queried
						try {
							JSONObject json = service.getDefinition()
									.serializeToJSON();
							JSONUtils.sendJSON(json.toString(), resp);
						} catch (JSONException e) {
							// TODO Error ??
							e.printStackTrace();
						}
					}
				}
				// pm.close();
			} else { // If all services queried
				// PersistenceManager pm = PMF.get().getPersistenceManager();

				List<Service> services = getAllServices(pm);
				if (split.length == 1) { // If no format specified, HTML
					// resp.getWriter().println("This is HTML for services");
					req.setAttribute(Constants.PAGE_KEY, Constants.SERVICES_KEY);
					req.setAttribute(Constants.SERVICES_KEY, services);
					req.getRequestDispatcher("/WEB-INF/services.jsp").forward(
							req, resp);
				} else {
					if (split[1].equals("xml")) { // If XML queried
						try {
							Document document = DocumentBuilderFactory
									.newInstance().newDocumentBuilder()
									.newDocument();
							Element root = document.createElement("services");
							document.appendChild(root);
							for (Service s : services) {
								root.appendChild(s.serializeToXML(document));
							}
							XMLUtils.sendXML(document, resp);
						} catch (ParserConfigurationException e) {
							// TODO Error ??
							e.printStackTrace();
						}
					} else { // If JSON queried
						JSONArray json = new JSONArray();
						for (Service s : services) {
							try {
								json.put(s.serializeToJSON());
							} catch (JSONException e) {
								// TODO Error ??
								e.printStackTrace();
							}
						}
						JSONUtils.sendJSON(json.toString(), resp);
					}
				}
			}
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	private List<Service> getAllServices(PersistenceManager pm) {
		String query = "select from " + Service.class.getName();
		return (List<Service>) pm.newQuery(query).execute();
	}
}