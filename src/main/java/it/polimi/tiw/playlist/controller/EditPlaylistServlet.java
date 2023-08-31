package it.polimi.tiw.playlist.controller;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.sql.*;

import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/EditPlaylist")
public class EditPlaylistServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public EditPlaylistServlet() {
		super();
	}
	
	public void init() throws ServletException{
		try {
			ServletContext context = getServletContext();
			this.connection = ConnectionHandler.getConnection(context);
			
		} catch (UnavailableException  e) {
			
		}
	}
	
	//method that adds the selected song to the playlist
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		HttpSession session = request.getSession(true);
		ServletContext servletContext = getServletContext();
		SongDAO songDAO = new SongDAO(this.connection);
		PlaylistDAO playlistDAO = new PlaylistDAO(this.connection);
		String userName = (String)session.getAttribute("user");
		String playlistError = null; // will be sent to the playlist page
		String error = null; //will be sent to the home page
		
		//checking whether playlistName parameter is valid or not
		String playlistName = request.getParameter("playlistName");

		try {
			if( !(playlistDAO.belongTo(playlistName,userName)) ) {
				error = "Something went wrong";
			}
		}
		catch(SQLException e) {
			error = "Database error, try again";
		}
	
		if(error != null) {
			String path = servletContext.getContextPath() + "/Home?generalError=" + error;
			response.sendRedirect(path);
			return;
		}
		
		//checking whether the selected song is valid or not
		int songId = -1;
		
		try {
			String song = request.getParameter("song");
			if(song != null) {
				Integer tempId = Integer.parseInt(song);
				if(songDAO.belongTo(tempId, userName) && 
						songDAO.getSongsNotInPlaylist(playlistName, userName).stream().map(x -> x.getId()).filter(x -> x == tempId).findFirst().isPresent() ) {
					songId = tempId;
				}
				else playlistError = "Song not found";
			}
			else playlistError = "No song selected";
		}
		catch(SQLException e) {
			playlistError = "Database error, try again";
		}
		catch(NumberFormatException e1) {
			playlistError = "Something went wrong";
		}
		
		//if an error occurred, the playlist page will be reloaded
		if(playlistError != null) {
			String path = servletContext.getContextPath() + "/Playlist?playlistName=" + playlistName + "&playlistError=" + playlistError;
			response.sendRedirect(path);
			return;
		}
		
		//Updating the database
		try {
			if(!playlistDAO.addSongToPlaylist(playlistName, userName, songId)) {
				playlistError = "Database error: Unable to add this song";
			}
		} catch (SQLException e) {
			playlistError = "Database error, try again";
		}
		
		String path = servletContext.getContextPath() + "/Playlist?playlistName=" + playlistName;
		if(playlistError != null) {
			path += "&playlistError=" + playlistError;
		}
		else path += "&message=Song added to the playlist";
		
		response.sendRedirect(path);
	}
	
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
	}
	
}
