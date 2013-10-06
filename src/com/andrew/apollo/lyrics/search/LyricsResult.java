package com.andrew.apollo.lyrics.search;

public class LyricsResult implements Comparable<LyricsResult> {
    public String artist;
    public String album;
    public String title;
    public String url;
    public AbstractLyricsSource source;
    public int icon;
    public String snippet;

    public int score;

    @Override
    public int compareTo(LyricsResult another) {
        if (score == another.score) {
            return 0;
        }
        return score > another.score ? -1 : 1;
    }
}
