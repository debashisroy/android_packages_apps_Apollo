package com.andrew.apollo.lyrics;

import static com.andrew.apollo.utils.PreferenceUtils.ENABLE_SONG_LYRICS;
import static com.andrew.apollo.utils.PreferenceUtils.SHOW_ALBUM_COVER_AS_LYRICS_BG;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.view.View;

import com.andrew.apollo.cache.LyricsFetcher;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.MusicUtils;
import com.andrew.apollo.utils.PreferenceUtils;

/**
 * @author Debashis Roy (debashis.dr@gmail.com)
 */
public class LyricsController implements OnSharedPreferenceChangeListener {
    public static final int LYRICS_BG_TRANSLUCENT = Color.argb(150, 0, 0, 0);
    public static final int LYRICS_BG_OPAQUE = Color.argb(0, 0, 0, 0);
    public static final int NO_LYRICS_BG_COLOR = Color.argb(0, 0, 0, 0);

    protected LyricsView mLyricsView;
    protected Context mContext;
    protected PreferenceUtils mPreferences;
    private static LyricsController mInstance;

    private LyricsController(Context context) {
        mContext = context;
        mPreferences = PreferenceUtils.getInstance(context);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public static LyricsController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LyricsController(context);
        }
        return mInstance;
    }

    /**
     * Show lyrics of currently playing song in lyrics view.
     * 
     * @param lyricsView The lyrics view.
     */
    public void showCurrentLyrics(LyricsView lyricsView) {
        mLyricsView = lyricsView;
        getLyricsFetcher().loadCurrentLyrics(MusicUtils.getFilePath());
    }

    /**
     * Update the lyrics view with new lyrics.
     * 
     * @param lyrics Song lyrics text.
     */
    public void setLyrics(String lyrics) {
        if (mLyricsView == null) {
            return;
        }

        mLyricsView.setLyrics(lyrics);

        if (lyrics != null) {
            if (lyrics == null || lyrics.isEmpty()) {
                setLyricsBackgroundColor(NO_LYRICS_BG_COLOR);
            } else {
                setLyricsBackgroundColor(LYRICS_BG_TRANSLUCENT);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mLyricsView == null) {
            return;
        }
        if (ENABLE_SONG_LYRICS.equals(key)) {
            if (sharedPreferences.getBoolean(ENABLE_SONG_LYRICS, true)) {
                mLyricsView.setVisibility(View.VISIBLE);
                showCurrentLyrics(mLyricsView);
            } else {
                mLyricsView.setLyrics("");
                mLyricsView.setVisibility(View.INVISIBLE);
            }
        }
        if (SHOW_ALBUM_COVER_AS_LYRICS_BG.equals(key)) {
            setLyricsBackgroundColor(mLyricsView.getBackgroundColor());
        }
    }

    /**
     * Sets the background color of lyrics view.
     * 
     * @param color The color.
     */
    private void setLyricsBackgroundColor(int color) {
        if (mLyricsView == null) {
            return;
        }
        if (!mPreferences.isShowCoverAsLyricsBackground()) {
            color = ApolloUtils.setColorAlpha(color, 255);
        }
        mLyricsView.animateSetBackgroundColor(color);
    }

    /**
     * Returns the lyrics fetcher used for loading song lyrics.
     */
    private LyricsFetcher getLyricsFetcher() {
        return LyricsFetcher.getInstance(mContext);
    }
}
