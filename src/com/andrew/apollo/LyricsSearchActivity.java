package com.andrew.apollo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.andrew.apollo.lyrics.LyricsController;
import com.andrew.apollo.lyrics.search.LyricsFinder;
import com.andrew.apollo.lyrics.search.LyricsResult;
import com.andrew.apollo.lyrics.search.LyricsResultAdapter;
import com.andrew.apollo.lyrics.search.LyricsResultAdapter.LyricsHolder;
import com.andrew.apollo.lyrics.search.LyricsUtils;
import com.andrew.apollo.utils.SongInfo;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class LyricsSearchActivity extends Activity implements OnItemClickListener {
    /**
     * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS}
     * milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction
     * before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private SongInfo mSongInfo;
    private ListView mLyricsListView;

    private ArrayList<LyricsResult> mLyricsList;

    private LyricsController mLyricsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mSongInfo = (SongInfo) intent.getSerializableExtra("song");

        // Lyrics controller
        mLyricsController = LyricsController.getInstance(this.getApplicationContext());

        setContentView(R.layout.activity_lyrics_search);

        mLyricsListView = (ListView) findViewById(R.id.list_lyrics);

        TextView selectedSongTitle = (TextView) findViewById(R.id.selectedSongTitle);
        TextView selectedSongArtist = (TextView) findViewById(R.id.selectedSongArtist);

        selectedSongTitle.setText(mSongInfo.getTitle());
        selectedSongArtist.setText(mSongInfo.getArtist());

        doSearch();
    }

    private void doSearch() {
        mLyricsList = new ArrayList<LyricsResult>();
        LyricsResultAdapter adapter = new LyricsResultAdapter(this, R.layout.lyrics_list_row, mLyricsList);

        mLyricsListView.setAdapter(adapter);

        mLyricsListView.setOnItemClickListener(this);

        new LyricsFinder(this).search(mSongInfo);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to
     * prevent the jarring behavior of controls going away while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            // mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void addSearchResult(List<LyricsResult> result) {
        mLyricsList.addAll(result);
        Collections.sort(mLyricsList);
        ((LyricsResultAdapter) mLyricsListView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final LyricsResult result = ((LyricsHolder) view.getTag()).data;
        final String songFilePath = mSongInfo.getPath();
        final AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            private ProgressDialog mProgressDialog;

            @Override
            protected String doInBackground(String... params) {
                String lyrics = result.source.getLyrics(result);
                LyricsUtils.saveLyricsToFile(lyrics, songFilePath);
                mLyricsController.getLyricsCache().put(songFilePath, lyrics);
                return lyrics;
            }

            @Override
            protected void onPreExecute() {
                mProgressDialog = ProgressDialog.show(LyricsSearchActivity.this,
                        getResources().getString(R.string.lyrics_fetch_dialog_title),
                        getResources().getString(R.string.lyrics_fetch_dialog_message));
            }

            @Override
            protected void onPostExecute(String result) {
                mProgressDialog.dismiss();
                onLyricsDownloaded(result);
            }
        };
        task.execute();
    }

    private void onLyricsDownloaded(String lyrics) {
        this.finish();
    }
}
