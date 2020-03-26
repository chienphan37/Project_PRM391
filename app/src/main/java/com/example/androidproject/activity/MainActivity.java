package com.example.androidproject.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.example.androidproject.R;
import com.example.androidproject.model.Song;
import com.example.androidproject.adapter.SongAdapter;
import com.example.androidproject.service.SoundService;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Song> songListInDevice;
    private ArrayList<Song> songListDisplay;
    private ListView songView;
    private EditText searchInput;
    private ImageButton playPause;
    private SoundService soundService;
    private Intent playIntent;
    private boolean musicBound = false;
    private SeekBar timeLine;
    private TextView seekbarHint;
    private int totalDuration = 0;
    private boolean isLoop = true;
    private boolean isMix = false;
    private ImageButton btnLoop;
    private ImageButton btnMix;
    private TextView songName;
    private NotificationChannel mChannel;
    String CHANNEL_ID = "my_channel_01";
    RemoteViews remoteViews;
    Notification notification;
    NotificationManager mNotificationManager;

    private BroadcastReceiver receiveData = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String timeLineDuration = bundle.getString("timeLineDuration");
                if (timeLineDuration != null && !timeLineDuration.equals("null")) {
                    updateTimeline(timeLineDuration);
                    setSeekBarHint(Integer.valueOf(timeLineDuration));
                }
                String max = bundle.getString("maxDuration");
                if (max != null && !max.equals("null")) {
                    timeLine.setMax(Integer.valueOf(max));
                    totalDuration = Integer.valueOf(max);
                }
                String name = bundle.getString("name");
                if (name != null) {
                    songName.setText(name);
                    remoteViews.setTextViewText(R.id.songName, name);
                    mNotificationManager.notify(1, notification);
                }
                String pause = bundle.getString("pause");
                if (pause != null && pause.equals("request")) {
                    playPauseClick(null);
                }
                String next = bundle.getString("next");
                if (next != null && next.equals("request")) {
                    nextClick(null);
                }
                String previous = bundle.getString("previous");
                if (previous != null && previous.equals("request")) {
                    previousClick(null);
                }
            }
        }
    };

    public void setSeekBarHint(int currentDuration) {
        String total = convertDuration(totalDuration);
        String current = convertDuration(currentDuration);
        seekbarHint.setText(current + " / " + total);
    }

    private String convertDuration(long timeSec) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeSec),
                TimeUnit.MILLISECONDS.toSeconds(timeSec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeSec))
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectView();
        searchInput.clearFocus();
        initVariable();
        getSongListOnDevice();
        registerReceiver(receiveData, new IntentFilter("musicRequest"));
        mChannel = new NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_LOW);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notify);
        Notification.MediaStyle style = new Notification.MediaStyle();
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_compact_disc)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setStyle(style)
                .setChannelId(CHANNEL_ID)
                .setOngoing(true);
        notification = builder.build();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mChannel);
        showNoti();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, SoundService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SoundService.MusicBinder binder = (SoundService.MusicBinder) service;
            soundService = binder.getService();
            soundService.setList(songListInDevice);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void initVariable() {
        songListInDevice = new ArrayList<>();
        songListDisplay = new ArrayList<>();
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
    }

    private void connectView() {
        songView = findViewById(R.id.listSong);
        searchInput = findViewById(R.id.inputSearch);
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                String strSearch = editable.toString();
                songListDisplay.clear();
                for (Song s : songListInDevice) {
                    if (containsIgnoreCase(s.getTitle(), strSearch)) {
                        songListDisplay.add(s);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String strSearch = searchInput.getText().toString();
                songListDisplay.clear();
                for (Song song : songListInDevice) {
                    if (containsIgnoreCase(song.getTitle(), strSearch)) {
                        songListDisplay.add(song);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strSearch = searchInput.getText().toString();
                songListDisplay.clear();
                for (Song song : songListInDevice) {
                    if (containsIgnoreCase(song.getTitle(), strSearch)) {
                        songListDisplay.add(song);
                    }
                }
            }
        });
        playPause =  findViewById(R.id.btnPlayPause);
        timeLine = findViewById(R.id.timeLine);
        this.timeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Khi giá trị progress thay đổi.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
            }

            // Khi người dùng bắt đầu cử chỉ kéo thanh gạt.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // Khi người dùng kết thúc cử chỉ kéo thanh gạt.
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int duration = seekBar.getProgress();
                soundService.setDurationSong(duration);
            }
        });

        seekbarHint = findViewById(R.id.timePlay);
        btnLoop = findViewById(R.id.btnLoop);
        btnMix = findViewById(R.id.btnMix);
        songName = findViewById(R.id.songName);
    }

    public void getSongListOnDevice() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                long duration = musicCursor.getLong(durationColumn);
                String path = musicCursor.getString(pathColumn);
                songListInDevice.add(new Song(thisId, thisTitle, thisArtist, duration, path));
                songListDisplay.add(new Song(thisId, thisTitle, thisArtist, duration, path));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    public void search(View view) {
        searchInput.clearFocus();
        String strSearch = searchInput.getText().toString();
        songListDisplay.clear();
        for (Song s : songListInDevice) {
            if (containsIgnoreCase(s.getTitle(), strSearch)) {
                songListDisplay.add(s);
            }
        }
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
    }

    private boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().contains(subString.toLowerCase());
    }

    public void songPicked(View view) {
        playPause.setImageResource(R.drawable.ic_rounded_pause_button);
        remoteViews.setImageViewResource(R.id.playPauseNoti, R.drawable.ic_rounded_pause_button);
        soundService.setSong(Integer.parseInt(view.getTag().toString()));
        soundService.playSong();
    }

    public void updateTimeline(String duration) {
        timeLine.setProgress(Integer.valueOf(duration));
    }

    public void mixClick(View view) {
        if (isMix) {
            btnMix.setColorFilter(Color.rgb(58, 58, 58), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            btnMix.setColorFilter(Color.rgb(177, 23, 232), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        isMix = !isMix;
        soundService.toggleMix();
    }

    public void loopClick(View view) {
        if (isLoop) {
            btnLoop.setColorFilter(Color.rgb(58, 58, 58), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            btnLoop.setColorFilter(Color.rgb(177, 23, 232), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        isLoop = !isLoop;
        soundService.toggleLoop();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void playPauseClick(View view) {
//        showNoti();
        if (soundService.isPlay) {
            remoteViews.setImageViewResource(R.id.playPauseNoti, R.drawable.ic_play_circle);
            playPause.setImageResource(R.drawable.ic_play_circle);
            soundService.pauseSong();
        } else {
            remoteViews.setImageViewResource(R.id.playPauseNoti, R.drawable.ic_rounded_pause_button);
            playPause.setImageResource(R.drawable.ic_rounded_pause_button);
            soundService.resumeSong();
        }
        mNotificationManager.notify(1, notification);
    }

    public void nextClick(View view) {
        soundService.playNext();
    }

    public void previousClick(View view) {
        soundService.playPrevious();
    }

    private PendingIntent onNotiPauseClick(@IdRes int id) {
        Intent intent = new Intent("musicRequest");
        intent.putExtra("pause", "request");
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    private PendingIntent onNextClick(@IdRes int id) {
        Intent intent = new Intent("musicRequest");
        intent.putExtra("next", "request");
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    private PendingIntent onPreviousClick(@IdRes int id) {
        Intent intent = new Intent("musicRequest");
        intent.putExtra("previous", "request");
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNoti() {
        remoteViews.setImageViewResource(R.id.thumbnail, R.drawable.ic_compact_disc);
        remoteViews.setImageViewResource(R.id.pre, R.drawable.ic_previous_track);
        remoteViews.setImageViewResource(R.id.playPauseNoti, R.drawable.ic_play_circle);
        remoteViews.setImageViewResource(R.id.nex, R.drawable.ic_play_next_button);
        remoteViews.setTextViewText(R.id.songName, songListInDevice.size() == 0 ? "No Songs" : songListInDevice.get(0).getTitle());
        remoteViews.setOnClickPendingIntent(R.id.playPauseNoti, onNotiPauseClick(R.id.playPauseNoti));
        remoteViews.setOnClickPendingIntent(R.id.pre, onPreviousClick(R.id.pre));
        remoteViews.setOnClickPendingIntent(R.id.nex, onNextClick(R.id.nex));
        mNotificationManager.notify(1, notification);
    }

    private int positionSongOpenMenu = 0;

    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();

    public void openBottomMenu(View view) {
        positionSongOpenMenu = Integer.parseInt(view.getTag().toString());
        bottomSheetDialog.show(getSupportFragmentManager(), bottomSheetDialog.getTag());
    }

    public void deleteSong(View view) {
        Log.e("sdsdasdas", "sdasdsadasdasdsd");
        Song deleteSong = songListDisplay.get(positionSongOpenMenu);
        long currSong = deleteSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        getContentResolver().delete(trackUri, null, null);

        songListInDevice.clear();
        songListDisplay.clear();
        getSongListOnDevice();

        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);

        bottomSheetDialog.dismiss();
        Toast.makeText(this, "Đã xóa khỏi bộ nhớ", Toast.LENGTH_SHORT).show();
    }

    public void reloadList(View view) {
        songListInDevice.clear();
        songListDisplay.clear();
        getSongListOnDevice();
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
        Toast.makeText(this, "Đã tải lại danh sách", Toast.LENGTH_SHORT).show();
    }
}
