package com.andrew.apollo.cache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
import android.util.LruCache;
import android.widget.TextView;

public class LyricsFetcher {

    public static final int LYRICS_BG_COLOR = Color.argb(150, 0, 0, 0);
    public static final int NO_LYRICS_BG_COLOR = Color.argb(0, 0, 0, 0);

    private static LyricsFetcher sInstance;
    private Context mContext;
    private LruCache<String, String> mLyricsCache = new LruCache<String, String>(100);

    protected LyricsFetcher(final Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Used to create a singleton of the lyrics fetcher
     * 
     * @param context
     *            The {@link Context} to use
     * @return A new instance of this class.
     */
    public static final LyricsFetcher getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new LyricsFetcher(context.getApplicationContext());
        }
        return sInstance;
    }

    public void loadLyrics(String filePath, TextView lyricsView) {
        String lyrics = mLyricsCache.get(filePath);

        if (lyrics == null) {
            updateLyricsView(lyricsView, null);
            new LyricsLoader(lyricsView).execute(filePath);
        } else {
            updateLyricsView(lyricsView, lyrics);
        }
    }

    private void updateLyricsView(final TextView lyricsView, String lyrics) {
        if (lyricsView == null)
            return;

        if (lyrics == null) {
            lyricsView.setText("");
        } else if (lyrics.isEmpty()) {
            lyricsView.setText("");
            updateColor(lyricsView, NO_LYRICS_BG_COLOR);
        } else {
            lyricsView.setText(lyrics);
            updateColor(lyricsView, LYRICS_BG_COLOR);
        }
    }

    private void updateColor(final TextView lyricsView, Integer colorTo) {
        if (lyricsView == null) {
            return;
        }
        Drawable bg = lyricsView.getBackground();
        Integer colorFrom = 0;
        if (bg instanceof ColorDrawable) {
            colorFrom = ((ColorDrawable) bg).getColor();
        }
        if (colorFrom.equals(colorTo)) {
            return;
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                lyricsView.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    private class LyricsLoader extends AsyncTask<String, Void, String> {
        private TextView mLyricsView;

        public LyricsLoader(TextView lyricsView) {
            mLyricsView = lyricsView;
        }

        @Override
        protected String doInBackground(String... params) {
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
            return lyrics;
        }

        @Override
        protected void onPostExecute(String result) {
            updateLyricsView(mLyricsView, result);
        }
    }
}
