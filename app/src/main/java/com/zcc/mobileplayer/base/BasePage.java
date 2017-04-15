package com.zcc.mobileplayer.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 朱超超 on 2017-03-26.
 * 作用：
 */
public abstract class BasePage extends Fragment {
    public Activity mcontext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mcontext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView();
    }

    @Override
         public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 子页面必须重写此方法，实现各个页面的View
     * @return
     */
    public abstract View initView();

    /**
     * 子页面重写此方法，联网请求数据
     */
    public  void  initData(){};
}
