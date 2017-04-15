package com.zcc.mobileplayer.pager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zcc.mobileplayer.R;
import com.zcc.mobileplayer.activity.VideoPlayerActivity;
import com.zcc.mobileplayer.adapter.NetVideoPagerAdapter;
import com.zcc.mobileplayer.base.BasePage;
import com.zcc.mobileplayer.bean.MediaItem;
import com.zcc.mobileplayer.utils.CacheUtils;
import com.zcc.mobileplayer.utils.Constants;
import com.zcc.mobileplayer.view.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 朱超超 on 2017-03-26.
 * 作用：
 */
public class NetVedioPager extends BasePage {
    private static final String TAG = NetVedioPager.class.getSimpleName();
    @ViewInject(R.id.listview)
    private XListView mListview;

    @ViewInject(R.id.tv_nonet)
    private TextView mTv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mProgressBar;

    /**
     * 装数据集合
     */
    private ArrayList<MediaItem> mediaItems;

    private NetVideoPagerAdapter adapter;

    /**
     * 是否已经加载更多了
     */
    private boolean isLoadMore = false;



    /**
     * 初始化当前页面的控件，由父类调用
     * @return
     */
    @Override
    public View initView() {
        View view = View.inflate(mcontext, R.layout.netvideo_pager,null);
        //第一个参数是：NetVideoPager.this,第二个参数：布局
        x.view().inject(NetVedioPager.this, view);
        mListview.setOnItemClickListener(new MyOnItemClickListener());
        mListview.setPullLoadEnable(true);
        mListview.setXListViewListener(new MyIXListViewListener());
        return view;
    }

    class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            getDataFromNet();
        }

        @Override
        public void onLoadMore() {

            getMoreDataFromNet();
        }
    }

    private void getMoreDataFromNet() {
        //联网
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG,"联网成功==");
                isLoadMore = true;
                //主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG,"联网失败==" );
                isLoadMore = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e(TAG,"onCancelled==" );
                isLoadMore = false;
            }

            @Override
            public void onFinished() {
                Log.e(TAG,"onFinished==");
                isLoadMore = false;
            }
        });
    }


    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //3.传递列表数据-对象-序列化
            Intent intent = new Intent(mcontext,VideoPlayerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaItems",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position-1);
            mcontext.startActivity(intent);

        }
    }



    @Override
    public void initData() {
        Log.e(TAG,"网络视频的数据被初始化了。。。");
        super.initData();
        String saveJson = CacheUtils.getString(mcontext, Constants.NET_URL);
        if(!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }
        getDataFromNet();

    }

    private void getDataFromNet() {
        //联网
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG,"联网成功==" );
                //缓存数据
                CacheUtils.putString(mcontext,Constants.NET_URL,result);
                //主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG,"联网失败==" );
                showData();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e(TAG,"onCancelled==" );
            }

            @Override
            public void onFinished() {
                Log.e(TAG,"onFinished==");
            }
        });
    }

    private void processData(String json) {

        if(!isLoadMore){
            mediaItems = parseJson(json);
            showData();


        }else{
            //加载更多
            //要把得到更多的数据，添加到原来的集合中
//            ArrayList<MediaItem> moreDatas = parseJson(json);
            isLoadMore = false;
            mediaItems.addAll(parseJson(json));
            //刷新适配器
            adapter.notifyDataSetChanged();
            onLoad();



        }



    }

    private void showData() {
        //设置适配器
        if(mediaItems != null && mediaItems.size() >0){
            //有数据
            //设置适配器
            adapter = new NetVideoPagerAdapter(mcontext,mediaItems);
            mListview.setAdapter(adapter);
            onLoad();
            //把文本隐藏
            mTv_nonet.setVisibility(View.GONE);
        }else{
            //没有数据
            //文本显示
            mTv_nonet.setVisibility(View.VISIBLE);
        }


        //ProgressBar隐藏
        mProgressBar.setVisibility(View.GONE);
    }


    private void onLoad() {
        mListview.stopRefresh();
        mListview.stopLoadMore();
        mListview.setRefreshTime("更新时间:"+getSysteTime());
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }
    /**
     * 解决json数据：
     * 1.用系统接口解析json数据
     * 2.使用第三方解决工具（Gson,fastjson）
     * @param json
     * @return
     */
    private ArrayList<MediaItem> parseJson(String json) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if(jsonArray!= null && jsonArray.length() >0){

                for (int i=0;i<jsonArray.length();i++){

                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);

                    if(jsonObjectItem != null){

                        MediaItem mediaItem = new MediaItem();


                        String movieName = jsonObjectItem.optString("movieName");//name
                        mediaItem.setName(movieName);

                        String videoTitle = jsonObjectItem.optString("videoTitle");//desc
                        mediaItem.setDesc(videoTitle);

                        String imageUrl = jsonObjectItem.optString("coverImg");//imageUrl
                        mediaItem.setImageUrl(imageUrl);

                        String hightUrl = jsonObjectItem.optString("hightUrl");//data
                        mediaItem.setData(hightUrl);

                        //把数据添加到集合
                        mediaItems.add(mediaItem);
                    }
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaItems;
    }
}
