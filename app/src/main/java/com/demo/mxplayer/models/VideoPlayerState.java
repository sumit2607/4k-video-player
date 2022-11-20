package com.demo.mxplayer.models;

public class VideoPlayerState {
    private int currentTime = 0;
    private String filename;
    private String messageText;
    private int start = 0;
    private int stop = 0;

    public String getMessageText() {
        return this.messageText;
    }

    public void setMessageText(String str) {
        this.messageText = str;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String str) {
        this.filename = str;
    }

    public float getStart() {
        return (float) this.start;
    }

    public void setStart(int i) {
        this.start = i;
    }

    public int getStop() {
        return this.stop;
    }

    public void setStop(int i) {
        this.stop = i;
    }

    public void reset() {
        this.stop = 0;
        this.start = 0;
    }

    public float getDuration() {
        return (float) (this.stop - this.start);
    }

    public int getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(int i) {
        this.currentTime = i;
    }

    public boolean isValid() {
        return this.stop > this.start;
    }
}
