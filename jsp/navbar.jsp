<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ page import="es.deusto.open311bilbao.utils.Constants"%>

<div id="navbar" class="navbar navbar-default navbar-fixed-top"
	role="navigation">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse"
				data-target=".navbar-collapse">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand">Vox populi</a>
		</div>
		<%
			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();
		%>
		<div class="navbar-collapse collapse">
			<ul class="nav navbar-nav">
				<%
					if (Constants.REQUESTS_KEY.equals(request
							.getAttribute(Constants.PAGE_KEY))) {
				%>
				<li class="active"><a href="#">Home</a></li>
				<%
					} else {
				%>
				<li><a href="/requests">Home</a></li>
				<%
					}
				%>
				<%
					if (Constants.SERVICES_KEY.equals(request
							.getAttribute(Constants.PAGE_KEY))) {
				%>
				<li class="active"><a href="#">Create request</a></li>
				<%
					} else {
				%>
				<li><a href="/services">Create request</a></li>
				<%
					}
				%>
			</ul>
			<ul class="nav navbar-nav navbar-right">
				<li class="divider"></li>
				<%
					if (user != null) {
						/* user.getNickname();
						user.getEmail();
						user.getFederatedIdentity();
						user.getUserId(); // Good one */
				%>
				<li class="text">Hello, <%=user.getNickname()%>!
				</li>
				<li><a
					href="<%=userService.createLogoutURL(request.getAttribute(
						"javax.servlet.forward.request_uri").toString())%>">Sign
						out</a></li>
				<%
					} else if (!request.getAttribute(
							"javax.servlet.forward.request_uri").toString()
							.startsWith("/login")) {
				%>
				<!-- <li><a
					href="<%=userService.createLoginURL(request.getAttribute(
						"javax.servlet.forward.request_uri").toString())%>">Sign
						in</a></li>-->
				<li><a
					href="/login?<%=Constants.LOGIN_REDIRECT_KEY%>=<%=request.getAttribute(
						"javax.servlet.forward.request_uri").toString()%>">Sign
						in</a></li>
				<%
					}
				%>
			</ul>
		</div>
		<!--/.nav-collapse -->
	</div>
</div>