package it.polimi.tiw.playlist.controller;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.sql.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;
import it.polimi.tiw.playlist.utils.TemplateHandler;


@WebServlet("/SignIn")
public class SignInServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public SignInServlet() {
		super();
	}
	
	public void init() throws ServletException{
		try {
			ServletContext context = getServletContext();
			this.connection = ConnectionHandler.getConnection(context);
			this.templateEngine = TemplateHandler.getTemplateEngine(context);
			
		} catch (UnavailableException  e) {
			
		}
	}
	
	//method that checks the user credentials
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		String error = null;
		
		//checking the given parameters
		if(userName == null || password == null || userName.isEmpty() || password.isEmpty()) error = "Missing parameters";
		else {
			try {
				if(new UserDAO(this.connection).authentication(userName, password)) {
					request.getSession(true).setAttribute("user", userName);
				} else {
					error = "Wrong UserName or Password";
				}
			} catch (SQLException e) {
				error = "Database error, try again";
			}
		}
		
		//if an error occurred, the page will be reloaded 
		if(error != null) {
			ctx.setVariable("error", error);
			templateEngine.process("sign-in.html", ctx, response.getWriter());
			return;
		}
			
		//Redirect to the home page
		String path = getServletContext().getContextPath() + "/Home";
		response.sendRedirect(path);
	}
	
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
    }
}