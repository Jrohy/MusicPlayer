package com.example.john.musicplayer.utils;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * Created by John on 2015/11/26.
 */
public class MusicInfo implements Parcelable {

    private long id;
    private String title;
    private String album;
    private int duration;
    private long size;
    private String artist;
    private String url;
    private Bitmap cover;

    public MusicInfo(){

    }

    public MusicInfo(long id, String title){
        this.id = id;
        this.title = title;
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel source) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setId(source.readLong());
            musicInfo.setTitle(source.readString());
            musicInfo.setAlbum(source.readString());
            musicInfo.setArtist(source.readString());
            musicInfo.setUrl(source.readString());
            musicInfo.setDuration(source.readInt());
            musicInfo.setSize(source.readLong());
            return musicInfo;
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(url);
        dest.writeInt(duration);
        dest.writeLong(size);
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }
}
