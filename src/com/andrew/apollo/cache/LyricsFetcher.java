package com.andrew.apollo.cache;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.lyrics3.Lyrics3v2Field;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.andrew.apollo.lyrics.LyricsController;
import com.andrew.apollo.lyrics.search.LyricsUtils;
import com.andrew.apollo.utils.MusicUtils;

/**
 * Loads song lyrics to lyrics view. If lyrics exists in the lyrics cache then set it to lyrics
 * view, otherwise asynchronously load lyrics embedded in the ID3 tag of audio file.
 * 
 * @author Debashis Roy (debashis.dr@gmail.com)
 */
public class LyricsFetcher {

    private static LyricsFetcher sInstance;
    private Context mContext;

    protected LyricsFetcher(final Context context) {
        mContext = context;
    }

    /**
     * Used to create a singleton of the lyrics fetcher
     * 
     * @param context The {@link Context} to use.
     * @return A new instance of this class.
     */
    public static final LyricsFetcher getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new LyricsFetcher(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Loads song lyrics into lyrics view.
     * 
     * @param filePath The path of the audio file.
     * @param lyricsView The lyrics view.
     */
    public void loadCurrentLyrics(String filePath) {
        String lyrics = "";
        if (filePath != null) {
            lyrics = getLyricsController().getLyricsCache().get(filePath);
        }

        if (lyrics == null) {
            getLyricsController().setLyrics(null);
            new EmbeddedLyricsLoader().execute(filePath);
        } else {
            getLyricsController().setLyrics(lyrics);
        }
    }

    /**
     * Returns the lyrics controller.
     */
    private LyricsController getLyricsController() {
        return LyricsController.getInstance(mContext);
    }

    /**
     * Cleans up song lyrics. Removes unnecessary empty lines from the lyrics.
     * 
     * @param lyrics The song lyrics.
     * @return The song lyrics after cleanup.
     */
    private String normalizeLyrics(String lyrics) {
        return (lyrics == null) ? null : lyrics.replaceAll("\n(\n)+", "\n\n").replaceAll("\r\n(\r\n)+", "\r\n\r\n");
    }

    /**
     * This class loads song lyrics from embedded ID3 tag and puts it in the lyrics cache.
     */
    private class EmbeddedLyricsLoader extends AsyncTask<String, Void, Pair<String, String>> {
        @Override
        protected Pair<String, String> doInBackground(String... params) {
            String filePath = params[0];

            String lyrics = LyricsUtils.readLyricsFromFile(filePath);

            if (lyrics != null) {
                return new Pair<String, String>(filePath, lyrics);
            }

            try {
                File sourceFile = new File(filePath);
                AudioFile f = null;
                f = AudioFileIO.read(sourceFile);
                Tag tag = f.getTag();
                TagField lyricsField = tag.getFirstField(FieldKey.LYRICS);
                if (lyricsField instanceof AbstractID3v2Frame) {
                    lyrics = ((AbstractID3v2Frame) lyricsField).getContent();
                }
                if (lyricsField instanceof Lyrics3v2Field) {
                    lyrics = ((Lyrics3v2Field) lyricsField).getBody().getLongDescription();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            lyrics = normalizeLyrics(lyrics);
            getLyricsController().getLyricsCache().put(filePath, (lyrics == null) ? "" : lyrics);
            return new Pair<String, String>(filePath, lyrics);
        }

        @Override
        protected void onPostExecute(Pair<String, String> result) {
            if (result.first.equals(MusicUtils.getFilePath())) {
                getLyricsController().setLyrics(result.second);
            }
        }
    }
}
