<%@page import="es.deusto.open311bilbao.utils.Constants"%>
<%@page import="es.deusto.open311bilbao.utils.Utils"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.HashSet"%>

<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

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

	<div class="container login-providers">

		<h1>Choose a provider to login</h1>

		<div class="well center-block">
			<%
				UserService userService = UserServiceFactory.getUserService();
				User user = userService.getCurrentUser();
				Set<String> attributes = new HashSet<String>();

				for (String provider : Utils.openIdProviders.keySet()) {
					String providerUrl = Utils.openIdProviders.get(provider);
					String loginUrl = userService.createLoginURL(request
							.getAttribute(Constants.LOGIN_REDIRECT_KEY).toString(),
							null, providerUrl, attributes);
			%>
			<p>
				<a href="<%=loginUrl%>"><img class="login-provider well"
					src="/images/<%=provider.toLowerCase()%>.png" /></a>
			</p>
			<%
				}
			%>
		</div>

	</div>
	<!-- /container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
</body>
</html>
