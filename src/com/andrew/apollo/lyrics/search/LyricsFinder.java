package com.andrew.apollo.lyrics.search;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

import com.andrew.apollo.LyricsSearchActivity;
import com.andrew.apollo.R;
import com.andrew.apollo.utils.SongInfo;

public class LyricsFinder {

    private LyricsSearchActivity mContext;
    private SongInfo mSongInfo;
    private int mResponseCount;
    private boolean mSearching;
    private ProgressDialog mProgressDialog;
    private List<LyricsResult> mSearchResult;
    private List<LyricsSearchTask> mTaskList = new ArrayList<LyricsSearchTask>();

    protected static List<AbstractLyricsSource> mLyricsSourceList = new ArrayList<AbstractLyricsSource>();

    public LyricsFinder(LyricsSearchActivity searchActivity) {
        mContext = searchActivity;
    }

    public void search(SongInfo info) {
        if (mSearching) {
            // TODO Log/warn
            return;
        }
        mSearching = true;
        mResponseCount = 0;
        mSongInfo = info;
        mSearchResult = new ArrayList<LyricsResult>();

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(R.string.lyrics_search_dialog_title);
        mProgressDialog.setMessage(mContext.getResources().getString(R.string.lyrics_search_dialog_message));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onSearchCancel();
            }
        });
        mProgressDialog.show();

        for (AbstractLyricsSource source : mLyricsSourceList) {
            LyricsSearchTask searchTask = new LyricsSearchTask(source);
            mTaskList.add(searchTask);
            searchTask.execute(mSongInfo);
        }
    }

    protected void onSearchResponse(AbstractLyricsSource source, List<LyricsResult> result) {
        mResponseCount++;
        mSearchResult.addAll(result);
        mContext.addSearchResult(result);
        if (mResponseCount == mLyricsSourceList.size()) {
            searchComplete();
        }
    }

    private void searchComplete() {
        mSearching = false;
        mResponseCount = 0;
        mSongInfo = null;

        mTaskList.clear();
        mProgressDialog.dismiss();
        mSearchResult = null;
    }

    private void onSearchCancel() {
        for (LyricsSearchTask searchTask : mTaskList) {
            searchTask.cancel(false);
        }
        searchComplete();
        mContext.finish();
    }

    class LyricsSearchTask extends AsyncTask<SongInfo, Void, List<LyricsResult>> {
        private AbstractLyricsSource mLyricsSource;

        LyricsSearchTask(AbstractLyricsSource lyricsSource) {
            mLyricsSource = lyricsSource;
        }

        @Override
        protected List<LyricsResult> doInBackground(SongInfo... params) {
            try {
                return mLyricsSource.queryLyrics(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LyricsResult> result) {
            super.onPostExecute(result);
            onSearchResponse(mLyricsSource, result);
        }
    }

    static {
        mLyricsSourceList.add(new LyricsNMusic());
        mLyricsSourceList.add(new LyricWiki());
        mLyricsSourceList.add(new ChartLyrics());
        mLyricsSourceList.add(new LyricsNMusic(true));
    }

}
