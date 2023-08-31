package it.polimi.tiw.playlist.beans;

import java.sql.Date;

public class Playlist{
	private String name;
    private Date creationDate;

    public Playlist(){
        this.name = null;
        this.creationDate = null;
    }

    public Playlist(String name, Date creationDate){
        this.name = name;
        this.creationDate = creationDate;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    public Date getCreationDate() {
        return this.creationDate;
    }

}