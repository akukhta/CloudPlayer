package com.example.myapplication.Mdl;

import com.google.firebase.database.Exclude;

public class UploadSong {
    public String title,songDuration,songLink,key;

    public UploadSong(){}
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String getSongLink() {
        return songLink;
    }

    public void setKey(String key){
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }

    public UploadSong(String tile, String songDuration, String songLink){
        this.title = tile;
        this.songDuration = songDuration;
        this.songLink = songLink;
    }

}
