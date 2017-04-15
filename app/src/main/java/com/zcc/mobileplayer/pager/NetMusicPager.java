package com.zcc.mobileplayer.pager;


import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.zcc.mobileplayer.R;
import com.zcc.mobileplayer.adapter.NetAudioPagerAdapter;
import com.zcc.mobileplayer.base.BasePage;
import com.zcc.mobileplayer.bean.NetAudioPagerData;
import com.zcc.mobileplayer.utils.CacheUtils;
import com.zcc.mobileplayer.utils.Constants;
import com.zcc.mobileplayer.view.XListView;

import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by 朱超超 on 2017-03-26.
 * 作用：
 */
public class NetMusicPager extends BasePage {
    private static final String TAG = NetMusicPager.class.getSimpleName();
    @ViewInject(R.id.listview)
    private XListView  mListView;


    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;


    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;
    /**
     * 页面的数据
     */
    private List<NetAudioPagerData.ListEntity> datas;

    private NetAudioPagerAdapter adapter;



    /**
     * 初始化当前页面的控件，由父类调用
     * @return
     */
    @Override
    public View initView() {
        View view = View.inflate(mcontext, R.layout.netaudio_pager,null);
        x.view().inject(NetMusicPager.this, view);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(new MyIXListViewListener());
        return view;
    }

    class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            getDataFromNet();
        }

        @Override
        public void onLoadMore() {

            //getMoreDataFromNet();
        }
    }
    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络音频的数据被初始化了。。。");
        String savaJson = CacheUtils.getString(mcontext, Constants.ALL_RES_URL);
        if(!TextUtils.isEmpty(savaJson)){
            //解析数据
            processData(savaJson);
        }
        //联网
        getDataFromNet();
    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("请求数据成功==" + result);
                //保持数据
                CacheUtils.putString(mcontext, Constants.ALL_RES_URL, result);
                processData(result);
                onLoad();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("请求数据失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }

    /**
     * 解析json数据和显示数据
     * 解析数据：1.GsonFormat生成bean对象；2.用gson解析数据
     * @param json
     */
    private void processData(String json) {
        //解析数据
        NetAudioPagerData data = parsedJson(json);
        datas =  data.getList();

        if(datas != null && datas.size() >0 ){
            //有数据
            tv_nonet.setVisibility(View.GONE);
            //设置适配器
            adapter = new NetAudioPagerAdapter(mcontext,datas);
            mListView.setAdapter(adapter);
        }else{
            tv_nonet.setText("没有对应的数据...");
            //没有数据
            tv_nonet.setVisibility(View.VISIBLE);
        }

        pb_loading.setVisibility(View.GONE);

    }

    /**
     * Gson解析数据
     * @param json
     * @return
     */
    private NetAudioPagerData parsedJson(String json) {
        return new Gson().fromJson(json,NetAudioPagerData.class);
    }
    private void onLoad() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mListView.setRefreshTime("更新时间:"+getSysteTime());
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
}
