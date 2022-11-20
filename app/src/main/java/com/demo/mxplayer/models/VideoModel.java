package com.demo.mxplayer.models;

/**
 * Created by cr4ck3r
 * Date: 12/6/18
 * Owner: Raisul Islam
 * Copyright (c) 2018 . All rights reserved.
 */
public class VideoModel {
    private int id;
    private String name;
    private String path;
    private String duration,resolution,format,lenght,media_id;
    Boolean newtag;
    Long milisecond;
    int originalposition;
    public VideoModel(int id, String name, String path, String duration,Boolean newtag) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.duration = duration;
        this.newtag = newtag;
    }
    public VideoModel(int id, String name, String path, String duration,Boolean newtag,String resolution,String format,String lenght,String media_id) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.duration = duration;

        this.newtag = newtag;
        this.resolution = resolution;
        this.format = format;
        this.lenght = lenght;
        this.media_id = media_id;
    }
    public VideoModel(int id, String name, String path, String duration,Boolean newtag,String resolution,String format,String lenght,String media_id,Long milisecond) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.duration = duration;

        this.newtag = newtag;
        this.resolution = resolution;
        this.format = format;
        this.lenght = lenght;
        this.media_id = media_id;
        this.milisecond = milisecond;
    }

    public Long getMilisecond() {
        return milisecond;
    }

    public void setMilisecond(Long milisecond) {
        this.milisecond = milisecond;
    }

    public String getMedia_id() {
        return media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setLenght(String lenght) {
        this.lenght = lenght;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getFormat() {
        return format;
    }

    public String getLenght() {
        return lenght;
    }

    public String getResolution() {
        return resolution;
    }

    public void setNewtag(Boolean newtag) {
        this.newtag = newtag;
    }

    public Boolean getNewtag() {
        return newtag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setOriginalposition(int originalposition) {
        this.originalposition = originalposition;
    }

    public int getOriginalposition() {
        return originalposition;
    }
}
