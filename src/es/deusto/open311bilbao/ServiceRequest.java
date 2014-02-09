package es.deusto.open311bilbao;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import es.deusto.open311bilbao.utils.Utils;
import es.deusto.open311bilbao.utils.XMLUtils;

@PersistenceCapable
public class ServiceRequest implements DataSerializable {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	@javax.jdo.annotations.Element(dependent = "false")
	@Unowned
	private Service service;

	@Persistent
	@javax.jdo.annotations.Element(dependent = "false")
	@Unowned
	private User user;

	@Persistent
	private Status status = Status.OPEN;

	@Persistent
	private String statusNotes = null;

	@Persistent
	private String description = null;

	@Persistent
	private String agency = null;

	@Persistent
	private String serviceNotice = null;

	@Persistent
	private Date requestDate = new Date();

	@Persistent
	private Date updateDate = requestDate;

	@Persistent
	private Date expectedDate = null;

	@Persistent
	private String address;

	@Persistent
	private int addressId;

	@Persistent
	private String postalCode;

	@Persistent
	private double lat;

	@Persistent
	private double lon;

	@Persistent
	private BlobKey media = null;

	@Persistent(serialized = "true", defaultFetchGroup = "true")
	private HashMap<Key, Object> attrValues = null;

	@Persistent
	private int votes = 3;

	@Persistent
	@Unowned
	private ArrayList<User> upvoted = new ArrayList<>();

	@Persistent
	@Unowned
	private ArrayList<User> downvoted = new ArrayList<>();

	public ServiceRequest() {
	}

	public ServiceRequest(long id, Service serviceCode, User user,
			Status status, String statusNotes, String description,
			String agency, String serviceNotice, Date requestDate,
			Date updateDate, Date expectedDate, String address, int addressId,
			String postalCode, double lat, double lon, BlobKey media,
			HashMap<Key, Object> attrValues, int votes,
			ArrayList<User> upvoted, ArrayList<User> downvoted) {
		super();
		this.id = id;
		this.service = serviceCode;
		this.user = user;
		this.status = status;
		this.statusNotes = statusNotes;
		this.description = description;
		this.agency = agency;
		this.serviceNotice = serviceNotice;
		this.requestDate = requestDate;
		this.updateDate = updateDate;
		this.expectedDate = expectedDate;
		this.address = address;
		this.addressId = addressId;
		this.postalCode = postalCode;
		this.lat = lat;
		this.lon = lon;
		this.media = media;
		this.attrValues = attrValues;
		this.votes = votes;
		this.upvoted = upvoted;
		this.downvoted = downvoted;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getStatusNotes() {
		return statusNotes;
	}

	public void setStatusNotes(String statusNotes) {
		this.statusNotes = statusNotes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getServiceNotice() {
		return serviceNotice;
	}

	public void setServiceNotice(String serviceNotice) {
		this.serviceNotice = serviceNotice;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(Date expectedDate) {
		this.expectedDate = expectedDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getAddressId() {
		return addressId;
	}

	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public BlobKey getMedia() {
		return media;
	}

	public void setMedia(BlobKey media) {
		this.media = media;
	}

	public HashMap<Key, Object> getAttrValues() {
		return attrValues;
	}

	public void setAttrValues(HashMap<Key, Object> attrValues) {
		this.attrValues = attrValues;
	}

	public int getVotes() {
		return votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}

	public ArrayList<User> getDownvoted() {
		return downvoted;
	}

	public void setDownvoted(ArrayList<User> downvoted) {
		this.downvoted = downvoted;
	}

	public ArrayList<User> getUpvoted() {
		return upvoted;
	}

	public void setUpvoted(ArrayList<User> upvoted) {
		this.upvoted = upvoted;
	}

	public void addDownvote(User user) {
		int vote = user.getRole().getVote();
		if (upvoted.contains(user)) {
			votes -= vote;
			upvoted.remove(user);
		}
		votes -= vote;
		downvoted.add(user);
	}

	public void addUpvote(User user) {
		int vote = user.getRole().getVote();
		if (downvoted.contains(user)) {
			votes += vote;
			downvoted.remove(user);
		}
		votes += vote;
		upvoted.add(user);
	}

	public void deserializeFromEncodedForm(HttpServletRequest req, Service s,
			User user) throws IllegalArgumentException {
		this.service = s;
		try {
			this.lat = Double.valueOf(req.getParameter("lat"));
			this.lon = Double.valueOf(req.getParameter("long"));
		} catch (Exception e) {
			throw new IllegalArgumentException("latitude or longitude", e);
		}
		this.user = user;
		String temp;
		temp = req.getParameter("description");
		if (temp != null) {
			this.description = temp;
		}

		BlobstoreService blobstoreService = BlobstoreServiceFactory
				.getBlobstoreService();
		Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
		List<BlobKey> mediaList = blobs.get("media");
		if (mediaList != null && !mediaList.isEmpty()) {
			this.media = mediaList.get(0);
		}

		this.votes = user.getRole().getBase();
		this.attrValues = new HashMap<>();
		if (s.getDefinition() != null
				&& s.getDefinition().getAttributes() != null) {
			for (Attribute a : s.getDefinition().getAttributes()) {
				long attrId = a.getCode().getId();
				if (a.getType() == Datatype.MULTIVALUELIST) {
					String[] values = req.getParameterValues("attribute["
							+ attrId + "]");
					if (values == null || values.length == 0) {
						if (a.isRequired()) {
							throw new IllegalArgumentException(
									a.getDescription());
						}
					} else {
						this.attrValues.put(a.getCode(), values);
					}
				} else if (req.getParameter("attribute[" + attrId + "]") == null
						|| req.getParameter("attribute[" + attrId + "]").trim()
								.isEmpty()) {
					if (a.isRequired()) {
						throw new IllegalArgumentException(a.getDescription());
					}
				} else {
					String attr = req.getParameter("attribute[" + attrId + "]")
							.trim();
					Object insert = attr;
					if (a.getType() == Datatype.DATETIME) {
						try {
							insert = Utils.W3C_DATE_FORMAT.parse(attr);
						} catch (ParseException e) {
							// e.printStackTrace();
							throw new IllegalArgumentException(
									a.getDescription(), e);
						}
					} else if (a.getType() == Datatype.NUMBER) {
						try {
							insert = Long.parseLong(attr);
						} catch (Exception e) {
							// e.printStackTrace();
							throw new IllegalArgumentException(
									a.getDescription(), e);
						}
					}
					this.attrValues.put(a.getCode(), insert);
				}
			}
		}
	}

	@Override
	public JSONObject serializeToJSON() throws JSONException {
		JSONObject root = new JSONObject();
		root.put("service_request_id", this.id);
		root.put("status", this.status.toString().toLowerCase());
		root.put("status_notes", this.statusNotes);
		root.put("service_name", this.service.getName());
		root.put("service_code", this.service.getCode().getId());
		root.put("description", this.description);
		root.put("agency_responsible", this.agency);
		root.put("service_notice", this.serviceNotice);
		root.put("requested_datetime",
				Utils.W3C_DATE_FORMAT.format(this.requestDate));
		root.put("updated_datetime",
				Utils.W3C_DATE_FORMAT.format(this.updateDate));
		if (this.expectedDate != null) {
			root.put("expected_datetime",
					Utils.W3C_DATE_FORMAT.format(this.expectedDate));
		}
		root.put("address", this.address);
		root.put("address_id", this.addressId);
		root.put("zipcode", this.postalCode);
		root.put("lat", this.lat);
		root.put("long", this.lon);
		String url = "";
		if (this.media != null) {
			url = "/media?key=" + this.media.getKeyString();
		}
		root.put("media_url", url);
		return root;
	}

	@Override
	public Node serializeToXML(Document document) {
		Element root = document.createElement("request");

		root.appendChild(XMLUtils.createTextElement(document,
				"service_request_id", Long.toString(this.id)));
		root.appendChild(XMLUtils.createTextElement(document, "status",
				this.status.toString().toLowerCase()));
		root.appendChild(XMLUtils.createTextElement(document, "status_notes",
				this.statusNotes));
		root.appendChild(XMLUtils.createTextElement(document, "service_name",
				this.service.getName()));
		root.appendChild(XMLUtils.createTextElement(document, "service_code",
				Long.toString(this.service.getCode().getId())));
		root.appendChild(XMLUtils.createTextElement(document, "description",
				this.description));
		root.appendChild(XMLUtils.createTextElement(document,
				"agency_responsible", this.agency));
		root.appendChild(XMLUtils.createTextElement(document, "service_notice",
				this.serviceNotice));
		root.appendChild(XMLUtils.createTextElement(document,
				"expected_datetime",
				Utils.W3C_DATE_FORMAT.format(this.requestDate)));
		root.appendChild(XMLUtils.createTextElement(document,
				"expected_datetime",
				Utils.W3C_DATE_FORMAT.format(this.updateDate)));
		if (this.expectedDate != null) {
			root.appendChild(XMLUtils.createTextElement(document,
					"expected_datetime",
					Utils.W3C_DATE_FORMAT.format(this.expectedDate)));
		}
		root.appendChild(XMLUtils.createTextElement(document, "address",
				this.address));
		root.appendChild(XMLUtils.createTextElement(document, "address_id",
				Long.toString(this.addressId)));
		root.appendChild(XMLUtils.createTextElement(document, "zipcode",
				this.postalCode));
		root.appendChild(XMLUtils.createTextElement(document, "lat",
				Double.toString(this.lat)));
		root.appendChild(XMLUtils.createTextElement(document, "long",
				Double.toString(this.lon)));
		String url = "";
		if (this.media != null) {
			url = "/media?key=" + this.media.getKeyString();
		}
		root.appendChild(XMLUtils.createTextElement(document, "media_url", url));

		return root;
	}

}
