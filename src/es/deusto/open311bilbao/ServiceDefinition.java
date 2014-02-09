package es.deusto.open311bilbao;

import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import es.deusto.open311bilbao.utils.XMLUtils;

@PersistenceCapable
public class ServiceDefinition implements DataSerializable {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private Key serviceCode;

	@Persistent
	@Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="order asc"))
	private List<Attribute> attributes;

	public ServiceDefinition() {
	}

	public ServiceDefinition(Key serviceCode, List<Attribute> attributes) {
		super();
		this.serviceCode = serviceCode;
		this.attributes = attributes;
	}

	public Key getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(Key serviceCode) {
		this.serviceCode = serviceCode;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public JSONObject serializeToJSON() throws JSONException {
		JSONObject root = new JSONObject();
		root.put("service_code", this.serviceCode.getId());

		JSONArray attrs = new JSONArray();
		for (Attribute a : attributes) {
			attrs.put(a.serializeToJSON());
		}
		root.put("attributes", attrs);
		return root;
	}

	@Override
	public Node serializeToXML(Document document) {
		Element root = document.createElement("service_definition");

		root.appendChild(XMLUtils.createTextElement(document, "service_code",
				Long.toString(this.serviceCode.getId())));

		Element attrs = document.createElement("attributes");
		for (Attribute a : attributes) {
			attrs.appendChild(a.serializeToXML(document));
		}
		root.appendChild(attrs);

		return root;
	}

}
