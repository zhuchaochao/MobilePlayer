package com.zcc.mobileplayer.activity;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.zcc.mobileplayer.R;
import com.zcc.mobileplayer.base.BasePage;
import com.zcc.mobileplayer.pager.LocalMusicPager;
import com.zcc.mobileplayer.pager.LocalVedioPager;
import com.zcc.mobileplayer.pager.NetMusicPager;
import com.zcc.mobileplayer.pager.NetVedioPager;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {
    private FrameLayout fl_main;
    private RadioGroup rg_main;
    private ArrayList<BasePage> basePages;
    private int position = 0;
    private Fragment tempPager;
    private boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPages();
        initListener();
    }

    private void initListener() {
        rg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_local_vedio://本地视频
                        position = 0;
                        break;
                    case R.id.rb_local_music://本地音乐
                        position = 1;
                        break;
                    case R.id.rb_net_vedio://网络视频
                        position = 2;
                        break;
                    case R.id.rb_net_music://网络音乐
                        position = 3;
                        break;
                    default://默认是本地视频
                        position = 0;
                        break;
                }
                BasePage nextPage = getPage();
                switchPager(tempPager, nextPage);
            }
        });
        rg_main.check(R.id.rb_local_vedio);

    }

    private void switchPager(Fragment fromPage, BasePage nextPage) {
        if(tempPager != nextPage){
            tempPager = nextPage;
            if(nextPage != null){
                FragmentTransaction  transaction = getSupportFragmentManager().beginTransaction();
                if(!nextPage.isAdded()){
                    if(fromPage != null){
                        transaction.hide(fromPage);
                    }
                    transaction.add(R.id.fl_main,nextPage).commit();
                }else{
                    if(fromPage != null){
                        transaction.hide(fromPage);
                    }
                    transaction.show(nextPage).commit();
                }
            }
        }
    }

    private void initView() {
        fl_main = (FrameLayout) findViewById(R.id.fl_main);
        rg_main = (RadioGroup) findViewById(R.id.rg_main);
    }

    private void initPages() {
        basePages = new ArrayList<>();
        basePages.add(new LocalVedioPager());
        basePages.add(new LocalMusicPager());
        basePages.add(new NetVedioPager());
        basePages.add(new NetMusicPager());
    }

    private BasePage getPage(){
        BasePage page =  basePages.get(position);
        if(page != null){
            return  page;
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode ==KeyEvent.KEYCODE_BACK){
            if(position != 0){
                rg_main.check(R.id.rb_local_vedio);
                return true;
            }else if(!isExit){
                Toast.makeText(this,"两秒内再点返回退出",Toast.LENGTH_SHORT).show();
                isExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
                return  true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
