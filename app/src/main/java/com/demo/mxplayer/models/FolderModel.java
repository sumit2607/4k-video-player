package com.demo.mxplayer.models;

import java.util.ArrayList;


public class FolderModel {
    private String name;
    private String path,newvideo;
    private int total_video;
    ArrayList<String> newvideoid;
    private String total_size;
    private String directoy_path;
    public FolderModel(String name, String path,int total_video,String newvideo,ArrayList<String> newvideoid,String total_size,String directoy_path) {
        this.name = name;
        this.path = path;
        this.total_video=total_video;
        this.newvideo=newvideo;
        this.newvideoid=newvideoid;
        this.total_size=total_size;
        this.directoy_path=directoy_path;
    }
    public FolderModel(String name, String path,int total_video,String newvideo,ArrayList<String> newvideoid) {
        this.name = name;
        this.path = path;
        this.total_video=total_video;
        this.newvideo=newvideo;
        this.newvideoid=newvideoid;

    }

    public void setTotal_size(String total_size) {
        this.total_size = total_size;
    }

    public String getTotal_size() {
        return total_size;
    }

    public void setDirectoy_path(String directoy_path) {
        this.directoy_path = directoy_path;
    }

    public String getDirectoy_path() {
        return directoy_path;
    }

    public void setNewvideoid(ArrayList<String> newvideoid) {
        this.newvideoid = newvideoid;
    }

    public ArrayList<String> getNewvideoid() {
        return newvideoid;
    }

    public int getTotal_video() {
        return total_video;
    }

    public void setTotal_video(int total_video) {
        this.total_video = total_video;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNewvideo() {
        return newvideo;
    }

    public void setNewvideo(String newvideo) {
        this.newvideo = newvideo;
    }
}
