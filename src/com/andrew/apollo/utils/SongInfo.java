package com.andrew.apollo.utils;

import java.io.File;
import java.io.Serializable;

public class SongInfo implements Serializable {
    private static final long serialVersionUID = 8236748309306557605L;

    private String mArtist;
    private String mAlbum;
    private String mTitle;
    private String mPath;
    private String mFileName;
    private long mAudioId;

    public SongInfo(long audioId, String title, String artist, String album, String path) {
        mAudioId = audioId;
        mTitle = title;
        mArtist = artist;
        mAlbum = album;
        mPath = path;
        mFileName = new File(path).getName();
    }

    public String getArtist() {
        return mArtist;
    }

    public String getmAlbum() {
        return mAlbum;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPath() {
        return mPath;
    }

    public String getFileName() {
        return mFileName;
    }

    public long getAudioId() {
        return mAudioId;
    }
}
