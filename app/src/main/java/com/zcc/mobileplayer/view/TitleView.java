package com.zcc.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zcc.mobileplayer.R;

/**
 * Created by 朱超超 on 2017-03-26.
 * 作用：头部视图类
 */
public class TitleView extends LinearLayout implements View.OnClickListener {
    /**
     * 搜索
     */
    private View tv_search;
    /**
     * 游戏
     */
    private  View rl_game;

    /**
     *历史记录
     */
    private  View iv_record;

    private Context context;
    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public TitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleView(Context context) {
        this(context,null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tv_search = getChildAt(1);
        rl_game = getChildAt(2);
        iv_record = getChildAt(3);

        //设置点击事件
        tv_search.setOnClickListener(this);
        rl_game.setOnClickListener(this);
        iv_record.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search://搜索
                Toast.makeText(context, "搜索", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(context,SearchActivity.class);
//                context.startActivity(intent);
                break;
            case R.id.rl_game://游戏
                Toast.makeText(context, "游戏", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_record://播放历史
                Toast.makeText(context, "播放历史", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
