package com.andrew.apollo.lyrics.search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.andrew.apollo.utils.SongInfo;

public abstract class AbstractLyricsSource {
    public abstract List<LyricsResult> queryLyrics(SongInfo info);

    protected abstract String fetchLyrics(LyricsResult result);

    public String getLyrics(LyricsResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("Artist: ");
        sb.append(result.artist);
        sb.append("\n");
        sb.append("Title: ");
        sb.append(result.title);
        sb.append("\n");
        if (result.album != null) {
            sb.append("Album: ");
            sb.append(result.album);
            sb.append("\n");
        }
        sb.append("\n");

        sb.append(fetchLyrics(result));

        sb.append("\n\n--\n");
        sb.append("Source: ");
        sb.append(result.url);

        return sb.toString();
    }

    public static String sendGETRequestAndReceiveResponseBody(String rqstUrl) throws Exception {
        URL url = new URL(rqstUrl);
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();

        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String read = br.readLine();

        while (read != null) {
            sb.append(read).append("\n");
            read = br.readLine();
        }

        return sb.toString();
    }

    protected void setScore(LyricsResult result, SongInfo info) {
        if (isStringsEqual(result.title, info.getTitle()) && isStringsEqual(result.artist, info.getArtist())) {
            result.score = 100;
        } else {
            result.score = 50;
        }
    }

    private boolean isStringsEqual(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.trim().equalsIgnoreCase(str2.trim());
    }
}
