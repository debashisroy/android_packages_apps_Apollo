package com.andrew.apollo.lyrics.search;

import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andrew.apollo.R;

public class LyricsResultAdapter extends ArrayAdapter<LyricsResult> {
    Context context;
    int layoutResourceId;
    List<LyricsResult> data = null;

    public LyricsResultAdapter(Context context, int layoutResourceId, List<LyricsResult> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public void addAll(Collection<? extends LyricsResult> collection) {
        super.addAll(collection);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LyricsHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new LyricsHolder();
            holder.liIcon = (ImageView) row.findViewById(R.id.liIcon);
            holder.liTitle = (TextView) row.findViewById(R.id.liTitle);
            holder.liArtist = (TextView) row.findViewById(R.id.liArtist);
            holder.liAlbum = (TextView) row.findViewById(R.id.liAlbum);
            holder.snippet = (TextView) row.findViewById(R.id.liLyricsSnippet);
            holder.liLyrics = (RelativeLayout) row.findViewById(R.id.liLyrics);
            holder.liArrow = (ImageView) row.findViewById(R.id.liArrow);

            row.setTag(holder);
        } else {
            holder = (LyricsHolder) row.getTag();
        }

        LyricsResult lyrics = data.get(position);
        holder.data = lyrics;
        holder.liTitle.setText(lyrics.title);
        holder.liArtist.setText(lyrics.artist);
        holder.liAlbum.setText(lyrics.album);
        holder.liIcon.setImageResource(lyrics.icon);
        holder.snippet.setText(lyrics.snippet);

        final RelativeLayout lyricsView = holder.liLyrics;
        final ImageView arrowImage = holder.liArrow;
        holder.liArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int vidibility = lyricsView.getVisibility();
                if (vidibility == View.VISIBLE) {
                    arrowImage.setImageResource(R.drawable.white_arrow_down);
                    lyricsView.setVisibility(View.GONE);
                } else {
                    arrowImage.setImageResource(R.drawable.white_arrow_up);
                    lyricsView.setVisibility(View.VISIBLE);
                }
            }
        });

        return row;
    }

    public static class LyricsHolder {
        public LyricsResult data;
        ImageView liArrow;
        RelativeLayout liLyrics;
        ImageView liIcon;
        TextView liTitle;
        TextView liArtist;
        TextView liAlbum;
        TextView snippet;
    }
}
