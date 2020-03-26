package com.example.androidproject.service;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;


import com.example.androidproject.model.Song;
import java.util.ArrayList;
import java.util.Random;

public class SoundService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    public class MusicBinder extends Binder {
        public SoundService getService() {
            return SoundService.this;
        }
    }

    private final IBinder musicBind = new MusicBinder();

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songIndex;
    public boolean isPlay = false;
    private boolean isLoop = true;
    private boolean isMix = false;

    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendStatus(String.valueOf(player.getCurrentPosition()), null);
            handler.postDelayed(runnable, 50);
        }
    };

    public SoundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songIndex = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onEndSong();
            }
        });
    }

    public void onEndSong() {
        if (isLoop) {
            playNext();
        } else {
            playSong();
            player.pause();
            isPlay = false;
        }
    }

    public void setSong(int songIndex) {
        this.songIndex = songIndex;
    }

    public void playNext() {
        if (isMix) {
            songIndex = getRandomNumberInRange(0, songs.size() - 1);
        } else {
            songIndex++;
        }

        if (songIndex == songs.size()) {
//            if (!isLoop) {
//                player.stop();
//                player.seekTo(0);
//                isPlay = false;
//                return;
//            }
            songIndex = 0;
        }
        playSong();
    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void playPrevious() {
        songIndex--;
        if (songIndex < 0) {
            songIndex = songs.size() - 1;
        }
        playSong();
    }

    public void playSong() {
        player.reset();
        Song playSong = songs.get(songIndex);
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
        sendStatus(null, String.valueOf(playSong.getDuration()));
        setNameActivity(playSong.getTitle());
        handler.postDelayed(runnable, 0);
        isPlay = true;
    }

    private void sendStatus(String timeLineDuration, String max) {
        if (timeLineDuration == null) {
            timeLineDuration = "null";
        }
        if (max == null) {
            max = "null";
        }
        Intent intent = new Intent("musicRequest");
        intent.putExtra("timeLineDuration", timeLineDuration);
        intent.putExtra("maxDuration", max);
        sendBroadcast(intent);
    }

    private void setNameActivity(String name) {
        if (name == null) {
            name = "null";
        }
        Intent intent = new Intent("musicRequest");
        intent.putExtra("name", name);
        sendBroadcast(intent);
    }

    public void setDurationSong(int a) {
        if (isPlay) {
            player.seekTo(a);
        }
    }

    public void toggleLoop() {
        isLoop = !isLoop;
        //TODO change icon
    }

    public void toggleMix() {
        isMix = !isMix;
        //TODO change icon
    }

    public void pauseSong() {
        player.pause();
        isPlay = false;
        handler.removeCallbacks(runnable);
    }

    public void resumeSong() {
        player.start();
        isPlay = true;
        handler.postDelayed(runnable, 0);
    }
}
