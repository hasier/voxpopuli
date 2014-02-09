package es.deusto.open311bilbao;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.appengine.repackaged.com.google.common.base.Joiner;

import es.deusto.open311bilbao.utils.XMLUtils;

@PersistenceCapable
public class Service implements DataSerializable {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key code;

	@Persistent
	private String name;

	@Persistent
	private String description;

	@Persistent
	private boolean hasMetadata = false;

	@Persistent
	private ServiceType type;

	@Persistent
	private List<String> keywords;

	@Persistent
	private String group;

	@Persistent
	private ServiceDefinition definition = null;

	@Persistent
	private String iconName;

	public Service() {
	}

	public Service(Key code, String name, String description,
			boolean hasMetadata, ServiceType type, List<String> keywords,
			String group, ServiceDefinition definition, String iconName) {
		super();
		this.code = code;
		this.name = name;
		this.description = description;
		this.hasMetadata = hasMetadata;
		this.type = type;
		this.keywords = keywords;
		this.group = group;
		this.definition = definition;
		this.iconName = iconName;
	}

	public Key getCode() {
		return code;
	}

	public void setCode(Key code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isHasMetadata() {
		return hasMetadata;
	}

	public void setHasMetadata(boolean hasMetadata) {
		this.hasMetadata = hasMetadata;
	}

	public ServiceType getType() {
		return type;
	}

	public void setType(ServiceType type) {
		this.type = type;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public ServiceDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ServiceDefinition definition) {
		this.definition = definition;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	@Override
	public JSONObject serializeToJSON() throws JSONException {
		JSONObject root = new JSONObject();
		root.put("service_code", this.code.getId());
		root.put("service_name", this.name);
		root.put("description", this.description);
		root.put("metadata", this.hasMetadata);
		root.put("type", this.type.toString().toLowerCase());
		root.put("keywords", Joiner.on(", ").join(keywords));
		root.put("group", this.group);
		return root;
	}

	@Override
	public Node serializeToXML(Document document) {
		Element root = document.createElement("service");

		root.appendChild(XMLUtils.createTextElement(document, "service_code",
				Long.toString(this.code.getId())));
		root.appendChild(XMLUtils.createTextElement(document, "service_name",
				this.name));
		root.appendChild(XMLUtils.createTextElement(document, "description",
				this.description));
		root.appendChild(XMLUtils.createTextElement(document, "metadata",
				Boolean.toString(this.hasMetadata)));
		root.appendChild(XMLUtils.createTextElement(document, "type", this.type
				.toString().toLowerCase()));
		root.appendChild(XMLUtils.createTextElement(document, "keywords",
				Joiner.on(", ").join(keywords)));
		root.appendChild(XMLUtils.createTextElement(document, "group",
				this.group));

		return root;
	}
}
