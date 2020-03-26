package com.example.androidproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.androidproject.R;
import com.example.androidproject.model.Song;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLayout = (LinearLayout) songInf.inflate(R.layout.song_display_layout, parent, false);
        //get title and artist views
        TextView songView = (TextView) songLayout.findViewById(R.id.songTitle);
        TextView artistView = (TextView) songLayout.findViewById(R.id.songArtist);
        ImageView albumView = (ImageView) songLayout.findViewById(R.id.album);
        TextView duration = (TextView) songLayout.findViewById(R.id.duration);
        ImageButton detailMenu = (ImageButton) songLayout.findViewById(R.id.menuIcon);
        //get song using position
        Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        duration.setText(convertDuration(currSong.getDuration()));
        //set position as tag
        songLayout.setTag(position);
        detailMenu.setTag(position);
        return songLayout;
    }

    private String convertDuration(long timeSec) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeSec),
                TimeUnit.MILLISECONDS.toSeconds(timeSec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeSec))
        );
    }

}
