package com.andrew.apollo.lyrics.search;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.andrew.apollo.R;
import com.andrew.apollo.utils.SongInfo;

public class LyricsNMusic extends AbstractLyricsSource {
    public static final String SOURCE_ID = "LyricsNMusic";

    private static final String mApiKey = "5bc28dd10850b1dd16d15b8faa5957";
    private static final String mURL = "http://api.lyricsnmusic.com/songs";
    private boolean mIsKeyworkSearch = false;

    public LyricsNMusic() {
        mIsKeyworkSearch = false;
    }

    public LyricsNMusic(boolean isKeyworkSearch) {
        mIsKeyworkSearch = isKeyworkSearch;
    }

    public String buildRqstUrl(SongInfo info) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(mURL);
        sb.append("?api_key=");
        sb.append(mApiKey);

        if (mIsKeyworkSearch) {
            sb.append("&q=");
            sb.append(URLEncoder.encode(info.getFileName(), "UTF-8"));
        } else {
            sb.append("&artist=");
            sb.append(URLEncoder.encode(info.getArtist(), "UTF-8"));

            sb.append("&track=");
            sb.append(URLEncoder.encode(info.getTitle(), "UTF-8"));
        }
        return sb.toString();
    }

    public String getSourceId() {
        return SOURCE_ID;
    }

    public List<LyricsResult> queryLyrics(SongInfo info) {
        List<LyricsResult> resultList = new ArrayList<LyricsResult>();
        try {
            String rqstUrl = buildRqstUrl(info);
            String response = sendGETRequestAndReceiveResponseBody(rqstUrl);

            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0, length = jsonArray.length(); i < 1 && i < length; i++) {
                JSONObject lyricsObj = jsonArray.getJSONObject(i);

                LyricsResult result = new LyricsResult();
                result.source = this;
                result.artist = lyricsObj.getJSONObject("artist").getString("name");
                result.title = lyricsObj.getString("title");
                result.url = lyricsObj.getString("url");
                result.snippet = lyricsObj.getString("snippet");
                result.icon = R.drawable.logo_lyrics_n_music;

                setScore(result, info);
                resultList.add(result);
            }
        } catch (Exception e) {
            // TODO log error
        }
        return resultList;
    }

    protected String fetchLyrics(LyricsResult lyricsResult) {
        try {
            Document doc = Jsoup.connect(lyricsResult.url).get();
            Element main = doc.getElementById("main");
            Elements pre = main.getElementsByAttributeValue("itemprop", "description");

            return pre.text();
        } catch (Exception e) {
            // TODO log error
            return "Error downloading lyrics.";
        }
    }
}
