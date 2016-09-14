package com.example.john.musicplayer;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.john.musicplayer.utils.MusicInfo;
import com.example.john.musicplayer.utils.MusicListAdapter;
import com.example.john.musicplayer.utils.MusicLoader;
import com.example.john.musicplayer.utils.PlayerMsg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private List<MusicInfo> musicList = new ArrayList<>();

    private  ImageButton playButton;

    private TextView startText, endText;

    private  ListView listView;

    private  SeekBar seekBar;

    private ServiceReceiver serviceReceiver;

    private final String TIMEFORMAT = "mm:ss";

    private boolean isFirstPlay;

    private boolean isPlaying;

    private int currentIndex;

    private int progress;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listview);
        playButton = (ImageButton) findViewById(R.id.play_arrow_button);
        startText = (TextView) findViewById(R.id.startText);
        endText = (TextView) findViewById(R.id.endText);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        editor = sharedPreferences.edit();

        serviceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerMsg.COMPLETE_MUSIC_BROADCAST);
        filter.addAction(PlayerMsg.UPDATE_SEEKBAR_BROADCAST);
        registerReceiver(serviceReceiver, filter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentIndex = position;
                isFirstPlay = false;
                switchMusic();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请READ_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PlayerMsg.READ_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            musicList = new MusicLoader(getContentResolver(), getApplicationContext()).getMusicList();
            listView.setAdapter(new MusicListAdapter(this, musicList));
        }
        startService(new Intent(this, PlayService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFirstPlay = sharedPreferences.getBoolean("isFirstPlay", true);
        isPlaying = sharedPreferences.getBoolean("isPlaying", false);
        currentIndex = sharedPreferences.getInt("currentIndex", 0);
        progress = sharedPreferences.getInt("progress", 0);
        if (isPlaying) {
            playButton.setImageResource(R.mipmap.ic_pause_white_24dp);
        } else {
            playButton.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
        }
        seekBar.setMax(musicList.get(currentIndex).getDuration());
        seekBar.setProgress(progress);
        startText.setText(new SimpleDateFormat(TIMEFORMAT, Locale.getDefault()).format(progress));
        endText.setText(new SimpleDateFormat(TIMEFORMAT, Locale.getDefault()).format(musicList.get(currentIndex).getDuration()));
        Log.d("debug", "大家好,这里是Resume 的progress: " + progress);
    }

    @Override
    protected void onDestroy() {
        if (serviceReceiver != null) unregisterReceiver(serviceReceiver);
        editor.putBoolean("isFirstPlay", isFirstPlay);
        editor.putBoolean("isPlaying", isPlaying);
        editor.putInt("currentIndex", currentIndex);
        editor.putInt("progress", progress);
        editor.commit();
        Log.d("debug", "大家好,这里是Destroy的progress: " + progress);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PlayerMsg.READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                musicList = new MusicLoader(getContentResolver(), getApplicationContext()).getMusicList();
                listView.setAdapter(new MusicListAdapter(this, musicList));

            } else {
                // Permission Denied
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent = new Intent();
        intent.setPackage(getPackageName());

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            isPlaying = false;
            isFirstPlay = true;
            sendBroadcast(intent.setAction(PlayerMsg.EXIT));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void musicControl(View view) {
        Intent intent = new Intent();
        intent.setPackage(getPackageName());
        switch (view.getId()) {
            case R.id.play_arrow_button :{
                if (isFirstPlay){
                    switchMusic();
                    intent.setAction(PlayerMsg.SEEK);
                    intent.putExtra("seekbar_progress", progress);
                    sendBroadcast(intent);
                    isFirstPlay = false;
                }
                else {
                    if (isPlaying) {
                        playButton.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
                        intent.setAction(PlayerMsg.PAUSE);
                    } else {
                        playButton.setImageResource(R.mipmap.ic_pause_white_24dp);
                        intent.setAction(PlayerMsg.PLAY);
                    }
                    isPlaying = !isPlaying;
                    sendBroadcast(intent);
                }
            } break;

            case R.id.previous_button: {
                if (--currentIndex < 0) currentIndex = musicList.size() - 1;
                isFirstPlay = false;
                switchMusic();
            } break;
            case R.id.next_button: {
                if (++currentIndex >= musicList.size()) {
                    currentIndex = 0;
                }
                isFirstPlay = false;
                switchMusic();
            } break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        startText.setText(new SimpleDateFormat(TIMEFORMAT, Locale.getDefault()).format(progress));
        if (!isFirstPlay) {
            this.progress = progress;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Intent intent = new Intent();
        intent.setPackage(getPackageName());
        intent.setAction(PlayerMsg.SEEK);
        intent.putExtra("seekbar_progress", seekBar.getProgress());
        sendBroadcast(intent);
    }

    private void switchMusic() {
        seekBar.setMax(musicList.get(currentIndex).getDuration());
        if (!isFirstPlay) {
            seekBar.setProgress(0);
        }
        endText.setText(new SimpleDateFormat(TIMEFORMAT, Locale.getDefault()).format(musicList.get(currentIndex).getDuration()));
        playButton.setImageResource(R.mipmap.ic_pause_white_24dp);
        Intent intent = new Intent();
        intent.setPackage(getPackageName());
        intent.putExtra("title", musicList.get(currentIndex).getTitle());
        intent.putExtra("artist", musicList.get(currentIndex).getArtist());
        intent.putExtra("musicUrl", musicList.get(currentIndex).getUrl());
        intent.setAction(PlayerMsg.SELECT);
        sendBroadcast(intent);
        isPlaying = true;
    }

    public class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case PlayerMsg.UPDATE_SEEKBAR_BROADCAST:
                    seekBar.setProgress(intent.getIntExtra("currentTime", -1));
                    break;
                case PlayerMsg.COMPLETE_MUSIC_BROADCAST:
                    if (!isFirstPlay) {
                        if (++currentIndex >= musicList.size()) currentIndex = 0;
                        switchMusic();
                    }
                    break;
            }
        }
    }


}
