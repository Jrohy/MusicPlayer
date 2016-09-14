package com.example.john.musicplayer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.john.musicplayer.utils.PlayerMsg;

import java.io.IOException;

public class PlayService extends Service implements MediaPlayer.OnCompletionListener {

    private  MediaPlayer mediaPlayer;

    private String artist;

    private String title;

    private NotificationManager manager;

    private MainActivityReceiver mainActivityReceiver;

    public final ThreadLocal<Handler> handler = new ThreadLocal<Handler>() {
        @Override
        protected Handler initialValue() {
            return new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        if (mediaPlayer != null) {
                            Intent intent = new Intent();
                            intent.setAction(PlayerMsg.UPDATE_SEEKBAR_BROADCAST);
                            intent.putExtra("currentTime", mediaPlayer.getCurrentPosition());
                            sendBroadcast(intent); // 给PlayerActivity发送广播
                            handler.get().sendEmptyMessageDelayed(1, 1000);
                        }

                    }
                }
            };
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mainActivityReceiver = new MainActivityReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerMsg.PLAY);
        filter.addAction(PlayerMsg.PAUSE);
        filter.addAction(PlayerMsg.EXIT);
        filter.addAction(PlayerMsg.SEEK);
        filter.addAction(PlayerMsg.SELECT);
        registerReceiver(mainActivityReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY; // 当本服务被系统关闭后，无需重启启动
    }

    @Override
    public void onDestroy() {
        if (mainActivityReceiver != null) unregisterReceiver(mainActivityReceiver);
        super.onDestroy();
    }

    private void playMusic(String path) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            handler.get().sendEmptyMessage(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateNotification() {
        Notification notification = new Notification.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), 0)).build();
        manager.notify(1, notification);
        startForeground(1, notification);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendBroadcast(new Intent(PlayerMsg.COMPLETE_MUSIC_BROADCAST));
    }

    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case PlayerMsg.SELECT:
                    playMusic(intent.getStringExtra("musicUrl"));
                    artist = intent.getStringExtra("artist");
                    title = intent.getStringExtra("title");
                    updateNotification();
                    break;
                case PlayerMsg.PLAY:
                    mediaPlayer.start();
                    break;
                case PlayerMsg.PAUSE:
                    mediaPlayer.pause();
                    break;
                case PlayerMsg.EXIT:
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        stopForeground(true);
                    }
                    handler.get().removeMessages(1);
                    stopSelf();
                case PlayerMsg.SEEK:
                    mediaPlayer.seekTo(intent.getIntExtra("seekbar_progress", 0));
                    break;
            }
        }
    }
}
