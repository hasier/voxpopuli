package es.deusto.open311bilbao.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import es.deusto.open311bilbao.utils.Constants;

public class Login extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String redirect = req.getParameter(Constants.LOGIN_REDIRECT_KEY);
		if (redirect == null) {
			redirect = "/requests";
		}
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			req.setAttribute(Constants.LOGIN_REDIRECT_KEY, redirect);
			req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
		} else {
			resp.sendRedirect(redirect);
		}
	}

}
