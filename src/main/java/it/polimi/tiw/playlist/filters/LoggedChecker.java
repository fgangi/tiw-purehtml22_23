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

@WebFilter( urlPatterns = {"/Home", "/Playlist", "/Player", "/CreatePlaylist", "/EditPlaylist", "/CreateSong", "/GetImage/*", "/GetSong/*", "/SignOut"})
public class LoggedChecker implements Filter {
	
	//checks that the session is active; in case it is not so, redirect to the Sign In page
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String signInPath = req.getServletContext().getContextPath() + "/sign-in.html";

		HttpSession s = req.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			res.sendRedirect(signInPath);
			return;
		}
		chain.doFilter(request, response);
	}
	
}