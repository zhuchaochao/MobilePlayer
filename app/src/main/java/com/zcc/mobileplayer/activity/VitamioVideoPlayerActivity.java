package com.zcc.mobileplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zcc.mobileplayer.R;
import com.zcc.mobileplayer.bean.MediaItem;
import com.zcc.mobileplayer.utils.Utils;
import com.zcc.mobileplayer.view.VitamioVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * 作者：朱超超 on 2017-04-01
 * 作用：基于vitamio万能播放器
 */
public class VitamioVideoPlayerActivity extends Activity implements View.OnClickListener {
    private static final int PROGRESS = 1;
    private static final int HIDEMEDIACONTROLL = 2;
    private static final int NOT_FULL_SCREEN = 3;
    private static final int FULL_SCREEN = 4;
    private static final int SHOW_SPEED = 5;
    private VitamioVideoView  vedioView;
    private Uri uri;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichPlayer;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private RelativeLayout mediaController;
    private LinearLayout ll_vedioBuffer;
    private TextView tvSpeed;
    private RelativeLayout rl_retreat_forward;
    private TextView tv_retreat_forward;
    private TextView tv_rf_change_position;
    private TextView tv_rf_current_time;
    private TextView tv_rf_total_time;
    private Utils utils;

    private MyReceiver myReceiver;

    private ArrayList<MediaItem> mediaItems;

    private GestureDetector detector;

    private boolean isShowingMediaController = false;
    /**
     * 播放mediaItems的位置
     */
    private int position;

    /**
     * 视频的宽
     */
    private int videoWidth = 0;
    /**
     * 视频的高
     */
    private int videoHeight = 0;
    /**
     * 是否全屏
     */
    private boolean isFullScreen = false;
    /**
     * 屏幕的宽
     */
    private int widthSpecSize = 0;
    /**
     * 屏幕的高
     */
    private int heightSpecSize = 0;
    /**
     * 调节音量
     */
    private AudioManager am;
    /**
     * 当前音量
     */
    private int currentVoice;
    /**
     * 最大音量
     */
    private int maxVoice;
    /**
     * 是否是静音
     */
    private boolean isMute = false;
    /**
     * 滑动改变音量起始点
     */
    private float startY;
    /**
     * 滑动改变音量终点
     */
    private float endY;
    /**
     * 横向滑动快进快退时起始X值
     */
    private float startX;
    /**
     * 横向滑动快进快退时终点X值
     */
    private float endX;
    /**
     * 滑动时，屏幕的高
     */
    private int move_height;
    /**
     * 滑动时，屏幕的宽
     */
    private int move_width;
    /**
     * 触碰屏幕时的音量
     */
    private int move_voice;
    /**
     * 触碰屏幕时的视频播放位置
     */
    private int move_position;
    /**
     * 是否用系统的监听卡
     */
    private boolean isUseSystem =false;
    /**
     * 上次播放位置
     */
    private int prePosition;
    /**
     * 改变的播放进度
     */
    private float changePosition = 0;
    /**
     * 音量监听服务
     */
    private MyVolumeReceiver mVolumeReceiver;
    private Vibrator vibrator;

    private  LinearLayout ll_loading;
    private  TextView tv_lodingSpeed;
    /**
     * 是否播放的是网络视频
     */
    //private boolean isNet = false;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_SPEED:
                    String speed = utils.getNetSpeed(VitamioVideoPlayerActivity.this);

                    tv_lodingSpeed.setText("加载中..."+speed);
                    tvSpeed.setText(speed);
                    //每2秒更新一次
                    removeMessages(SHOW_SPEED);
                    sendEmptyMessageDelayed(SHOW_SPEED, 2000);

                    break;
                case PROGRESS:
                    //得到当前进度
                    int currentPosition = (int) vedioView.getCurrentPosition();
                    //设置进度条进度
                    seekbarVideo.setProgress(currentPosition);
                    //设置文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //设置系统时间
                    tvSystemTime.setText(getSystemTime());
//                    if(isNet){
//                        int buffer = vedioView.getBufferPercentage();//0~100;
//                        int totalBuffer = buffer*seekbarVideo.getMax();
//                        int secondaryProgress = totalBuffer/100;
//                        seekbarVideo.setSecondaryProgress(secondaryProgress);
//                    }else{
//                        //不是网络视频没有缓冲
//                        seekbarVideo.setSecondaryProgress(0);
//                    }

                    if(!isUseSystem && vedioView.isPlaying()){
                        int buffer = currentPosition - prePosition;
                        if(buffer<500){
                            //卡了
                            ll_vedioBuffer.setVisibility(View.VISIBLE);
                        }else{
                            //不卡了
                            ll_vedioBuffer.setVisibility(View.GONE);
                        }
                        prePosition = currentPosition;
                    }

                    //每秒更新一次
                    handler.removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
                case HIDEMEDIACONTROLL:
                    if(isShowingMediaController){
                        hideMediaController();
                    }else{
                        removeMessages(HIDEMEDIACONTROLL);
                    }
                    break;
            }
        }
    };
    private int finalPosition = 0;


    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViews();
        initData();
        initListener();
    }

    private void initListener() {
        //视频准备情况监听
        vedioView.setOnPreparedListener(new myOnPreparedListener());
        //播放出错监听
        vedioView.setOnErrorListener(new myOnErrorListener());
        //播放完成监听
        vedioView.setOnCompletionListener(new myOnCompletionListener());
        //设置视频拖动
        seekbarVideo.setOnSeekBarChangeListener(new VideoSeekBarChangeListener());
        //设置音量seekbar
        seekbarVoice.setOnSeekBarChangeListener(new VoiceSeekBarChangeListener());

        if(isUseSystem){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                vedioView.setOnInfoListener(new myOnInfoListener());
            }
        }

    }
    class myOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    //视频播放卡了
                    ll_vedioBuffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    //视频播放不卡了
                    ll_vedioBuffer.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }
    private void initData() {
        //得到屏幕的宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthSpecSize = displayMetrics.widthPixels;
        heightSpecSize= displayMetrics.heightPixels;

        utils = new Utils();
        //得到uri
        uri = getIntent().getData();
        //得到mediaItems
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("mediaItems");
        position = getIntent().getIntExtra("position",0);

        if(mediaItems != null && mediaItems.size()>0){
            //isNet = utils.isNetUri(mediaItems.get(position).getData());
            vedioView.setVideoPath(mediaItems.get(position).getData());
            tvName.setText(mediaItems.get(position).getName());//设置视频的名称
        }else if(uri != null){
            //isNet = utils.isNetUri(uri.toString());
            vedioView.setVideoURI(uri);
            tvName.setText(uri.toString());//设置视频的名称
        }else {
            Toast.makeText(VitamioVideoPlayerActivity.this,"没有任何数据",Toast.LENGTH_SHORT).show();
        }
        setButtonState();


        //监听手势
        detector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(isShowingMediaController){
                    hideMediaController();
                    handler.removeMessages(HIDEMEDIACONTROLL);
                }else{
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDEMEDIACONTROLL,4000);
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                changeVideoScreen();
                return super.onDoubleTap(e);
            }
        });
        /**
         * 得到系统音量参数，设置音量seekbar参数
         */
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekbarVoice.setProgress(currentVoice);
        seekbarVoice.setMax(maxVoice);
        //监听系统音量变化和电量变化
        myRegisterReceiver();

        handler.sendEmptyMessage(SHOW_SPEED);
    }

    private void showMediaController() {
        mediaController.setVisibility(View.VISIBLE);
        isShowingMediaController = true;
    }

    private void hideMediaController() {
        mediaController.setVisibility(View.GONE);
        isShowingMediaController = false;
    }

    private void findViews() {
        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_vitamio_video_player);
        mediaController = (RelativeLayout) findViewById(R.id.media_controller);
        vedioView = (VitamioVideoView ) findViewById(R.id.videoview);
        tvName = (TextView)findViewById( R.id.tv_name );
        ivBattery = (ImageView)findViewById( R.id.iv_battery );
        tvSystemTime = (TextView)findViewById( R.id.tv_system_time );
        btnVoice = (Button)findViewById( R.id.btn_voice );
        seekbarVoice = (SeekBar)findViewById( R.id.seekbar_voice );
        btnSwichPlayer = (Button)findViewById( R.id.btn_swich_player );
        tvCurrentTime = (TextView)findViewById( R.id.tv_current_time );
        seekbarVideo = (SeekBar)findViewById( R.id.seekbar_video );
        tvDuration = (TextView)findViewById( R.id.tv_duration );
        btnExit = (Button)findViewById( R.id.btn_exit );
        btnVideoPre = (Button)findViewById( R.id.btn_video_pre );
        btnVideoStartPause = (Button)findViewById( R.id.btn_video_start_pause );
        btnVideoNext = (Button)findViewById( R.id.btn_video_next );
        btnVideoSiwchScreen = (Button)findViewById( R.id.btn_video_siwch_screen );
        ll_vedioBuffer = (LinearLayout) findViewById(R.id.ll_vedioBuffer);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loding);
        tv_lodingSpeed = (TextView) findViewById(R.id.tv_lodingSpeed);
        rl_retreat_forward = (RelativeLayout) findViewById(R.id.rl_retreat_forward);
        tv_retreat_forward = (TextView) findViewById(R.id.tv_retreat_forward);
        tv_rf_change_position = (TextView) findViewById(R.id.tv_rf_change_position);
        tv_rf_current_time = (TextView) findViewById(R.id.tv_rf_current_time);
        tv_rf_total_time = (TextView) findViewById(R.id.tv_rf_total_time);

        btnVoice.setOnClickListener( this );
        btnSwichPlayer.setOnClickListener( this );
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener( this );
        btnVideoStartPause.setOnClickListener( this );
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if ( v == btnVoice ) {
            isMute=!isMute;
            updateVoice(currentVoice,isMute);
        } else if ( v == btnSwichPlayer ) {
            showSwichPlayerDialog();
        } else if ( v == btnExit ) {
            finish();
        } else if ( v == btnVideoPre ) {
            preVideo();

        } else if ( v == btnVideoStartPause ) {

            startAndPause();

        } else if ( v == btnVideoNext ) {
            nextVideo();

        } else if ( v == btnVideoSiwchScreen ) {
            changeVideoScreen();
        }
    }
    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("万能播放器提醒您");
        builder.setMessage("当您播放一个视频，有花屏的时候，可以尝试使用系统播放器播放，确定切换");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVideoPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void startVideoPlayer() {

        if(vedioView != null){
            vedioView.stopPlayback();
        }
        Intent intent = new Intent(VitamioVideoPlayerActivity.this, VideoPlayerActivity.class);
        if(mediaItems != null && mediaItems.size()>0){
            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaItems", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        }else if(uri != null){
            intent.setDataAndType(uri, "video/*");
        }
        Log.e("ssss--->", "启动vitamio");
        startActivity(intent);
        finish();
    }

    private void changeVideoScreen() {
        if(isFullScreen){
            //目前状态是全屏，变为不是全屏
            Log.e("TAG----->","变默认");
            changeVideoScreenType(NOT_FULL_SCREEN);
            //改按钮状态btn_video_siwch_screen_full_selector
            btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
            //设置状态为不是全屏
             isFullScreen = false;
        }else{
            //目前状态不是全屏，变为全屏
            Log.e("TAG----->","变全屏");
            changeVideoScreenType(FULL_SCREEN);
            //该按钮状态
            btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
            //设置状态为不是全屏
            isFullScreen = true;
        }
    }

    private void changeVideoScreenType(int type){
        switch (type){
            case FULL_SCREEN:
                changeVideoScreenSize(widthSpecSize,heightSpecSize);
                break;
            case NOT_FULL_SCREEN:
                int width = widthSpecSize;
                int height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if ( videoWidth * height  < width * videoHeight ) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * videoWidth / videoHeight;
                } else if ( videoWidth * height  > width * videoHeight ) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * videoHeight / videoWidth;
                }
                changeVideoScreenSize(width, height);
                break;
        }
    }
    private void changeVideoScreenSize(int width,int height) {
        vedioView.setVideoSize(width,height);
    }

    private void startAndPause() {
        if(vedioView.isPlaying()){
            //暂停播放
            vedioView.pause();
            //设置为播放图片
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        }else{
            //播放
            vedioView.start();
            //设置为暂停图片
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    private void nextVideo() {
        if(mediaItems != null && mediaItems.size()>0){
            position++;
            if(position <mediaItems.size()){
                //isNet = utils.isNetUri(mediaItems.get(position).getData());
                vedioView.setVideoPath(mediaItems.get(position).getData());
                tvName.setText(mediaItems.get(position).getName());
                ll_loading.setVisibility(View.VISIBLE);
                setButtonState();
            }
        }else if(uri != null){
            setButtonState();
        }
    }

    private void preVideo() {
        if(mediaItems != null && mediaItems.size()>0){
            position--;
            if(position >=0){
                //isNet = utils.isNetUri(mediaItems.get(position).getData());
                vedioView.setVideoPath(mediaItems.get(position).getData());
                tvName.setText(mediaItems.get(position).getName());
                ll_loading.setVisibility(View.VISIBLE);
                setButtonState();
            }else if(uri != null){
                setButtonState();
            }
        }
    }

    private void setButtonState() {
        if(mediaItems != null && mediaItems.size()>0){
            if(mediaItems.size() == 1){
                setEnable(false);
            }else{
                if(position == 0){
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                }else if(position == mediaItems.size()-1) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                }else if(uri != null){
                    setEnable(true);
                }
            }

        }else {
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            //两个按钮设置灰色
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//0~100;
            setBattery(level);//主线程
            //context.unregisterReceiver(this);
        }
    }

    private void setBattery(int level) {
            if (level <= 0) {
                ivBattery.setImageResource(R.drawable.ic_battery_0);
            } else if (level <= 10) {
                ivBattery.setImageResource(R.drawable.ic_battery_10);
            } else if (level <= 20) {
                ivBattery.setImageResource(R.drawable.ic_battery_20);
            } else if (level <= 40) {
                ivBattery.setImageResource(R.drawable.ic_battery_40);
            } else if (level <= 60) {
                ivBattery.setImageResource(R.drawable.ic_battery_60);
            } else if (level <= 80) {
                ivBattery.setImageResource(R.drawable.ic_battery_80);
            } else if (level <= 100) {
                ivBattery.setImageResource(R.drawable.ic_battery_100);
            } else {
                ivBattery.setImageResource(R.drawable.ic_battery_100);
            }
    }

    class  myOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            vedioView.start();
            int duration = (int) vedioView.getDuration();
            seekbarVideo.setMax(duration);
            tv_rf_total_time.setText(utils.stringForTime(duration));
            tvDuration.setText(utils.stringForTime(duration));
            handler.sendEmptyMessage(PROGRESS);

            videoHeight = mp.getVideoHeight();
            videoWidth = mp.getVideoWidth();
            //设置为暂停图片
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
            //设置缓存条
            mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {

                   seekbarVideo.setSecondaryProgress(percent*seekbarVideo.getMax()/100);
                }
            });
            ll_loading.setVisibility(View.GONE);
        }
    }
    class  myOnErrorListener implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            showErrorDialog();
            return true;
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VitamioVideoPlayerActivity.this);
        builder.setTitle("提示");
        builder.setMessage("抱歉，无法播放该视频！！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    class  myOnCompletionListener implements MediaPlayer.OnCompletionListener{
        @Override
        public void onCompletion(MediaPlayer mp) {
            nextVideo();
        }
    }

     class VideoSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
         /**
          * 当手指滑动seekbar时回调
          * @param seekBar
          * @param progress
          * @param fromUser  如果是用户引起的是true
          */
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
             if(fromUser){
                 vedioView.seekTo(progress);
             }
         }

         /**
          * 当手指触碰seekbar时回调
          * @param seekBar
          */
         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {

         }

         /**
          * 当手指离开seekbar时回调
          * @param seekBar
          */
         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {

         }
     }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if(myReceiver != null){
            unregisterReceiver(myReceiver);
        }
        if(mVolumeReceiver != null){
            unregisterReceiver(mVolumeReceiver);
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //记录起始Y
                startY = event.getY();
                //记录起始X
                startX = event.getX();
                if(heightSpecSize>widthSpecSize){
                    //竖屏
                    move_height = heightSpecSize;
                    move_width = widthSpecSize;
                }else{
                    //横屏
                    move_height = widthSpecSize;
                    move_width = heightSpecSize;
                }
                //move_height = Math.min(heightSpecSize, widthSpecSize);
                move_voice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                move_position = (int) vedioView.getCurrentPosition();
                tv_rf_current_time.setText(utils.stringForTime(move_position)+"/");
                handler.removeMessages(HIDEMEDIACONTROLL);
                break;
            case MotionEvent.ACTION_MOVE:
                //记录结束Y
                endY = event.getY();
                float distanceY = startY - endY;
                //记录结束X
                endX = event.getX();
                float distanceX = endX - startX;
                if(Math.abs(distanceX) > Math.abs(distanceY)){
                    //横滑
                    //显示快进快退
                    final double FLING_MIN_DISTANCEX = 0.5;
                    if (Math.abs(distanceX) > FLING_MIN_DISTANCEX) {
                        rl_retreat_forward.setVisibility(View.VISIBLE);
                        //移动的距离 ：总宽 = 改变的播放进度 ：总进度
                        changePosition = (distanceX/move_width)*seekbarVideo.getMax();
                        finalPosition = (int) Math.min(Math.max(0,(changePosition+move_position)),seekbarVideo.getMax());
                        if(changePosition>0){
                            tv_retreat_forward.setText("+");
                        }else if(changePosition == 0){
                            tv_retreat_forward.setText(" ");
                        }else {
                            tv_retreat_forward.setText("-");
                        }
                        tv_rf_change_position.setText(utils.stringForTime((int) Math.abs(changePosition)));
                        tv_rf_current_time.setText(utils.stringForTime(finalPosition)+"/");
                    }

                }else{
                    //竖滑
                    if(endX < move_width/2){
                        //左边滑动调节屏幕亮度
                        //左边屏幕-调节亮度
                        final double FLING_MIN_DISTANCE = 0.5;
                        final double FLING_MIN_VELOCITY = 0.5;
                        if (distanceY > FLING_MIN_DISTANCE
                                && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                            Log.e("啦啦啦啦啦啦啦","变亮");
                            setBrightness(20);
                        }
                        if (distanceY < FLING_MIN_DISTANCE
                                && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                            Log.e("啦啦啦啦啦啦啦","变暗");
                            setBrightness(-20);
                        }
                    }else {
                        //右边滑动调节声量
                        //移动的距离 ：总高 = 改变的音量 ：总音量
                        float changeVoice =  (distanceY/move_height)*maxVoice;
                        int finalVoice = (int) Math.min(Math.max(0,changeVoice+move_voice), maxVoice);
                        if(changeVoice != 0){
                            isMute = false;
                            updateVoice(finalVoice,isMute);
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                tv_rf_change_position.setText("00:00");
                tv_retreat_forward.setText(" ");
                if(changePosition != 0){
                    vedioView.seekTo(finalPosition);
                    changePosition=0;
                }
                rl_retreat_forward.setVisibility(View.GONE);
                handler.sendEmptyMessageDelayed(HIDEMEDIACONTROLL,4000);
                break;

        }
        return super.onTouchEvent(event);
    }

    class VoiceSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                if(progress >0){
                    isMute = false;
                }else {
                    isMute = true;
                }
                updateVoice(progress,false);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDEMEDIACONTROLL);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDEMEDIACONTROLL,4000);
        }
    }

    private void updateVoice(int progress,boolean isMute) {
        if(isMute){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            seekbarVoice.setProgress(0);
        }else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
        }

    }
    /**
     * 注册当音量发生变化时接收的广播
     */
    private void myRegisterReceiver(){
        //注册音量变化广播
        mVolumeReceiver = new MyVolumeReceiver() ;
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.media.VOLUME_CHANGED_ACTION") ;
        registerReceiver(mVolumeReceiver, filter) ;


        //注册电量广播
        myReceiver = new MyReceiver();
        IntentFilter intentFiler = new IntentFilter();
        //当电量变化的时候发这个广播
        intentFiler.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(myReceiver, intentFiler);
    }

    /*
     *
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // if (lp.screenBrightness <= 0.1) {
        // return;
        // }
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        }
//        Log.e(TAG, "lp.screenBrightness= " + lp.screenBrightness);
        getWindow().setAttributes(lp);
    }
    /**
     * 处理音量变化时的界面显示，相比监听keyDown，这个服务可以同步seekBarVoice和系统声量的seekbar，而keydown不可以
     * @author long
     */
    private class MyVolumeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){
                int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC) ;// 当前的媒体音量
                seekbarVoice.setProgress(currVolume) ;
                if(currVolume == 0){
                    isMute = true;
                }else {
                    isMute = false;
                }
                updateVoice(currVolume,isMute);
            }
        }
    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
//            currentVoice--;
//            if(currentVoice <= 0){
//                isMute = true;
//                currentVoice = 0;
//            }else{
//                isMute = false;
//            }
//            updateVoice(currentVoice,isMute);
//        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
//            currentVoice++;
//            isMute = false;
//            if(currentVoice>=maxVoice){
//                currentVoice = maxVoice;
//            }
//            updateVoice(currentVoice,isMute);
//        }
//        return true;
//    }
}
