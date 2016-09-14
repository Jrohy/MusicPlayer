package com.example.john.musicplayer.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;

import com.example.john.musicplayer.R;
import com.example.john.musicplayer.utils.MusicInfo;

/**
 *
 * Created by John on 2015/11/26.
 */
public class MusicLoader {
    private List<MusicInfo> musicList = new ArrayList<>();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MusicLoader(ContentResolver contentResolver , Context context) {
        //利用ContentResolver的query函数来查询数据，然后将得到的结果放到MusicInfo对象中，最后放到数组中
        Uri contentUri = Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                Media.TITLE,
                Media.ALBUM,
                Media._ID,
                Media.DURATION,
                Media.SIZE,
                Media.ARTIST,
                Media.DATA,
                Media.ALBUM_ID
        };
        String where = "mime_type in ('audio/mpeg','audio/x-ms-wma', 'audio/mp4') and is_music > 0 ";
        Cursor cursor = contentResolver.query(contentUri, projection, where, null,  MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        //cursor开始是放在-1的下标，需要移到最开始0下标
        if (cursor != null && cursor.moveToFirst()) {
            int displayNameCol = cursor.getColumnIndex(Media.TITLE);
            int albumCol = cursor.getColumnIndex(Media.ALBUM);
            int idCol = cursor.getColumnIndex(Media._ID);
            int durationCol = cursor.getColumnIndex(Media.DURATION);
            int sizeCol = cursor.getColumnIndex(Media.SIZE);
            int artistCol = cursor.getColumnIndex(Media.ARTIST);
            int urlCol = cursor.getColumnIndex(Media.DATA);
            int albumIDCol = cursor.getColumnIndex(Media.ALBUM_ID);
            do {
                String url = cursor.getString(urlCol);
                long id = cursor.getLong(idCol);
                int duration = cursor.getInt(durationCol);
                long size = cursor.getLong(sizeCol);
                String title = cursor.getString(displayNameCol);
                String album = cursor.getString(albumCol);
                String artist = cursor.getString(artistCol);
                if (album.equals("<unknown>")) {
                    album = "未知专辑";
                }
                if (artist.equals("<unknown>")) {
                    artist = "未知歌手";
                }

                MusicInfo musicInfo = new MusicInfo(id, title);
                musicInfo.setAlbum(album);
                musicInfo.setDuration(duration);
                musicInfo.setSize(size);
                musicInfo.setArtist(artist);
                musicInfo.setUrl(url);

                int albumID = cursor.getInt(albumIDCol);
                String COVERURI = "content://media/external/audio/albums";
                Cursor coverCursor = contentResolver.query(Uri.parse(COVERURI + "/" + Integer.toString(albumID)),
                        new String[]{"album_art"}, null, null, null);
                if (coverCursor != null) {
                    coverCursor.moveToFirst();
                }
                String coverUri = coverCursor != null ? coverCursor.getString(0) : null;
                if (coverUri == null) {
                    //处理专辑无封面的情况
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.default_cover, null);
                    musicInfo.setCover(Bitmap.createScaledBitmap(bitmapDrawable != null ? bitmapDrawable.getBitmap() : null,360, 360, true));

                } else {
                    musicInfo.setCover(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(coverUri),360,360,true));
                }
                musicList.add(musicInfo);
                if (coverCursor != null) {
                    coverCursor.close();
                }

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public List<MusicInfo> getMusicList(){
        return musicList;
    }

}
