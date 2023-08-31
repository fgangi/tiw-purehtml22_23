package it.polimi.tiw.playlist.controller;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;

import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/CreatePlaylist")
public class CreatePlaylistServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public CreatePlaylistServlet() {
		super();
	}
	
	public void init() throws ServletException{
		try {
			ServletContext context = getServletContext();
			this.connection = ConnectionHandler.getConnection(context);
			
		} catch (UnavailableException  e) {
			
		}
	}
	
	//method that creates a playlist
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		HttpSession session = request.getSession(true);
		ServletContext servletContext = getServletContext();
		SongDAO songDAO = new SongDAO(this.connection);
		PlaylistDAO playlistDAO = new PlaylistDAO(this.connection);
		String userName = (String)session.getAttribute("user");
		String playlistError = null;
		
		String playlistName = request.getParameter("playlistName");
		
		//checking whether the given playlist name is valid or not
		if(playlistName == null || playlistName.isEmpty()) {
			playlistError = "Missing parameters";
		}
		else if(playlistName.length() > 50) {
			playlistError = "Playlist name is too long";
		}
		if(playlistError == null){
			try {
				if(playlistDAO.taken(playlistName, userName)) {
					playlistError = playlistName + " playlist already exist";
				}
			} catch (SQLException e) {
				playlistError = "Database error, try again";
			}
		}
		
		//if an error occurred, the home page will be reloaded
		if(playlistError != null) {
			String path = servletContext.getContextPath() + "/Home?=playlistError=" + playlistError;
			response.sendRedirect(path);
			return;
		}
		
		//taking the selected songs and checking whether they are valid or not
		ArrayList<Integer> songsToAdd = new ArrayList<Integer>();
		
		try {
			int maxSize = songDAO.getNumOfSongsbyUser(userName);
			for(Integer i=0; i<maxSize;i++) {
				
				String song = request.getParameter("song"+i.toString());
				if(song != null) { //This song has been chosen
					
					Integer songId = Integer.parseInt(song);
					if(songDAO.belongTo(songId, userName) ) {
						songsToAdd.add(songId);
					}
				}
			}
			if(songsToAdd.isEmpty()) {
				playlistError = "You must select at least one song";
			}
		}
		catch(SQLException e) {
			playlistError = "Database error, try again";
		}
		catch(NumberFormatException e1) {
			playlistError = "Something went wrong";
		}
		
		if(playlistError != null) {
			String path = servletContext.getContextPath() + "/Home?playlistError=" + playlistError;
			response.sendRedirect(path);
			return;
		}
		
		try {
			if(!playlistDAO.addPlaylistWithSongs(playlistName, userName, new Date(System.currentTimeMillis()), songsToAdd.toArray(new Integer[songsToAdd.size()]))) {
				playlistError = "Database error: unable to upload your playlist";
			}
		} catch(SQLException e) {
			playlistError = "Database error, try again";
		}
		
		
		String path = servletContext.getContextPath() + "/Home";
		if(playlistError != null) {
			path += "?playlistError=" + playlistError;
		}
		else path += "?message=Playlist succesfully created";
		response.sendRedirect(path);
	}
	
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
	}
	
}
