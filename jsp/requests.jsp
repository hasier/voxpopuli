<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.repackaged.org.joda.time.DateTime" %>
<%@ page import="java.util.Date" %>
<%@ page import="es.deusto.open311bilbao.servlets.Requests" %>
<%@ page import="es.deusto.open311bilbao.ServiceRequest" %>
<%@ page import="es.deusto.open311bilbao.Service" %>
<%@ page import="es.deusto.open311bilbao.utils.Constants" %>
<%@ page import="es.deusto.open311bilbao.utils.Utils" %>
<%@ page import="java.util.List"%>

<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">
<!-- <link rel="shortcut icon" href="../../docs-assets/ico/favicon.png"> -->

<!-- <title>Open 311 Bilbao</title> -->
<title>Vox populi</title>

<!-- Bootstrap core CSS -->
<link href="/css/bootstrap.css" rel="stylesheet">

<!-- Custom styles for this template -->
<link href="/css/Open311Bilbao.css" rel="stylesheet">

<!-- Datetime local support -->
<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.1/themes/base/jquery.ui.all.css" />
<link href="/css/datetime-local-polyfill.css" rel="stylesheet">
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.1/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://www.modernizr.com/downloads/modernizr-latest.js"></script>
<script type="text/javascript" src="/js/datetime-local-polyfill.min.js"></script>


<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
</head>

<body>
	<jsp:include page="navbar.jsp" />

	<div class="container">

		<!-- Main component for a primary marketing message or call to action -->
		<h1>Requests</h1>

		<div id="map" style="min-height: 400px; align: center:"></div>
		
		<div class="well">
		<div onclick="$('.filter').toggle();" class="filter-control">
			<span class="lead">Filter</span>
			<span class="glyphicon glyphicon-filter filter-button"></span>
		</div>
		<form id="filter" method="get" action="/requests" class="form-inline filter" role="form">
			<div class="form-group">
			<label for="service_code">Service</label>
			<select class="form-control" name="service_code" id="service_filter">
				<option value="">Not specified</option>
			<%
			List<Service> services = (List<Service>) request
				.getAttribute(Constants.SERVICES_KEY);
			for (Service s : services) {
			%>
				<option value="<%=s.getCode().getId()%>"><%=s.getName()%></option>
			<%
			}
			%>
			</select>
			</div>
			
			<div class="form-group">
			<label for="status">Status</label>
			<select class="form-control" name="status" id="status_filter">
				<option value="">Not specified</option>
				<option value="open">Open</option>
				<option value="closed">Closed</option>
			</select>
			</div>
			
			<div class="form-group">
			<label for="start_date">From</label>
			<div>
			<%
				String sDate = request.getParameter("start_date");
				if (sDate == null || sDate.equals("")) {
					sDate = Utils.W3C_DATE_FORMAT.format(new DateTime(new Date()).minusDays(90).toDate());
					sDate = sDate.substring(0, sDate.length() - 5) + ":00.000";
				}
			%>
			<input name="start_date" id="start_date_filter"
				type="datetime-local" value="<%=sDate%>" />
			</div>
			</div>
			
			<div class="form-group">
			<label for="end_date">To</label>
			<div>
			<%
				String eDate = request.getParameter("end_date");
				if (eDate == null || eDate.equals("")) {
					eDate = Utils.W3C_DATE_FORMAT.format(new Date());
					eDate = eDate.substring(0, eDate.length() - 5) + ":00.000";
				}
			%>
			<input name="end_date" id="end_date_filter"
				type="datetime-local" value="<%=eDate%>" />
			</div>
			</div>
			
			<div class="form-group">
			<label for="order">Order</label>
			<select onchange="locationSelected(this.value);" class="form-control" name="order" id="order">
				<option value="">Date</option>
				<option value="location">Closest first</option>
				<option value="popular">Most important first</option>
			</select>
			</div>
			
			<button style="margin-top: 5px;" type="submit" class="btn btn-success">Filter</button>
			
		</form>
		</div>
		<%
			List<ServiceRequest> list = (List<ServiceRequest>) request
					.getAttribute(Constants.REQUESTS_KEY);
				
			if (list == null || list.isEmpty()) {
		%>
		<h3>There are no requests</h3>
		<%
			} else {
		%>
		<div class="list-group">
			<%
				for (ServiceRequest s : list) {
			%>
			<a href="/requests/<%=s.getId()%>" class="list-group-item"> <span
				class="badge"><%=s.getVotes()%></span>
				<h4 class="list-group-item-heading"><%=new String(s.getService().getName().getBytes(), "UTF-8")%></h4>
				<p class="list-group-item-text"><%=new String(s.getDescription().getBytes(), "UTF-8")%></p>
			</a>
			<%
				}
			%>
		</div>
		<%
			}
		%>

	</div>
	<!-- /container -->

	<jsp:include page="alerts.jsp" />


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<jsp:include page="js.jsp" />
	<script src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
	<script src="/js/requests.js"></script>

	<script type="text/javascript">
	<%
		for (ServiceRequest sr : list) {
			String img = "/images/" + sr.getService().getIconName();
			int size = 1;
			if (sr.getVotes() < -5) {
				size = 0;
			} else if (sr.getVotes() > 20) {
				size = 3;
			} else if (sr.getVotes() > 10) {
				size = 2;
			}
			img = img + size + ".png";
	%>
		addMarker(new google.maps.LatLng(<%=sr.getLat()%>, <%=sr.getLon()%>),
			<%=sr.getVotes()%>, "<%=sr.getService().getName()%>",
			"<%=sr.getDescription()%>", "/requests/<%=sr.getId()%>", '<%=img%>');
	<%
		}
	
		String s = request.getParameter("service_code");
		if (s != null && !s.equals("")) {
	%>
		$("select option[value='<%=s%>']").attr("selected","selected");
	<%
		}
		s = request.getParameter("status");
		if (s != null && !s.equals("")) {
	%>
		$("select option[value='<%=s%>']").attr("selected","selected");
	<%
		}
		s = request.getParameter("order");
		if (s != null && !s.equals("")) {
	%>
		$("select option[value='<%=s%>']").attr("selected","selected");
	<%
			if (s.equals("location")) {
	%>
		appendHiddenLocation('<%=request.getParameter("location")%>');
	<%
			}
		}
	%>
	</script>

</body>
</html>
