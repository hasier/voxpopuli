package es.deusto.open311bilbao.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class XMLUtils {

	private XMLUtils() {
	}

	/**
	 * Retrieves an error XML node
	 * 
	 * @param document
	 *            The document to create nodes
	 * @param code
	 *            The error code
	 * @param message
	 *            The error message
	 * @return The built XML element
	 */
	public static Element getXMLError(Document document, int code,
			String message) {
		Element root = document.createElement("error");
		root.appendChild(createTextElement(document, "code",
				Integer.toString(code)));
		root.appendChild(createTextElement(document, "description", message));
		return root;
	}

	/**
	 * Creates an XML text node
	 * 
	 * @param document
	 *            The document to create nodes
	 * @param key
	 *            The name of the XML tag
	 * @param text
	 *            The text in the XML tag
	 * @return The built XML element
	 */
	public static Element createTextElement(Document document, String key,
			String text) {
		Element e = document.createElement(key);
		if (text != null) {
			e.appendChild(document.createTextNode(text));
		}
		return e;
	}

	public static void sendXML(Document document, HttpServletResponse resp)
			throws IOException {
		try {
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			resp.setContentType("application/xml");
			resp.setHeader("Content-Type", "application/xml");
			transformer.transform(new DOMSource(document), new StreamResult(
					resp.getWriter()));
		} catch (TransformerException e) {
			// TODO Error ??
			e.printStackTrace();
		}
	}

}
