package it.polimi.tiw.playlist.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter( urlPatterns = {"/SignIn", "/SignUp", "/sign-in.html", "/sign-up.html"})
public class NotLoggedChecker implements Filter {
	
	//checks that the session is not active; in case it is not so, redirect to the Home page
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String homePath = req.getServletContext().getContextPath() + "/Home";

		HttpSession s = req.getSession();
		if (!s.isNew() && s.getAttribute("user") != null) {
			res.sendRedirect(homePath);
			return;
		}
		chain.doFilter(request, response);
	}
	
}