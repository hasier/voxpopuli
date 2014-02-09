package es.deusto.open311bilbao.servlets;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.com.google.common.base.Joiner;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.appengine.repackaged.org.joda.time.DateTime;

import es.deusto.open311bilbao.PMF;
import es.deusto.open311bilbao.Service;
import es.deusto.open311bilbao.ServiceRequest;
import es.deusto.open311bilbao.Status;
import es.deusto.open311bilbao.User;
import es.deusto.open311bilbao.utils.Constants;
import es.deusto.open311bilbao.utils.JSONUtils;
import es.deusto.open311bilbao.utils.Utils;
import es.deusto.open311bilbao.utils.Utils.Format;
import es.deusto.open311bilbao.utils.XMLUtils;

public class Requests extends HttpServlet {

	private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.email";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setCharacterEncoding("UTF-8");
		String path = req.getRequestURI();
		if (!path.startsWith("/requests")) { // Invalid URL error
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
				ServiceRequest request = null;
				try {
					request = pm.getObjectById(
							ServiceRequest.class,
							KeyFactory.createKey(
									ServiceRequest.class.getSimpleName(),
									Long.parseLong(split[0].substring(1))));
				} catch (Exception ex) {
					Logger.getGlobal().log(Level.SEVERE, ex.getMessage(), ex);
					// ex.printStackTrace();
				}
				if (request == null) {
					// If service not found, error

					Logger.getGlobal().info("Request not found");
					int code = 404;
					String message = "Request with code "
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
					UserService userService = UserServiceFactory
							.getUserService();
					com.google.appengine.api.users.User user = userService
							.getCurrentUser();
					// req.setAttribute(Constants.PAGE_KEY,
					// Constants.REQUESTS_KEY);
					req.setAttribute(Constants.REQUEST_KEY, request);
					req.setAttribute(Constants.CURRENT_USER_KEY,
							Utils.getOrCreateUser(user, pm));
					req.getRequestDispatcher("/WEB-INF/request.jsp").forward(
							req, resp);
				} else {
					if (split[1].equals("xml")) { // If XML queried
						try {
							Document document = DocumentBuilderFactory
									.newInstance().newDocumentBuilder()
									.newDocument();
							Element root = document
									.createElement("service_requests");
							document.appendChild(root);
							root.appendChild(request.serializeToXML(document));
							XMLUtils.sendXML(document, resp);
						} catch (ParserConfigurationException e) {
							// TODO Error ??
							e.printStackTrace();
						}
					} else { // If JSON queried
						JSONArray json = new JSONArray();
						try {
							json.put(request.serializeToJSON());
						} catch (JSONException e) {
							// TODO Error ??
							e.printStackTrace();
						}
						JSONUtils.sendJSON(json.toString(), resp);
					}
				}
				// pm.close();
			} else { // If multiple services queried
				List<ServiceRequest> requests = null;
				if (req.getParameter("service_request_id") != null) {
					requests = new ArrayList<>();
					String[] ids = req.getParameter("service_request_id")
							.split(",");
					for (String id : ids) {
						try {
							requests.add(pm.getObjectById(ServiceRequest.class,
									KeyFactory.createKey(ServiceRequest.class
											.getSimpleName(), Long
											.parseLong(id))));
						} catch (Exception ex) {
							Logger.getGlobal().log(Level.SEVERE,
									ex.getMessage(), ex);
						}
					}
				} else {
					if (req.getParameter("service_code") != null
							|| req.getParameter("start_date") != null
							|| req.getParameter("end_date") != null
							|| req.getParameter("status") != null) {
						requests = getFilteredRequests(pm, req,
								req.getParameter("service_code"),
								req.getParameter("start_date"),
								req.getParameter("end_date"),
								req.getParameter("status"));
					} else {
						requests = getAllRequests(pm);
					}
				}

				// requests = new ArrayList<>(requests);

				order(req.getParameter("order"), req.getParameter("location"),
						requests);

				if (split.length == 1) { // If no format specified, HTML
					req.setAttribute(Constants.PAGE_KEY, Constants.REQUESTS_KEY);
					req.setAttribute(Constants.REQUESTS_KEY, requests);
					req.setAttribute(Constants.SERVICES_KEY, getAllServices(pm));
					req.getRequestDispatcher("/WEB-INF/requests.jsp").forward(
							req, resp);
				} else {
					if (split[1].equals("xml")) { // If XML queried
						try {
							Document document = DocumentBuilderFactory
									.newInstance().newDocumentBuilder()
									.newDocument();
							Element root = document
									.createElement("service_requests");
							document.appendChild(root);
							for (ServiceRequest sr : requests) {
								root.appendChild(sr.serializeToXML(document));
							}
							XMLUtils.sendXML(document, resp);
						} catch (ParserConfigurationException e) {
							// TODO Error ??
							e.printStackTrace();
						}
					} else { // If JSON queried
						JSONArray json = new JSONArray();
						for (ServiceRequest sr : requests) {
							try {
								json.put(sr.serializeToJSON());
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

	@SuppressWarnings("unchecked")
	private List<ServiceRequest> getAllRequests(PersistenceManager pm) {
		String query = "select from " + ServiceRequest.class.getName();
		return (List<ServiceRequest>) pm.newQuery(query).execute();
	}

	@SuppressWarnings("unchecked")
	private List<ServiceRequest> getFilteredRequests(PersistenceManager pm,
			HttpServletRequest req, String service_code, String startDate,
			String endDate, String status) {
		ArrayList<String> filters = new ArrayList<>();
		ArrayList<String> params = new ArrayList<>();
		HashMap<String, Object> variables = new HashMap<>();
		if (service_code != null && !service_code.trim().isEmpty()) {
			try {
				Long id = Long.parseLong(service_code.trim());
				Service s = pm
						.getObjectById(
								Service.class,
								KeyFactory.createKey(
										Service.class.getSimpleName(), id));
				filters.add("this.service == qService");
				params.add("es.deusto.open311bilbao.Service qService");
				variables.put("qService", s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Date sDate = null;
		if (startDate != null && !startDate.trim().isEmpty()) {
			try {
				sDate = Utils.W3C_DATE_FORMAT.parse(startDate.trim());
			} catch (ParseException e) {
				if (startDate.trim().indexOf('.') > -1) {
					try {
						sDate = Utils.W3C_DATE_FORMAT
								.parse(startDate.trim().substring(0,
										startDate.trim().lastIndexOf(':'))
										+ Utils.getTimeZoneOffset(req));
					} catch (ParseException ex) {
						e.printStackTrace();
						ex.printStackTrace();
					}
				}
				if (sDate == null) {
					try {
						sDate = Utils.W3C_DATE_FORMAT.parse(startDate.trim()
								+ Utils.getTimeZoneOffset(req));
					} catch (ParseException ex) {
						e.printStackTrace();
						ex.printStackTrace();
					}
				}
			}
		}
		if (sDate == null) {
			sDate = new DateTime(new Date()).minusDays(90).toDate();
		}
		filters.add("this.requestDate >= startDate");
		params.add("java.util.Date startDate");
		variables.put("startDate", sDate);

		Date eDate = null;
		if (endDate != null && !endDate.trim().isEmpty()) {
			try {
				eDate = Utils.W3C_DATE_FORMAT.parse(endDate.trim());
			} catch (ParseException e) {
				if (endDate.trim().indexOf('.') > -1) {
					try {
						eDate = Utils.W3C_DATE_FORMAT.parse(endDate.trim()
								.substring(0, endDate.trim().lastIndexOf(':'))
								+ Utils.getTimeZoneOffset(req));
					} catch (ParseException ex) {
						e.printStackTrace();
						ex.printStackTrace();
					}
				}
				if (eDate == null) {
					try {
						eDate = Utils.W3C_DATE_FORMAT.parse(endDate.trim()
								+ Utils.getTimeZoneOffset(req));
					} catch (ParseException ex) {
						e.printStackTrace();
						ex.printStackTrace();
					}
				}
			}
		}
		if (eDate == null) {
			DateTime dt1 = new DateTime(sDate);
			dt1.plusDays(90);
			eDate = dt1.toDate();
		}

		long diff = Math.abs(sDate.getTime() - eDate.getTime());
		long diffDays = diff / (24 * 60 * 60 * 1000);
		if (diffDays > 90) {
			DateTime dt1 = new DateTime(sDate);
			dt1.plusDays(90);
			eDate = dt1.toDate();
		}
		filters.add("this.requestDate <= endDate");
		params.add("java.util.Date endDate");
		variables.put("endDate", eDate);

		if (status != null && !status.trim().isEmpty()) {
			try {
				Status s = Status.valueOf(status.trim().toUpperCase());
				filters.add("this.status == qStatus");
				params.add("es.deusto.open311bilbao.Status qStatus");
				variables.put("qStatus", s);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		Query q = pm.newQuery(ServiceRequest.class);
		q.setFilter(Joiner.on(" && ").join(filters));
		q.declareParameters(Joiner.on(", ").join(params));
		List<ServiceRequest> list = (List<ServiceRequest>) q
				.executeWithMap(variables);

		// FIXME Arreglar dates
		return list;
	}

	private void order(String order, String location,
			List<ServiceRequest> requests) {
		if (order != null) {
			if (order.equals("popular")) {
				Collections.sort(requests, new Comparator<ServiceRequest>() {

					@Override
					public int compare(ServiceRequest sr1, ServiceRequest sr2) {
						if (sr1.getVotes() == sr2.getVotes()) {
							return 0;
						} else if (sr1.getVotes() > sr2.getVotes()) {
							return -1;
						} else {
							return 1;
						}
					}
				});
			} else if (order.equals("location")) {
				if (location != null && !location.trim().equals("")) {
					String[] coords = location.split(",");
					final GeoPoint current = new GeoPoint(
							Double.parseDouble(coords[0]),
							Double.parseDouble(coords[1]));
					// for (int i = 0; i < requests.size(); i++) {
					// GeoPoint gp = new GeoPoint(requests.get(i).getLat(),
					// requests.get(i).getLon());
					// if (Utils.getDistance(current, gp) > 0.5) {
					// requests.remove(i);
					// i--;
					// }
					// }
					Collections.sort(requests,
							new Comparator<ServiceRequest>() {

								@Override
								public int compare(ServiceRequest sr1,
										ServiceRequest sr2) {
									GeoPoint gp1 = new GeoPoint(sr1.getLat(),
											sr1.getLon());
									GeoPoint gp2 = new GeoPoint(sr2.getLat(),
											sr2.getLon());
									double d1 = Utils.getDistance(current, gp1);
									double d2 = Utils.getDistance(current, gp2);
									if (d1 == d2) {
										return 0;
									} else if (d1 > d2) {
										return -1;
									} else {
										return 1;
									}
								}
							});
				}
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String path = req.getRequestURI();
		if (!path.startsWith("/requests")) {
			Logger.getGlobal().info("Invalid URL " + path);
			int error = 400;
			String errorStr = "The URL path " + path + " is not valid";
			Utils.sendError(req, resp, path, error, errorStr, null);
			return;
		} // End URL error

		boolean voteHandling = true;
		path = req.getPathInfo();
		if (path == null) {
			path = req.getServletPath();
			voteHandling = false;
		}

		String[] split = path.split("\\.");
		if (split.length > 2) { // If split greater than 2, invalid arguments
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
		} else {
			com.google.appengine.api.users.User user = null;
			// FIXME Enable and check! Change for OAuth validation if JSON or
			// XML
			// if (path.endsWith("xml") || path.endsWith("json")) {
			// OAuthService oauth = OAuthServiceFactory.getOAuthService();
			// try {
			// user = oauth.getCurrentUser(SCOPE);
			// } catch (OAuthRequestException e) {
			// e.printStackTrace();
			// }
			// }
			if (user == null) {
				UserService userService = UserServiceFactory.getUserService();
				user = userService.getCurrentUser();
			}
			if (user == null) {
				// Do not allow POST from not logged in users
				Logger.getGlobal().info("Not logged in to post request");
				int error = 400;
				String errorStr = "You must be logged in to post a request";
				if (path.endsWith("xml") || path.endsWith("json")) {
					errorStr += " and provide a valid OAuth2 token with SCOPE '"
							+ SCOPE + "'";
				}
				Utils.sendError(req, resp, path, error, errorStr, null);
				return;
			}
			if (voteHandling) {
				if (split.length != 2) {
					Logger.getGlobal().info("No format specified");
					Utils.sendError(req, resp, path, 400,
							"A format must be specified (xml or json)", null);
				} else if (!split[0].equals("/upvote")
						&& !split[0].equals("/downvote")) {
					Logger.getGlobal().info("Invalid access to " + path);
					int error = 400;
					String errorStr = "Invalid access to " + path;
					Utils.sendError(req, resp, path, error, errorStr,
							Format.valueOf(split[1].toUpperCase()));
				} else {
					int errorCode = -1;
					String errorMessage = "";
					PersistenceManager pm = PMF.get().getPersistenceManager();
					try {
						ServiceRequest sr = pm.getObjectById(
								ServiceRequest.class, Long.parseLong(req
										.getParameter("service_request_id")));
						User dataUser = Utils.getOrCreateUser(user, pm);
						if (sr.getStatus() == Status.CLOSED) {
							errorCode = 400;
							errorMessage = "The request is closed, votes are not accepted";
						} else if (split[0].equals("/downvote")) {
							if (sr.getDownvoted().contains(dataUser)) {
								errorCode = 400;
								errorMessage = "You already voted down, cannot do it again";
							} else {
								sr.addDownvote(dataUser);
								if (!dataUser.equals(sr.getUser())) {
									ArrayList<User> list = new ArrayList<>();
									list.add(sr.getUser());
									Utils.pushUpdates(
											list,
											"Your request got downvoted. Click here to check status",
											"/requests/" + sr.getId());
								}
							}
						} else {
							if (sr.getUpvoted().contains(dataUser)) {
								errorCode = 400;
								errorMessage = "You already voted up, cannot do it again";
							} else {
								sr.addUpvote(dataUser);
								if (!dataUser.equals(sr.getUser())) {
									ArrayList<User> list = new ArrayList<>();
									list.add(sr.getUser());
									Utils.pushUpdates(
											list,
											"Your request got upvoted. Click here to check status",
											"/requests/" + sr.getId());
								}
							}
						}
						if (path.endsWith("xml")) {
							if (errorCode > 0) {
								Logger.getGlobal().info(
										"Error: " + errorCode + " "
												+ errorMessage);
								Utils.sendError(req, resp, path, errorCode,
										errorMessage, Format.XML);
							} else {
								try {
									Document document = DocumentBuilderFactory
											.newInstance().newDocumentBuilder()
											.newDocument();
									Element root = document
											.createElement("service_requests");
									Element request = document
											.createElement("request");
									request.appendChild(XMLUtils
											.createTextElement(document,
													"service_request_id",
													Long.toString(sr.getId())));
									request.appendChild(XMLUtils
											.createTextElement(document,
													"votes", Long.toString(sr
															.getVotes())));
									root.appendChild(request);

									document.appendChild(root);
									XMLUtils.sendXML(document, resp);
								} catch (ParserConfigurationException e) {
									// TODO Error ??
									e.printStackTrace();
								}
							}
						} else if (path.endsWith("json")) {
							if (errorCode > 0) {
								Logger.getGlobal().info(
										"Error: " + errorCode + " "
												+ errorMessage);
								Utils.sendError(req, resp, path, errorCode,
										errorMessage, Format.JSON);
							} else {
								JSONArray json = new JSONArray();
								try {
									JSONObject request = new JSONObject();
									request.put("service_request_id",
											sr.getId());
									request.put("votes", sr.getVotes());
									json.put(request);
								} catch (JSONException e) {
									// TODO Error ??
									e.printStackTrace();
								}
								JSONUtils.sendJSON(json.toString(), resp);
							}
						} else {
							errorMessage = "Illegal access";
							Logger.getGlobal().info(
									"Error: " + errorCode + " " + errorMessage);
							Utils.sendError(req, resp, path, errorCode,
									errorMessage, null);
						}
					} catch (Exception e) {
						e.printStackTrace();
						errorCode = 404;
						errorMessage = "Request with code "
								+ req.getParameter("service_request_id")
								+ " was not found";
						Logger.getGlobal().info(
								"Error: " + errorCode + " " + errorMessage);
						Utils.sendError(req, resp, path, errorCode,
								errorMessage, null);
					}
				}
			} else {// Get data from post body, create ServiceRequest, store it
					// and
					// send response in requested format
				PersistenceManager pm = PMF.get().getPersistenceManager();
				ServiceRequest sr = new ServiceRequest();
				try {
					int errorCode = -1;
					String errorMessage = null;
					User dataUser = Utils.getOrCreateUser(user, pm);
					try {
						if (req.getParameter("service_code") == null) {
							throw new IllegalArgumentException("service code");
						}

						Service service = pm.getObjectById(Service.class,
								KeyFactory.createKey(Service.class
										.getSimpleName(), Long.parseLong(req
										.getParameter("service_code"))));

						sr.deserializeFromEncodedForm(req, service, dataUser);
						pm.makePersistent(sr);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						errorCode = 400;
						errorMessage = "Required parameter " + e.getMessage()
								+ " is not provided or has invalid format";
					} catch (Exception e) {
						e.printStackTrace();
						errorCode = 404;
						// errorMessage = "Service with code " +
						// split[0].substring(1)
						// + " was not found";
						errorMessage = "Service with code "
								+ req.getParameter("service_code")
								+ " was not found";
					}
					if (split.length == 1) {
						if (errorCode > 0) {
							Logger.getGlobal().info(
									"Error: " + errorCode + " " + errorMessage);
							Utils.sendError(req, resp, path, errorCode,
									errorMessage, Format.HTML);
						} else {
							// req.setAttribute(Constants.PAGE_KEY,
							// Constants.REQUESTS_KEY);
							// req.setAttribute(Constants.REQUEST_KEY, sr);
							// req.getRequestDispatcher("/WEB-INF/request.jsp")
							// .forward(req, resp);
							resp.sendRedirect("/requests/" + sr.getId());
						}
					} else if (path.endsWith("xml")) {
						if (errorCode > 0) {
							Logger.getGlobal().info(
									"Error: " + errorCode + " " + errorMessage);
							Utils.sendError(req, resp, path, errorCode,
									errorMessage, Format.XML);
						} else {
							try {
								Document document = DocumentBuilderFactory
										.newInstance().newDocumentBuilder()
										.newDocument();
								Element root = document
										.createElement("service_requests");
								Element request = document
										.createElement("request");
								request.appendChild(XMLUtils.createTextElement(
										document, "service_request_id",
										Long.toString(sr.getId())));
								request.appendChild(XMLUtils
										.createTextElement(
												document,
												"service_notice",
												"The City will inspect and require the responsible party to correct within 24 hours and/or issue a Correction Notice or Notice of Violation of the Public Works Code"));
								request.appendChild(XMLUtils.createTextElement(
										document, "account_id", null));
								root.appendChild(request);

								document.appendChild(root);
								XMLUtils.sendXML(document, resp);
							} catch (ParserConfigurationException e) {
								// TODO Error ??
								e.printStackTrace();
							}
						}
					} else {
						if (errorCode > 0) {
							Logger.getGlobal().info(
									"Error: " + errorCode + " " + errorMessage);
							Utils.sendError(req, resp, path, errorCode,
									errorMessage, Format.JSON);
						} else {
							JSONArray json = new JSONArray();
							try {
								JSONObject request = new JSONObject();
								request.put("service_request_id", sr.getId());
								request.put(
										"service_notice",
										"The City will inspect and require the responsible party to correct within 24 hours and/or issue a Correction Notice or Notice of Violation of the Public Works Code");
								request.put("account_id", (Object) null);
								json.put(request);
							} catch (JSONException e) {
								// TODO Error ??
								e.printStackTrace();
							}
							JSONUtils.sendJSON(json.toString(), resp);
						}
					}
				} finally {
					pm.close();
				}
			}
		}
	}
}