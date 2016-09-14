package com.example.john.musicplayer.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.john.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by John on 2015/11/30.
 */
public class MusicListAdapter extends BaseAdapter {

    private List<MusicInfo> musicList = new ArrayList<>();

    private Context context;

    public MusicListAdapter(Context context, List<MusicInfo> musicList) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.musicList = musicList;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, null);
        viewHolder.iv_cover = (ImageView) convertView.findViewById(R.id.iv_cover);
        viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        viewHolder.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);

        viewHolder.tv_artist.setText(musicList.get(position).getArtist());
        viewHolder.tv_title.setText(musicList.get(position).getTitle());
        viewHolder.iv_cover.setImageBitmap(musicList.get(position).getCover());
        convertView.setTag(viewHolder);
        return convertView;
    }

    private class ViewHolder {
        //所有控件对象引用
        public ImageView iv_cover;    //专辑图片
        public TextView tv_title;     //音乐标题
        public TextView tv_artist;    //音乐艺术家
    }
}
