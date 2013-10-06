package com.andrew.apollo.lyrics.search;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.XML;

import com.andrew.apollo.R;
import com.andrew.apollo.utils.SongInfo;

public class ChartLyrics extends AbstractLyricsSource {
    public static final String SOURCE_ID = "ChartLyrics";

    private static final String mURL = "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect";

    public String buildRqstUrl(SongInfo info) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(mURL);
        sb.append("?");
        if (info.getArtist() != null && info.getTitle() != null) {
            sb.append("&artist=");
            sb.append(URLEncoder.encode(info.getArtist(), "UTF-8"));

            sb.append("&song=");
            sb.append(URLEncoder.encode(info.getTitle(), "UTF-8"));
        }
        return sb.toString();
    }

    @Override
    public List<LyricsResult> queryLyrics(SongInfo info) {
        List<LyricsResult> resultList = new ArrayList<LyricsResult>();
        try {
            String rqstUrl = buildRqstUrl(info);
            String responseXml = sendGETRequestAndReceiveResponseBody(rqstUrl);
            JSONObject lyricsObj = XML.toJSONObject(responseXml).getJSONObject("GetLyricResult");

            String lyricChecksum = lyricsObj.has("LyricChecksum") ? lyricsObj.getString("LyricChecksum") : null;
            if (lyricChecksum != null) {
                ChartLyricsResult result = new ChartLyricsResult();
                result.source = this;
                result.artist = lyricsObj.getString("LyricArtist");
                result.title = lyricsObj.getString("LyricSong");
                result.url = lyricsObj.getString("LyricUrl");
                result.lyrics = lyricsObj.getString("Lyric");

                int length = result.lyrics.length() / 4;
                while (length > 0 && Character.isWhitespace(result.lyrics.charAt(length))) {
                    length--;
                }
                result.snippet = result.lyrics.substring(0, length) + "...";

                result.icon = R.drawable.logo_lyrics_source;

                setScore(result, info);
                resultList.add(result);
            }
        } catch (Exception e) {
            // TODO log error
        }
        return resultList;
    }

    @Override
    protected String fetchLyrics(LyricsResult result) {
        if (result instanceof ChartLyricsResult) {
            return ((ChartLyricsResult) result).lyrics;
        }
        return "Invalid request.";
    }

    private class ChartLyricsResult extends LyricsResult {
        private String lyrics;
    }
}
