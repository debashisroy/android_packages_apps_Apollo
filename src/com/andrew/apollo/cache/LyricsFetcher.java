package com.andrew.apollo.cache;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.lyrics3.Lyrics3v2Field;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.ScrollView;
import android.widget.TextSwitcher;

import com.andrew.apollo.utils.MusicUtils;

/**
 * Loads song lyrics to lyrics view. If lyrics exists in the lyrics cache then set it to lyrics
 * view, otherwise asynchronously load lyrics embedded in the audio file.
 * 
 * @author Debashis Roy (debashis.dr@gmail.com)
 */
public class LyricsFetcher {

    public static final int LYRICS_BG_COLOR = Color.argb(150, 0, 0, 0);
    public static final int NO_LYRICS_BG_COLOR = Color.argb(0, 0, 0, 0);

    private static LyricsFetcher sInstance;
    private Context mContext;
    private LruCache<String, String> mLyricsCache = new LruCache<String, String>(100);

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
     * @param lyricsSwitcher The lyrics view.
     */
    public void loadCurrentLyrics(TextSwitcher lyricsSwitcher) {
        if (lyricsSwitcher == null)
            return;

        String lyrics = "";
        String filePath = MusicUtils.getFilePath();
        if (filePath != null) {
            lyrics = mLyricsCache.get(filePath);
        }

        if (lyrics == null) {
            updateLyricsView(lyricsSwitcher, null);
            new EmbeddedLyricsLoader(lyricsSwitcher).execute(filePath);
        } else {
            updateLyricsView(lyricsSwitcher, lyrics);
        }
    }

    /**
     * Update the lyrics view with new lyrics.
     * 
     * @param lyricsSwitcher The lyrics switcher.
     * @param lyrics Song lyrics text.
     */
    private void updateLyricsView(final TextSwitcher lyricsSwitcher, String lyrics) {
        if (lyricsSwitcher == null)
            return;

        lyricsSwitcher.setText(lyrics);

        if (lyrics != null) {
            if (lyrics == null || lyrics.isEmpty()) {
                updateBackgroundColor(lyricsSwitcher, NO_LYRICS_BG_COLOR);
            } else {
                ((ScrollView) lyricsSwitcher.getParent()).scrollTo(0, 0);
                updateBackgroundColor(lyricsSwitcher, LYRICS_BG_COLOR);
            }
        }
    }

    /**
     * Animate background color transition of song lyrics.
     * 
     * @param lyricsSwitcher The lyrics switcher.
     * @param bgColor Background color.
     */
    private void updateBackgroundColor(final TextSwitcher lyricsSwitcher, Integer bgColor) {
        if (lyricsSwitcher == null) {
            return;
        }
        Drawable background = lyricsSwitcher.getBackground();
        Integer colorFrom = 0;
        if (background instanceof ColorDrawable) {
            colorFrom = ((ColorDrawable) background).getColor();
        }
        if (colorFrom.equals(bgColor)) {
            return;
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, bgColor);
        colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                lyricsSwitcher.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    /**
     * This class loads song lyrics from embedded ID3 tag and puts it in the lyrics cache.
     */
    private class EmbeddedLyricsLoader extends AsyncTask<String, Void, Pair<String, String>> {
        private TextSwitcher mLyricsView;

        public EmbeddedLyricsLoader(TextSwitcher lyricsView) {
            mLyricsView = lyricsView;
        }

        @Override
        protected Pair<String, String> doInBackground(String... params) {
            String filePath = params[0];
            String lyrics = null;
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
            mLyricsCache.put(filePath, (lyrics == null) ? "" : lyrics);
            return new Pair<String, String>(filePath, lyrics);
        }

        @Override
        protected void onPostExecute(Pair<String, String> result) {
            if (result.first.equals(MusicUtils.getFilePath())) {
                updateLyricsView(mLyricsView, result.second);
            }
        }
    }
}
