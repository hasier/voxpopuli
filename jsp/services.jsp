<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="es.deusto.open311bilbao.servlets.Services"%>
<%@ page import="es.deusto.open311bilbao.Service"%>
<%@ page import="es.deusto.open311bilbao.utils.Constants"%>
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
		<%
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();
			if (user == null) {
		%>
		<h3>You need to be logged in to be able to post a new request</h3>
		<%
			} else {
		%>
		<h1>Services</h1>
		<%
			List<Service> list = (List<Service>) request
						.getAttribute(Constants.SERVICES_KEY);

				if (list == null || list.isEmpty()) {
		%>
		<h3>There are no services</h3>
		<%
			} else {
		%>
		<p>Choose a service to send a request</p>
		<div class="list-group">
			<%
				for (Service s : list) {
							long code = s.getCode().getId();
							boolean hasMetadata = s.isHasMetadata();
			%>
			<a id="<%=code%>"
				href="javascript:loadService(<%=code%>,'<%=s.getName()%>',<%=hasMetadata%>);"
				class="list-group-item">
				<h4 class="list-group-item-heading"><%=s.getName()%></h4>
				<p class="list-group-item-text"><%=s.getDescription()%></p>
			</a>
			<%
				}
			%>
		</div>
		
		<form method="post" action="<%= blobstoreService.createUploadUrl("/requests") %>" id="request" class="form-horizontal" style="display: none;" enctype="multipart/form-data">
			<fieldset>

				<!-- Form Name -->
				<legend>New request: <span id="request-issue"></span></legend>

				<input id="service_code" type="hidden" name="service_code" value="" />

				<!-- Description -->
				<div class="control-group">
					<label class="control-label" for="description">Description</label>
					<div class="controls">
						<textarea class="large-input" id="description" name="description"></textarea>
					</div>
				</div>

				<!-- Location -->
				<div class="control-group">
					<label class="control-label" for="location">Location <span class="red">*</span></label>
					<div id="location" class="controls">
						<input id="lat" name="lat" placeholder="Latitude"
							class="button-size-input small-input" type="text" disabled />
						<input
							id="long" name="long" placeholder="Longitude"
							class="button-size-input small-input" type="text" disabled />
						<button type="button" class="btn btn-primary"
							onclick="javascript:getLocation(onLocationSuccess);">
							<i class="glyphicon glyphicon-map-marker"></i>
						</button>
					</div>
				</div>
				
				<label class="control-label" for="media">Picture</label>
				<div id="media" class="control-group">
					<div class="fileinput fileinput-new" data-provides="fileinput">
						<div class="fileinput-new thumbnail"
							style="width: 200px; height: 150px;">
					  		<img src="http://www.placehold.it/200x150/EFEFEF/AAAAAA&text=no+image">
					  	</div>
						<div class="fileinput-preview fileinput-exists thumbnail"
							style="max-width: 200px; max-height: 150px;"></div>
						<div>
						  	<span class="btn btn-primary btn-file">
						  		<span class="fileinput-new">Select image</span>
						  		<span class="fileinput-exists">Change</span>
						  		<input type="file" name="media" accept="image/*" capture>
						  	</span>
						  	<a href="#" class="btn btn-primary fileinput-exists"
						  		data-dismiss="fileinput">Remove</a>
						</div>
					</div>
				</div>
				
				<img id="loading" src="/images/ajax-loader.gif" style="display: none;" />
				<div id="extra-attributes"></div>
				
				<div style="margin-top: 20px; margin-bottom: 5px;"><span class="red">*</span> Required fields</div>
				
				<button type="submit" class="btn btn-success">Submit request</button>

			</fieldset>
		</form>
		<%
			}
		%>

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
	<script src="/js/services.js"></script>
	
	<%
		Service service = (Service) request.getAttribute(Constants.SERVICE_KEY);
		if (service != null) {
	%>
	<script type="text/javascript">
		loadService(<%=service.getCode().getId()%>,'<%=service.getName()%>',<%=service.isHasMetadata()%>);
	</script>
	<%
		}
	%>
	
</body>
</html>
