package com.example.androidproject.model;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String thumbnailImage;
    private long duration;

    public Song(long id, String title, String artist, long duration, String thumbnailImage) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.thumbnailImage = thumbnailImage;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(String thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
