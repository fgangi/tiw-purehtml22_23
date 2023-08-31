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
import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.utils.ConnectionHandler;
import it.polimi.tiw.playlist.utils.TemplateHandler;
import java.util.ArrayList;

@WebServlet("/Playlist")
public class PlaylistServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public PlaylistServlet() {
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
		SongDAO songDAO = new SongDAO(this.connection);
		PlaylistDAO playlistDAO = new PlaylistDAO(this.connection);
		String userName = (String)session.getAttribute("user");
		String error = null;
		
		//taking attributes
		String playlistName = request.getParameter("playlistName"); //its existence is checked by the filter
		String message = request.getParameter("message") != null ? URLDecoder.decode(request.getParameter("message"), "UTF-8") : null;
		String playlistError = request.getParameter("playlistError") != null ? URLDecoder.decode(request.getParameter("playlistError"), "UTF-8") : "";
		int lowerBound = -1;
		try {
			lowerBound = request.getParameter("lowerBound") != null ? Integer.parseInt(request.getParameter("lowerBound")) : 0;
		}
		catch(NumberFormatException e) {
			lowerBound = 0;
		}
		
		//checking the playlist name
		try {
			if( !(playlistDAO.belongTo(playlistName, userName)) ) {
				String path = servletContext.getContextPath() + "/Home?generalError=Playlist not found";
				response.sendRedirect(path);
				return;
			}
		} catch (SQLException | IOException e1) {
			String path = servletContext.getContextPath() + "/Home?generalError=Database error, try again";
			response.sendRedirect(path);
			return;
		}
		
		//fixing the lower bound
		lowerBound -= lowerBound%5;
		
		//taking all the user's songs that are not in the playlist
		ArrayList<Song> notInPlaylistSongs = null;
		try {
			notInPlaylistSongs = songDAO.getSongsNotInPlaylist(playlistName, userName);
			if(notInPlaylistSongs == null || notInPlaylistSongs.isEmpty()) {
				playlistError += "\nYou have no more songs to add";
			}
		}
		catch(SQLException e) {
			playlistError += "\nDatabase error: Unable to load your playlist";
		}
		
		//taking all the user's songs contained in the playlist
		ArrayList<Song> allSongs = null;
		try {
			allSongs = songDAO.getSongTitleAndImg(playlistName, userName);
		}
		catch(SQLException e) {
			error = "Database error: Unable to load your playlist";
		}
		
		//if an error occurred, the user will be redirected to the home page
		if(error != null) {
			String path = servletContext.getContextPath() + "/Home?generalError=" + error;
			response.sendRedirect(path);
			return;
		}
		
		//taking only the songs to render on the page
		ArrayList<Song> songs = new ArrayList<Song>();
		for(int i=0; i<5 && error == null; i++) {
			try {
				songs.add(allSongs.get(lowerBound + i));
			}
			catch(IndexOutOfBoundsException e) {
				error = "Section not found";
			}
		}
		
		//if songs is empty the user is trying to load a section of the playlist in which there are no songs
		if(songs.isEmpty()) {
			String path = servletContext.getContextPath() + "/Home?generalError=" + error;
			response.sendRedirect(path);
			return;
		}
		
		//starting to prepare the presentation of the page
		ctx.setVariable("playlistName", playlistName);
		if(message != null) ctx.setVariable("message", message);
		if(playlistError != "") ctx.setVariable("playlistError", playlistError);
		ctx.setVariable("songs", songs);
		ctx.setVariable("notInPlaylistSongs", notInPlaylistSongs);
		ctx.setVariable("lowerBound", lowerBound);
		if(lowerBound == 0) ctx.setVariable("previousButton", 0);
		else  ctx.setVariable("previousButton", 1);
		if(lowerBound + 5 < allSongs.size()) ctx.setVariable("nextButton", 1);
		else ctx.setVariable("nextButton", 0);
		
		templateEngine.process("WEB-INF/playlist.html", ctx, response.getWriter());	
	}
		
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
	}
	
}
