<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="es.deusto.open311bilbao.Datatype" %>
<%@ page import="es.deusto.open311bilbao.Status" %>
<%@ page import="es.deusto.open311bilbao.User" %>
<%@ page import="es.deusto.open311bilbao.ServiceRequest" %>
<%@ page import="es.deusto.open311bilbao.Attribute" %>
<%@ page import="es.deusto.open311bilbao.utils.Constants" %>

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

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
</head>

<body>
	<jsp:include page="navbar.jsp" />

	<div class="container">

		<%
			User user = (User) request.getAttribute(Constants.CURRENT_USER_KEY);
			ServiceRequest sr = (ServiceRequest) request
					.getAttribute(Constants.REQUEST_KEY);
		%>

		<!-- Main component for a primary marketing message or call to action -->
		<h1><%=sr.getService().getName()%></h1>
		<div class="lead"><%=new String(sr.getDescription().getBytes(), "UTF-8")%></div>
		<div class="col-xs-6">
			Request date:
			<%=SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT,
					request.getLocale()).format(sr.getRequestDate())%></div>
		<div class="col-xs-6 text-right">
		<%
			if (sr.getExpectedDate() != null) {
		%>
			Expected date:
			<%=SimpleDateFormat.getDateInstance(
						SimpleDateFormat.SHORT, request.getLocale()).format(
						sr.getExpectedDate())%>
		<%
			} else {
		%>
		<p><br/></p>
		<%
			}
		%>
		</div>
		<div style="vertical-align: middle;" class="col-xs-6 lead">
		<%
			if (sr.getStatus() == Status.CLOSED) {
		%>
		Closed
		<%
			} else if (!sr.getUpdateDate().equals(sr.getRequestDate())) {
		%>
			<br/>
			Updated:
			<%=SimpleDateFormat.getDateInstance(
						SimpleDateFormat.SHORT, request.getLocale()).format(
						sr.getUpdateDate())%>
		<%
			}
		%>
		</div>
		<%
			if (user == null || sr.getUser().equals(user)
				|| sr.getStatus() == Status.CLOSED) {
		%>
		<div class="col-xs-6 text-right lead">
			<br/>
			Votes: <span class="badge"><%=sr.getVotes()%></span>
		</div>
		<%
			} else {
		%>
		<div class="col-xs-6">
			<div class="text-center pull-right">
				<p class="text-right">
					<%
						if (sr.getUpvoted().contains(user)) {
					%>
					<span id="up" style="color: Green;"
						class="glyphicon glyphicon-chevron-up"></span>
					<%
						} else {
					%>
					<a href="javascript:upvote(<%=sr.getId()%>);"><span id="up"
						class="glyphicon glyphicon-chevron-up"></span></a>
					<%
						}
					%>
				</p>
				<p>
					Votes: <img id="loading" src="/images/ajax-loader.gif"
						style="display: none;" /> <span id="votes" class="badge"><%=sr.getVotes()%></span>
				</p>
				<p class="text-right">
					<%
						if (sr.getDownvoted().contains(user)) {
					%>
					<span id="down" style="color: Green;"
						class="glyphicon glyphicon-chevron-down"></span>
					<%
						} else {
					%>
					<a href="javascript:downvote(<%=sr.getId()%>);"><span id="down"
						class="glyphicon glyphicon-chevron-down"></span></a>
					<%
						}
					%>
				</p>
			</div>

		</div>
		<%
			}
		%>
		
		<p><br/><br/><br/><br/><br/><br/></p>
		
		<div class="col-md-6 grid-right">
			<% String coords = sr.getLat() + "," + sr.getLon(); %>
			<a href="https://maps.google.es/maps?q=<%=coords%>" target="_blank"><img class="img-responsive grid-right"
				src="http://maps.googleapis.com/maps/api/staticmap?center=<%=coords%>&markers=color:red%7Ccolor:red%7Clabel:A%7C<%=coords%>&zoom=15&size=300x300&sensor=false">
			</a>
		<%	if (sr.getMedia() != null) { %>
			<a href="/media?key=<%=sr.getMedia().getKeyString()%>">
			<img style="width: 300px; margin-top: 10px;" class="img-responsive grid-right"
				src="/media?key=<%=sr.getMedia().getKeyString()%>" /></a>
		<%
			}
		%>
		</div>
		<div class="col-md-6 pull-left" style="margin-top: 20px;">
		<%
			if (sr.getStatusNotes() != null) {
		%>
		<h3>Status</h3>
		<p>
			<%=sr.getStatusNotes()%>
		</p>
		<%
			} else {
		%>
		<p class="lead">No action has been performed yet</p>
		<%
			}
		%>

		<%
			if (sr.getService().isHasMetadata()) {
				List<Attribute> attrs = sr.getService().getDefinition()
						.getAttributes();
				HashMap<Key, Object> map = sr.getAttrValues();
				for (Attribute attr : attrs) {
					Object value = map.get(attr.getCode());
					if (!attr.isVariable()) {
		%>
		<p><%=attr.getDescription()%></p>
		<%
			} else if (value != null) {
		%>
		<h3><%=attr.getDescription()%></h3>
		<%
		String print = value.toString();
			if (attr.getType() == Datatype.DATETIME) {
				print = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT,
						request.getLocale()).format(sr.getRequestDate());
			} else if (attr.getType() == Datatype.MULTIVALUELIST) {
				String[] values = (String[]) value;
				print = "";
				for (String s : values) {
					print += "<p>" + s + "</p>";
				}
			}
		%>
		<%=new String(print.getBytes(), "UTF-8") %>
		<%
					}
				}
			}
		%>
		</div>


	</div>
	<!-- /container -->

	<jsp:include page="alerts.jsp" />


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<jsp:include page="js.jsp" />
	<script src="/js/request.js"></script>
</body>
</html>
