package es.deusto.open311bilbao.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.KeyFactory;

import es.deusto.open311bilbao.Attribute;
import es.deusto.open311bilbao.Datatype;
import es.deusto.open311bilbao.PMF;
import es.deusto.open311bilbao.Service;
import es.deusto.open311bilbao.ServiceDefinition;
import es.deusto.open311bilbao.ServiceType;

public class CreateServices extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Service s = new Service();
		s.setName("Street lighting");
		s.setDescription("Issues about street lightning");
		s.setHasMetadata(false);
		s.setGroup("electric");
		s.setType(ServiceType.REALTIME);
		ArrayList<String> list = new ArrayList<>();
		s.setKeywords(list);
		s.setIconName("poweroutage");
		pm.makePersistent(s);
		
		s = new Service();
		s.setName("Park");
		s.setDescription("Issues in parks");
		s.setHasMetadata(false);
		s.setGroup("street");
		s.setType(ServiceType.REALTIME);
		list = new ArrayList<>();
		s.setKeywords(list);
		s.setIconName("urbanpark");
		pm.makePersistent(s);
		
		s = new Service();
		s.setName("Pavement");
		s.setDescription("Issues related to street pavement");
		s.setHasMetadata(false);
		s.setGroup("street");
		s.setType(ServiceType.REALTIME);
		list = new ArrayList<>();
		s.setKeywords(list);
		s.setIconName("pedestriancrossing");
		pm.makePersistent(s);
		
		s = new Service();
		s.setName("Road");
		s.setDescription("Issues about roads");
		s.setHasMetadata(false);
		s.setGroup("road");
		s.setType(ServiceType.REALTIME);
		list = new ArrayList<>();
		s.setKeywords(list);
		s.setIconName("road");
		pm.makePersistent(s);
		
		s = new Service();
		s.setName("Traffic sign");
		s.setDescription("Issues with traffic signs or lights");
		s.setHasMetadata(false);
		s.setGroup("road");
		s.setType(ServiceType.REALTIME);
		list = new ArrayList<>();
		s.setKeywords(list);
		s.setIconName("trafficlight");
		pm.makePersistent(s);
		
		s = new Service();
		s.setName("Rubbish");
		s.setDescription("Issues about rubbish or street cleaning");
		s.setHasMetadata(false);
		s.setGroup("rubbish");
		s.setType(ServiceType.REALTIME);
		list = new ArrayList<>();
		s.setKeywords(list);
		s.setIconName("trash");
		pm.makePersistent(s);
		
		s = new Service();
		s.setName("Other");
		s.setDescription("Other kind of issues");
		s.setHasMetadata(false);
		s.setGroup("other");
		s.setType(ServiceType.REALTIME);
		list = new ArrayList<>();
		s.setKeywords(list);
		s.setIconName("caution");
		pm.makePersistent(s);
	}

	public void doGet1(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		Service s1 = new Service();
		s1.setName("Street lighting");
		s1.setDescription("Issues about street lightning");
		s1.setHasMetadata(false);
		s1.setGroup("electric");
		s1.setType(ServiceType.REALTIME);
		ArrayList<String> list = new ArrayList<>();
		s1.setKeywords(list);

		Service s2 = new Service();
		s2.setName("Traffic lights");
		s2.setDescription("Issues about traffic lights");
		s2.setHasMetadata(true);
		s2.setGroup("electric");
		s2.setType(ServiceType.REALTIME);
		list = new ArrayList<>();
		s2.setKeywords(list);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(s1);
		pm.makePersistent(s2);
		ServiceDefinition sd = new ServiceDefinition();
		// sd.setServiceCode(s2.getCode());
		sd.setServiceCode(KeyFactory.createKey(
				ServiceDefinition.class.getSimpleName(), s2.getCode().getId()));
		List<Attribute> atts = new ArrayList<>();
		Attribute a = new Attribute();
		a.setDescription("The reputation of the request will start at 5");
		a.setVariable(false);
		atts.add(a);
		a = new Attribute();
		a.setType(Datatype.DATETIME);
		a.setDescription("Date");
		a.setType_description("Input date");
		a.setVariable(true);
		a.setRequired(false);
		atts.add(a);
		a = new Attribute();
		a.setType(Datatype.NUMBER);
		a.setDescription("Number");
		a.setVariable(true);
		a.setRequired(true);
		atts.add(a);
		a = new Attribute();
		a.setType(Datatype.STRING);
		a.setDescription("String");
		a.setVariable(true);
		a.setRequired(true);
		atts.add(a);
		a = new Attribute();
		a.setType(Datatype.TEXT);
		a.setDescription("Text");
		a.setType_description("Text area");
		a.setVariable(true);
		a.setRequired(false);
		atts.add(a);
		a = new Attribute();
		a.setType(Datatype.SINGLEVALUELIST);
		a.setDescription("Single value");
		a.setVariable(true);
		a.setRequired(true);
		HashMap<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		a.setListValues(map);
		atts.add(a);
		a = new Attribute();
		a.setType(Datatype.MULTIVALUELIST);
		a.setDescription("Multi value");
		a.setVariable(true);
		a.setRequired(false);
		map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		a.setListValues(map);
		atts.add(a);

		sd.setAttributes(atts);
		s2.setDefinition(sd);
		// pm.makePersistent(s2);
		pm.close();

		// System.out.println(s2.getCode().toString());
		// System.out.println(s2.getCode().getId());
		// System.out.println(s2.getCode().getKind());
		//
		// System.out.println(sd.getServiceCode().toString());
		// System.out.println(sd.getServiceCode().getId());
		// System.out.println(sd.getServiceCode().getKind());
		// Key key2 = KeyFactory.createKey(Service.class.getSimpleName(), s1
		// .getCode().getId());
		// System.out.println(key2.equals(s1.getCode()));

		resp.sendRedirect("/services");
	}
}