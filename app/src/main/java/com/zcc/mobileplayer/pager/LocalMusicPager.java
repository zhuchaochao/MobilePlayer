package com.zcc.mobileplayer.pager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zcc.mobileplayer.R;
import com.zcc.mobileplayer.activity.MusicPlayerActivity;
import com.zcc.mobileplayer.activity.VideoPlayerActivity;
import com.zcc.mobileplayer.adapter.VideoAndMusicPagerAdapter;
import com.zcc.mobileplayer.base.BasePage;
import com.zcc.mobileplayer.bean.MediaItem;

import java.util.ArrayList;

/**
 * Created by 朱超超 on 2017-03-26.
 * 作用：
 */
public class LocalMusicPager extends BasePage {
    private static final String TAG = LocalMusicPager.class.getSimpleName();
    private ListView listView;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private Button btn_nopermision;
    private ArrayList<MediaItem> mediaItems;
    private VideoAndMusicPagerAdapter videoAndMusicPagerAdapter;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mediaItems != null && mediaItems.size() >0){
                //有数据
                //设置适配器
                videoAndMusicPagerAdapter = new VideoAndMusicPagerAdapter(mcontext,mediaItems,false);
                listView.setAdapter(videoAndMusicPagerAdapter);
                //把文本隐藏
                tv_nomedia.setVisibility(View.GONE);

                btn_nopermision.setVisibility(View.GONE);
            }else{
                //没有数据
                //文本显示
                if (msg.what == 0){
                    tv_nomedia.setVisibility(View.VISIBLE);
                    tv_nomedia.setText("没有找到音乐...");
                }else if (msg.what == 1){
                    btn_nopermision.setVisibility(View.VISIBLE);
                }

            }


            //ProgressBar隐藏
            pb_loading.setVisibility(View.GONE);
        }
    };

    @Override
    public View initView() {
        View view = View.inflate(mcontext, R.layout.vedio_music_pager,null);
        listView = (ListView) view.findViewById(R.id.listview);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        btn_nopermision = (Button) view.findViewById(R.id.btn_nopermision);
        btn_nopermision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if(mediaItems != null && mediaItems.size()>0){
                    //调用自己创建的播放器
                    Intent intent = new Intent(mcontext, MusicPlayerActivity.class);
                    intent.putExtra("position", position);
                    mcontext.startActivity(intent);
                }

            }
        });
        return view;
    }

    @Override
    public void initData() {
        Log.e(TAG, "本地音乐数据初始化");
        super.initData();
        isGrantExternalRW( mcontext);
    }

    private void getDataFromLocal() {
        new Thread(){
            @Override
            public void run() {
                super.run();

                mediaItems = new ArrayList();

                ContentResolver contentResolver = mcontext.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                String[] projection = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//音频文件在sdcard的名称
                        MediaStore.Audio.Media.DURATION,//音频总时长
                        MediaStore.Audio.Media.SIZE,//音频的文件大小
                        MediaStore.Audio.Media.DATA,//音频的绝对地址
                        MediaStore.Audio.Media.ARTIST,//歌曲的演唱者
                };

                Cursor cursor  = contentResolver.query(uri, projection, null, null, null);
                if(cursor != null){
                    while (cursor.moveToNext()){
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);

                        String name = cursor.getString(0);//音频的名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);//音频的时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);//音频的文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);//音频的播放地址
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);//艺术家
                        mediaItem.setArtist(artist);
                    }
                    cursor.close();
                }
                //Handler发消息
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     * @param activity
     * @return
     */
    public  void isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ permission IS NOT granted...");

                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "11111111111111");
                    if (mcontext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        handler.sendEmptyMessage(1);
                    }
                } else {
                    // 0 是自己定义的请求coude
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    Log.d(TAG, "222222222222");
                }
            } else {
                getDataFromLocal();
            }

        }else {
            getDataFromLocal();
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "requestCode=" + requestCode + "; --->" + permissions.toString()
                + "; grantResult=" + grantResults.toString());
        switch (requestCode) {
            case 0: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    // request successfully, handle you transactions
                    getDataFromLocal();

                } else {
                    handler.sendEmptyMessage(1);
                    // permission denied
                    // request failed
                }

                return;
            }
            default:
                break;

        }
    }
}
