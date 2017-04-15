package com.zcc.mobileplayer.pager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zcc.mobileplayer.R;
import com.zcc.mobileplayer.activity.VideoPlayerActivity;
import com.zcc.mobileplayer.activity.VitamioVideoPlayerActivity;
import com.zcc.mobileplayer.adapter.VideoAndMusicPagerAdapter;
import com.zcc.mobileplayer.base.BasePage;
import com.zcc.mobileplayer.bean.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 朱超超 on 2017-03-26.
 * 作用：
 */
public class LocalVedioPager extends BasePage {
    private static final String TAG = LocalVedioPager.class.getSimpleName();
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
                videoAndMusicPagerAdapter = new VideoAndMusicPagerAdapter(mcontext,mediaItems,true);
                listView.setAdapter(videoAndMusicPagerAdapter);
                //把文本隐藏
                tv_nomedia.setVisibility(View.GONE);

                btn_nopermision.setVisibility(View.GONE);
            }else{
                //没有数据
                //文本显示
                if (msg.what == 0){
                    tv_nomedia.setVisibility(View.VISIBLE);
                }else if (msg.what == 1){

                    btn_nopermision.setVisibility(View.VISIBLE);
                }

            }


            //ProgressBar隐藏
            pb_loading.setVisibility(View.GONE);
        }
    };
    private boolean allowed = false;

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


//                //调用系统所有的播放器
//                Intent intent = new Intent();
//                intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//                mcontext.startActivity(intent);
                if(mediaItems != null && mediaItems.size()>0){
                    //调用自己创建的播放器
                    Intent intent = new Intent(mcontext, VideoPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mediaItems",mediaItems);
                    intent.putExtras(bundle);
                    intent.putExtra("position", position);
                    mcontext.startActivity(intent);
                }

            }
        });
        return view;
    }

    @Override
    public void initData() {
        Log.e(TAG, "本地视频数据初始化");
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
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

                String[] projection = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Video.Media.DURATION,//视频总时长
                        MediaStore.Video.Media.SIZE,//视频的文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST,//歌曲的演唱者
                };

               Cursor cursor  = contentResolver.query(uri, projection, null, null, null);
                if(cursor != null){
                    while (cursor.moveToNext()){
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);

                        String name = cursor.getString(0);//视频的名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);//视频的时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);//视频的文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);//视频的播放地址
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

//        return true;
    }

//    @SuppressLint("NewApi")
//    private void requestReadExternalPermission() {
//        if (ContextCompat.checkSelfPermission((Activity)mcontext,Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "READ permission IS NOT granted...");
//
//            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//
//                Log.d(TAG, "11111111111111");
//            } else {
//                // 0 是自己定义的请求coude
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
//                Log.d(TAG, "222222222222");
//            }
//        } else {
//            Log.d(TAG, "READ permission is granted...");
//        }
//    }

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
