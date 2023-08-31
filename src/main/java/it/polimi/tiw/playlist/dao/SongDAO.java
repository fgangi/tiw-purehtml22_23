package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.Album;

public class SongDAO {
	
private Connection con;
	
	public SongDAO(Connection c) {
		this.con = c;
	}
	
	//methods that returns the Id of the searched album
	private int findAlbumId(String albumTitle , String image , String singer , int publicationYear)throws SQLException {
		int result = 0;
		String query = "SELECT Id FROM ALBUM WHERE Title = ? AND FileImage = ? AND Singer = ? AND PublicationYear = ?";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, image);
			pStatement.setString(3, singer);
			pStatement.setInt(4, publicationYear);
			
			queryRes = pStatement.executeQuery();
			if(queryRes.next()) {
				result = queryRes.getInt("Id");
			}
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return result;
	}
	
	//method that creates a new album (if it not exists yet) and returns its Id
	private int addAlbum(String albumTitle , String image , String singer , int publicationYear) throws SQLException{
		int result = 0;		
		String query = "INSERT into ALBUM (Title , FileImage , Singer , PublicationYear) VALUES (? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, albumTitle);
			pStatement.setString(2, image);
			pStatement.setString(3, singer);
			pStatement.setInt(4, publicationYear);
			result = findAlbumId(albumTitle , image , singer , publicationYear); 
			if(result == 0) {
				if(pStatement.executeUpdate() > 0) {
					result = findAlbumId(albumTitle , image , singer , publicationYear); 
				}
			}
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return result;
	}
	
	//methods that states whether a song exists or not in the database
	private boolean songAlreadyIn(String songTitle , String genre , String audio , String userName , int albumId)throws SQLException {
		boolean result = false;
		String query = "SELECT Id FROM SONG WHERE Title = ? AND Genre = ? AND FileAudio = ? AND User = ? AND Album = ?";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, songTitle);
			pStatement.setString(2,genre);
			pStatement.setString(3, audio);
			pStatement.setString(4, userName);
			pStatement.setInt(5, albumId);
			
			queryRes = pStatement.executeQuery();
			if(queryRes.next()) {
				result = true;
			}
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return result;
	}
	
	//method that adds a song to the database
	private boolean addSong(String songTitle , String genre , String audio , String userName , int albumId) throws SQLException{
		boolean result = false;		
		String query = "INSERT into SONG (Title , Genre, FileAudio , User , Album) VALUES (? , ? , ? , ? , ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, songTitle);
			pStatement.setString(2, genre);
			pStatement.setString(3, audio);
			pStatement.setString(4, userName);
			pStatement.setInt(5, albumId);
			if(!this.songAlreadyIn(songTitle, genre, audio, userName, albumId)) {
				pStatement.executeUpdate();
				result = true;
				
			}
		}catch(SQLException e) {
			throw e;
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return result;
	}
	
	//method that add a song and the corresponding album to the database
	public boolean addSongAndAlbum(String songTitle , String genre , String audio, String userName , String albumTitle , String image,  String singer , int publicationYear) throws SQLException {
		boolean result = false;
		
		try {
			con.setAutoCommit(false);
			
			int albumId = this.addAlbum(albumTitle , image , singer , publicationYear);
			if(this.addSong(songTitle, genre, audio, userName, albumId)) {
				con.commit();
				result = true;
			}
			else con.rollback();
		}catch(SQLException e){
			con.rollback();
			throw e;
		}finally {
			con.setAutoCommit(true);
		}
		return result;
	}
	
	
	//method that returns the necessary attributes for the presentation of the playlist 
	public ArrayList<Song> getSongTitleAndImg(String playlistName, String userName) throws SQLException{
		ArrayList<Song> result = new ArrayList<Song>();
		String query = "SELECT SONG.Id, SONG.Title, ALBUM.FileImage "
				+ "FROM CONTAINS JOIN SONG ON CONTAINS.Song = SONG.Id JOIN ALBUM ON SONG.Album = ALBUM.Id "
				+ "WHERE CONTAINS.PlaylistName = ? AND CONTAINS.PlaylistUser = ? ORDER BY ALBUM.PublicationYear DESC";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, playlistName);
			pStatement.setString(2, userName);
			
			queryRes = pStatement.executeQuery();
			
			Album album;
			Song song;
			while(queryRes.next()) {
				album = new Album();
				song = new Song();
				
				//Read the image from the data base
				song.setId(queryRes.getInt("SONG.Id"));
				song.setTitle(queryRes.getString("SONG.Title"));
				album.setFileImage(queryRes.getString("ALBUM.FileImage"));
				song.setAlbum(album);
				result.add(song);
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	//method that returns the songs of the given user that are not in the given playlist
	public ArrayList<Song> getSongsNotInPlaylist(String playlistName ,String userName) throws SQLException{
		ArrayList<Song> result = new ArrayList<Song>();
		String query = "SELECT SONG.Id, SONG.Title "
				+ "FROM SONG JOIN ALBUM ON SONG.Album = ALBUM.Id WHERE SONG.User = ? AND SONG.Id NOT IN ("
				+ "SELECT Song FROM CONTAINS WHERE PlaylistName = ? AND PlaylistUser = ?) ORDER BY ALBUM.PublicationYear DESC";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, userName);
			pStatement.setString(2, playlistName);
			pStatement.setString(3, userName);
			
			queryRes = pStatement.executeQuery();
			
			Song song;
			while(queryRes.next()) {
				song = new Song();
				song.setId(queryRes.getInt("SONG.Id"));
				song.setTitle(queryRes.getString("SONG.Title"));
				result.add(song);
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	//method that returns all the songs of the given user
	public ArrayList<Song> getSongsbyUser(String userName) throws SQLException{
		ArrayList<Song> result = new ArrayList<Song>();
		String query = "SELECT SONG.Id, SONG.Title "
				+ "FROM SONG JOIN ALBUM ON SONG.Album = ALBUM.Id WHERE SONG.User = ? ORDER BY ALBUM.PublicationYear DESC";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, userName);
			
			queryRes = pStatement.executeQuery();
			
			Song song;
			while(queryRes.next()) {
				song = new Song();
				song.setId(queryRes.getInt("SONG.Id"));
				song.setTitle(queryRes.getString("SONG.Title"));
				result.add(song);
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	//method that returns the necessary attributes in order to reproduce the song
	public Song playSong(int songId) throws SQLException{
		Song result = null;
		String query = "SELECT * FROM SONG JOIN ALBUM on SONG.Album = ALBUM.Id WHERE SONG.Id = ?";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setInt(1, songId);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) {
				result = new Song();
				Album album = new Album();
				result.setId(queryRes.getInt("SONG.Id"));
				result.setTitle(queryRes.getString("SONG.Title"));
				result.setGenre(queryRes.getString("SONG.Genre"));
				result.setFileAudio(queryRes.getString("SONG.FileAudio"));
				album.setTitle(queryRes.getString("ALBUM.Title"));
				album.setFileImage(queryRes.getString("ALBUM.FileImage"));
				album.setSinger(queryRes.getString("ALBUM.Singer"));
				album.setYear(queryRes.getInt("ALBUM.PublicationYear"));
				result.setAlbum(album);
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	//method that verifies whether a song belongs to the given user or not
	public boolean belongTo(int songId , String userName) throws SQLException{
		boolean result = false;
		String query = "SELECT * FROM SONG WHERE Id = ? AND User = ?";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setInt(1, songId);
			pStatement.setString(2, userName);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) {
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return result;
	}	
	
	//method that returns how many songs belong to the given user
	public int getNumOfSongsbyUser(String userName) throws SQLException{
		int result = 0;
		String query = "SELECT * FROM SONG JOIN ALBUM ON SONG.Album = ALBUM.Id WHERE SONG.User = ?";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, userName);
			
			queryRes = pStatement.executeQuery();
			
			while(queryRes.next()) {
				result++;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	//method that verifies whether an image file belongs to the given user or not
	public boolean imgBelongTo(String fileImage , String userName) throws SQLException{
		boolean result = false;
		String query = "SELECT * FROM SONG JOIN ALBUM ON SONG.Album = ALBUM.Id WHERE ALBUM.FileImage = ? AND SONG.User = ?";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, fileImage);
			pStatement.setString(2, userName);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) {
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return result;
	}
		
	//method that verifies whether an audio file belongs to the given user or not
	public boolean audioBelongTo(String fileAudio , String userName) throws SQLException{
		boolean result = false;
		String query = "SELECT * FROM SONG WHERE FileAudio = ? AND User = ?";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, fileAudio);
			pStatement.setString(2, userName);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) {
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return result;
	}

}