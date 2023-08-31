package it.polimi.tiw.playlist.controller;

import java.io.*;
import java.net.URLDecoder;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.sql.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.utils.ConnectionHandler;
import it.polimi.tiw.playlist.utils.TemplateHandler;
import java.util.ArrayList;

@WebServlet("/Home")
public class HomeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public HomeServlet() {
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
	
	//method that prepares the presentation of the home page
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		HttpSession session = request.getSession(true);
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String userName = (String)session.getAttribute("user");
		
		//taking from the database all the playlists of the user
		String playlistListError = null;
		ArrayList<Playlist> playlists = null;
		try {
			playlists = new PlaylistDAO(this.connection).allPlaylists(userName);
			if(playlists == null || playlists.isEmpty()) {
				playlistListError = "You have no playlists saved on your account";
			}
			else {
				ctx.setVariable("playlists", playlists);
			}
		}
		catch(SQLException e) {
			playlistListError = "Database error: Unable to load your palylists";
		}
		
		//taking from the database all the songs of the user
		String playlistError = null;
		ArrayList<Song> songs = null;
		
		try {
			songs = new SongDAO(this.connection).getSongsbyUser(userName);
			if(songs == null || songs.isEmpty()) {
				playlistError = "Upload a song before creating a playlist";
			}
			else ctx.setVariable("songs", songs);
		}
		catch(SQLException e) {
			playlistError = "Database error: Unable to load your songs";
		}
		
		//taking errors coming from the selection of a playlist in the home page
		if(request.getParameter("playlistListError") != null) {
			String temp = request.getParameter("playlistListError");
			temp = URLDecoder.decode(temp, "UTF-8");
			if(playlistListError == null) playlistListError = temp;
			else playlistListError = temp + "\n" + playlistListError;
		}
		if(playlistListError != null) ctx.setVariable("playlistListError", playlistListError);
		
		//taking the errors coming from the create playlist form in the home page
		if(request.getParameter("playlistError") != null) {
			String temp = request.getParameter("playlistError");
			temp = URLDecoder.decode(temp, "UTF-8");
			if(playlistError == null) playlistError = temp;
			else playlistError = temp + "\n" + playlistError;
		}
		if(playlistError != null) ctx.setVariable("playlistError", playlistError);
		
		//taking the errors coming from the create song form in the home page
		if(request.getParameter("songError") != null) {
			ctx.setVariable("songError", URLDecoder.decode(request.getParameter("songError"), "UTF-8"));
		}
		
		//taking the errors
		if(request.getParameter("generalError") != null) {
			ctx.setVariable("generalError",URLDecoder.decode(request.getParameter("generalError"), "UTF-8"));
		}
		
		//taking the messages 
		if(request.getParameter("message") != null) {
			ctx.setVariable("message", URLDecoder.decode(request.getParameter("message"), "UTF-8"));
		}
		
		templateEngine.process("WEB-INF/home.html", ctx, response.getWriter());
	}
	
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
 	}
	
}