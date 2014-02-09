<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ page import="com.google.appengine.api.channel.ChannelServiceFactory"%>
<%@ page import="com.google.appengine.api.channel.ChannelService"%>
<%@ page import="com.google.appengine.api.datastore.KeyFactory"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="es.deusto.open311bilbao.PMF"%>
<%@ page import="es.deusto.open311bilbao.utils.Utils"%>
<!-- <script src="https://code.jquery.com/jquery-1.10.2.js"></script>
	<script src="https://code.jquery.com/jquery-1.10.2.min.js"></script> -->
<script>
if(!window.jQuery)
{
   var script = document.createElement('script');
   script.type = "text/javascript";
   script.src = "/js/jquery-1.9.1.js";
   document.getElementsByTagName('head')[0].appendChild(script);
}
</script>
<!-- <script src="/js/jquery-1.9.1.js"></script>
<script src="/js/jquery-1.9.1.min.js"></script>
<script src="/js/bootstrap.js"></script> -->
<script src="/js/bootstrap.min.js"></script>
<script src="/js/voxpopuli.js"></script>
<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	if (user != null) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ChannelService channelService = ChannelServiceFactory
				.getChannelService();
		es.deusto.open311bilbao.User dbUser = Utils.getOrCreateUser(
				user, pm);
		String token = channelService.createChannel(KeyFactory
				.keyToString(dbUser.getKey()));
		dbUser.setPushToken(token);
%>
<script type="text/javascript" src="/_ah/channel/jsapi"></script>
<script>
	channel = new goog.appengine.Channel('<%=token%>');
	socket = channel.open();
	socket.onmessage = function(message) {
		var json = $.parseJSON(message.data);
		var alertMessage = '<a href="' + json.link + '">' + json.message
				+ '</a>';
		showMessage(alertMessage);
	};
</script>
<%
	pm.close();
	}
%>