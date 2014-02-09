package es.deusto.open311bilbao;

import javax.servlet.http.HttpServletRequest;

public interface DataDeserializable {

	public void deserializeFromEncodedForm(HttpServletRequest req)
			throws IllegalArgumentException;

}
