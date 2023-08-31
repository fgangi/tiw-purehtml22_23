package it.polimi.tiw.playlist.controller;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.sql.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.utils.ConnectionHandler;
import it.polimi.tiw.playlist.utils.TemplateHandler;

@WebServlet("/Player")
public class PlayerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public PlayerServlet() {
		super();
	}
	
	public void init() throws ServletException{
		ServletContext context = getServletContext();
		try {
			this.connection = ConnectionHandler.getConnection(context);
			this.templateEngine = TemplateHandler.getTemplateEngine(context);
			
		} catch (UnavailableException  e) {
			
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		HttpSession session = request.getSession(true);
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String userName = (String)session.getAttribute("user");
		SongDAO songDAO = new SongDAO(this.connection);
		String songError = null;
		
		//taking songId attribute
		Integer songId = -1;
		try{
			songId = Integer.parseInt(request.getParameter("songId"));
		}
		catch(NumberFormatException e) {
			String path = servletContext.getContextPath() + "/Home?generalError=Song not found";
			response.sendRedirect(path);
			return;
		}
		
		//checking song attribute
		String generalError = null;
		try {
			if( !(songDAO.belongTo(songId, userName)) ) {
				generalError = "Song not found";
			}
		} catch (SQLException e) {
			generalError = "Database error, try again";
		}
		
		//if an error occurred, the user will be redirected to the home page
		if(generalError != null) {
			String path = servletContext.getContextPath() + "/Home?generalError=" + generalError;
			response.sendRedirect(path);
			return;
		}
		
		//taking the song details
		Song song = null;
		try {
			song = songDAO.playSong(songId);
		}
		catch(SQLException e) {
			songError = "Database error, try again";
		}
		
		if(songError != null) {
			ctx.setVariable("songError", songError);
			templateEngine.process("WEB-INF/player.html", ctx, response.getWriter());
			return;
		}

		//starting to prepare the presentation of the page
		ctx.setVariable("song", song);
		
		templateEngine.process("WEB-INF/player.html", ctx, response.getWriter());	
	}
	
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
	}
	
}
