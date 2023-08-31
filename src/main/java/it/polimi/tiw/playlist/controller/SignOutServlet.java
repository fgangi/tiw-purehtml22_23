package it.polimi.tiw.playlist.controller;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/SignOut")
public class SignOutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public SignOutServlet() {
		super();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		//Invalidate session
		request.getSession(true).invalidate();;
		
		//Redirect to the sign in page
		String path = getServletContext().getContextPath();
		response.sendRedirect(path);

	}
	
	
}