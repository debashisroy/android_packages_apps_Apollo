package com.andrew.apollo.lyrics.search;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.andrew.apollo.R;
import com.andrew.apollo.utils.SongInfo;

public class LyricWiki extends AbstractLyricsSource {
    public static final String SOURCE_ID = "LyricWiki";

    private static final String mURL = "http://lyrics.wikia.com/api.php";

    public String buildRqstUrl(SongInfo info) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(mURL);
        sb.append("?fmt=json");
        if (info.getArtist() != null) {
            sb.append("&artist=");
            sb.append(URLEncoder.encode(info.getArtist(), "UTF-8"));
        }
        if (info.getTitle() != null) {
            sb.append("&song=");
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
            response = response.substring(response.indexOf('=') + 1);

            JSONObject lyricsObj = new JSONObject(response);
            String snippet = lyricsObj.getString("lyrics");

            if (!"Not found".equalsIgnoreCase(snippet)) {
                LyricsResult result = new LyricsResult();
                result.source = this;
                result.artist = lyricsObj.getString("artist");
                result.title = lyricsObj.getString("song");
                result.url = lyricsObj.getString("url");
                result.snippet = snippet;
                result.icon = R.drawable.logo_lyric_wiki;

                setScore(result, info);
                resultList.add(result);
            }
        } catch (Exception e) {
            // TODO log error
            // Do nothing
        }
        return resultList;
    }

    protected String fetchLyrics(LyricsResult lyricsResult) {
        try {
            Document doc = Jsoup.connect(lyricsResult.url).get();
            Element lyricbox = doc.select("div.lyricbox").get(0);
            Elements divs = lyricbox.getElementsByTag("div");
            for (Iterator<Element> iterator = divs.iterator(); iterator.hasNext();) {
                Element div = (Element) iterator.next();
                div.remove();
            }
            String lyricboxText = lyricbox.text();

            StringBuilder sb = new StringBuilder();
            for (TextNode textNode : lyricbox.textNodes()) {
                sb.append(textNode.text()).append("\n");
            }

            if (lyricboxText.contains(LIRICS_INCOMPLETE)) {
                sb.append(LIRICS_INCOMPLETE);
            }
            return sb.toString();
        } catch (Exception e) {
            // TODO log error
            return "Error downloading lyrics.";
        }
    }

    private static String LIRICS_INCOMPLETE = "Unfortunately, we are not licensed to display the full lyrics for this song at the moment.";
}
