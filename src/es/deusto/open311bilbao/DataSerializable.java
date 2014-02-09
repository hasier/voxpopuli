package es.deusto.open311bilbao;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public interface DataSerializable {

	public JSONObject serializeToJSON() throws JSONException;

	public Node serializeToXML(Document document);

}
