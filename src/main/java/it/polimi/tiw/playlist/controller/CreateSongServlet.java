package it.polimi.tiw.playlist.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.sql.*;

import java.util.Calendar;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/CreateSong")
@MultipartConfig
public class CreateSongServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private String imgFolderPath;
	private String audioFolderPath;
	
	public CreateSongServlet() {
		super();
	}
	
	public void init() throws ServletException{
		ServletContext context = getServletContext();
		imgFolderPath = context.getInitParameter("imgFolderPath");
		audioFolderPath = context.getInitParameter("audioFolderPath");
		
		try {
			this.connection = ConnectionHandler.getConnection(context);
			
		} catch (UnavailableException  e) {
			
		}
	}
	
	//method that creates a song
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		HttpSession session = request.getSession(true);
		ServletContext servletContext = getServletContext();
		String userName = (String)session.getAttribute("user");
		
		String songTitle = request.getParameter("songTitle");
		String genre = request.getParameter("genre");
		String singer = request.getParameter("singer");
		String albumTitle = request.getParameter("albumTitle");
		String year = request.getParameter("year");
		int publicationYear = -1;
		
		Part fileImage = request.getPart("fileImage");
		Part fileAudio = request.getPart("fileAudio");
		
		String songError = null;
		
		//Checking the String parameters
		if(songTitle == null || songTitle.isEmpty() || genre == null || genre.isEmpty() || singer == null || singer.isEmpty()
				|| albumTitle == null || albumTitle.isEmpty() || year == null || year.isEmpty() 
				|| fileImage == null || fileImage.getSize() <= 0 || fileAudio == null ||  fileAudio.getSize() <= 0) {
			songError = "Missing parameters";
		}
		
		if(songError == null && (songTitle.length() > 50)) songError = "Song title is too long";
		if(songError == null && !( genre.equals("Others") || genre.equals("Rap") || genre.equals("Rock") || genre.equals("Jazz") || genre.equals("Pop") )) songError = "Genre not valid";
		if(songError == null && (singer.length() > 50)) songError = "Singer name is too long";
		if(songError == null && (singer.length() > 50)) songError = "Album title is too long";
		
		if(songError == null) {
			try {
				publicationYear = Integer.parseInt(year);
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				if(publicationYear > currentYear)
					songError = "The release year of the album is bigger than the current year";
			}catch(NumberFormatException e) {
				songError = "The release year of the album is not valid";
			}
		}
		
		//if an error occurred, the home page is reloaded
		if(songError != null) {
			String path = servletContext.getContextPath() + "/Home?songError=" + songError;
			response.sendRedirect(path);
			return;
		}
		
		//Checking the image file
		String fileImageName = Path.of(fileImage.getSubmittedFileName()).getFileName().toString();
		if(fileImageName.contains("/")) songError = "'/' are not allowed in file names";
		else {
			if(!fileImage.getContentType().startsWith("image")) songError = "The image file is not valid;";
			else {
				if(fileImage.getSize() > 5000000) songError = "Image file size is too big;"; //5 000 000 bytes = 5MB
				else if(fileImageName.length() > 50) songError = "Image file name is too long";
			}
		}
		
		//if an error occurred, the home is reloaded
		if(songError != null) {
			String path = servletContext.getContextPath() + "/Home?songError=" + songError;
			response.sendRedirect(path);
			return;
		}
		
		//Checking the audio file
		String fileAudioName = Path.of(fileAudio.getSubmittedFileName()).getFileName().toString();
		if(fileAudioName.contains("/")) songError = "'/' are not allowed in file names";
		else {
			if(!fileAudio.getContentType().startsWith("audio")) songError = "The audio file is not valid;";
			else {
				if(fileAudio.getSize() > 5000000) songError = "Audio file size is too big;"; //5 000 000 bytes = 5MB
				else if(fileAudioName.length() > 50) songError = "Audio file name is too long";	
			}
		}
		
		//if an error occurred, the home is reloaded
		if(songError != null) {
			String path = servletContext.getContextPath() + "/Home?songError=" + songError;
			response.sendRedirect(path);
			return;
		}
		
		//Preparing the paths for storing the files
		String fileImagePath = this.imgFolderPath + userName + "_" + fileImageName;
		
		String fileAudioPath = this.audioFolderPath + userName + "_" + fileAudioName;
		
		//checking whether an audio/image file with the same name already exists or not
		if(songError == null && new File(fileAudioPath).exists()) songError = "An audio file with this name already exists, change the name please";
		
		boolean alreadyExists = new File(fileImagePath).exists();
		
		//storing the two files
		if(songError == null) {
			if(!alreadyExists) { //if the image file already exists, it will not be replaced		
				try {
					Files.copy(fileImage.getInputStream(), new File(fileImagePath).toPath());
				} catch (Exception e) {
					songError = "Error in uploading the image";
				}
			}
			
			if(songError == null) {
				try {
					Files.copy(fileAudio.getInputStream(), new File(fileAudioPath).toPath());
				} catch (Exception e) {
					songError = "Error in uploading the audio";
				}
			}
		}
		
		
		//if an error occurred, the home is reloaded
		if(songError != null) {
			String path = servletContext.getContextPath() + "/Home?songError=" + songError;
			response.sendRedirect(path);
			return;
		}
		
		//Updating the database
		try {
			if( !(new SongDAO(this.connection).addSongAndAlbum(songTitle, genre, fileAudioName, userName, albumTitle, fileImageName, singer, publicationYear)) ) {
				if(!alreadyExists) {
					new File(fileImagePath).delete();
				}
				new File(fileAudioPath).delete();
				songError = "This song already exists";
			}
		} catch (SQLException e) {
			if(!alreadyExists) {
				new File(fileImagePath).delete();
			}
			new File(fileAudioPath).delete();
			songError = "Database error, try again";
		}
		
		
		String path = servletContext.getContextPath() + "/Home";
		if(songError != null) {
			path += "?songError=" + songError;
		}
		else path += "?message=Song succesfully uploaded"; 
		
		
		response.sendRedirect(path);	
	}
	
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
	}
	
}
