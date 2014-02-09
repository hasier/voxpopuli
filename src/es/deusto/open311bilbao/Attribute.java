package es.deusto.open311bilbao;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.jdo.annotations.IdGeneratorStrategy;
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
public class Attribute implements DataSerializable {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key code;

	@Persistent
	private boolean variable;

	@Persistent
	private Datatype type;

	@Persistent
	private boolean required = false;

	@Persistent
	private String type_description = "";

	@Persistent
	private int order;

	@Persistent
	private String description = "";

	@Persistent(serialized = "true", defaultFetchGroup="true")
	private HashMap<String, String> listValues;

	public Attribute() {
	}

	public Attribute(Key code, boolean variable, Datatype type,
			boolean required, String type_description, int order,
			String description, HashMap<String, String> listValues) {
		super();
		this.code = code;
		this.variable = variable;
		this.type = type;
		this.required = required;
		this.type_description = type_description;
		this.order = order;
		this.description = description;
		this.listValues = listValues;
	}

	public Key getCode() {
		return code;
	}

	public void setCode(Key code) {
		this.code = code;
	}

	public boolean isVariable() {
		return variable;
	}

	public void setVariable(boolean variable) {
		this.variable = variable;
	}

	public Datatype getType() {
		return type;
	}

	public void setType(Datatype type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getType_description() {
		return type_description;
	}

	public void setType_description(String type_description) {
		this.type_description = type_description;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public HashMap<String, String> getListValues() {
		return listValues;
	}

	public void setListValues(HashMap<String, String> listValues) {
		this.listValues = listValues;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute other = (Attribute) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public JSONObject serializeToJSON() throws JSONException {
		JSONObject root = new JSONObject();
		root.put("variable", this.variable);
		root.put("code", this.code.getId());
		root.put("datatype", this.type == null ? "" : this.type.toString()
				.toLowerCase());
		root.put("required", this.required);
		root.put("datatype_description", this.type_description);
		root.put("order", this.order);
		root.put("description", this.description);
		JSONArray values = new JSONArray();
		if (listValues != null) {
			for (Entry<String, String> e : listValues.entrySet()) {
				JSONObject j = new JSONObject();
				j.put("key", e.getKey());
				j.put("name", e.getValue());
				values.put(j);
			}
		}
		root.put("values", values);
		return root;
	}

	@Override
	public Node serializeToXML(Document document) {
		Element root = document.createElement("attribute");

		root.appendChild(XMLUtils.createTextElement(document, "variable",
				Boolean.toString(this.variable)));
		root.appendChild(XMLUtils.createTextElement(document, "code",
				Long.toString(this.code.getId())));
		root.appendChild(XMLUtils
				.createTextElement(document, "datatype",
						((this.type == null) ? "" : this.type.toString()
								.toLowerCase())));
		root.appendChild(XMLUtils.createTextElement(document, "required",
				Boolean.toString(this.required)));
		root.appendChild(XMLUtils.createTextElement(document,
				"datatype_description", this.type_description));
		root.appendChild(XMLUtils.createTextElement(document, "order",
				Integer.toString(this.order)));
		root.appendChild(XMLUtils.createTextElement(document, "description",
				this.description));

		Element values = document.createElement("values");
		if (listValues != null) {
			for (Entry<String, String> e : listValues.entrySet()) {
				Element value = document.createElement("value");
				value.appendChild(XMLUtils.createTextElement(document, "key",
						e.getKey()));
				value.appendChild(XMLUtils.createTextElement(document, "name",
						e.getValue()));
				values.appendChild(value);
			}
		}
		root.appendChild(values);

		return root;
	}

}