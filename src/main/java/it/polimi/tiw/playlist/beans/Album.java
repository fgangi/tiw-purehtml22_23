package it.polimi.tiw.playlist.beans;

public class Album {
    private int id;
    private String title;
    private String fileImage;
    private String singer;
    private int year;

    public Album(){
        this.id = 0;
        this.title = null;
        this.fileImage = null;
        this.singer = null;
        this.year = 0;
    }

    public Album(int id, String title, String fileImage, String singer, int year){
        this.id = id;
        this.title = title;
        this.fileImage = fileImage;
        this.singer = singer;
        this.year = year;
    }


    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }

    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return this.title;
    }

    public void setFileImage(String fileImage){
        this.fileImage = fileImage;
    }
    public String getFileImage(){
        return this.fileImage;
    }

    public void setSinger(String singer){
        this.singer = singer;
    }
    public String getSinger(){
        return this.singer;
    }

    public void setYear(int year){
        this.year = year;
    }
    public int getYear(){
        return this.year;
    }

}
