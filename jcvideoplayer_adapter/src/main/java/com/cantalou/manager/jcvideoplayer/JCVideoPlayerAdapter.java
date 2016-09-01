package com.cantalou.manager.jcvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cantalou.manager.soplugin.DownloadItem;
import com.cantalou.manager.soplugin.RequestListener;
import com.cantalou.manager.soplugin.SoPluginManager;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * @author LinZhiWei
 * @date 2016年08月31日 14:30
 */
public class JCVideoPlayerAdapter extends JCVideoPlayerStandard implements RequestListener {

    private IjkplayerRequest request;

    private boolean hasInit = false;

    private TextView tv;

    public JCVideoPlayerAdapter(Context context) {
        super(context);
    }

    public JCVideoPlayerAdapter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        request = new IjkplayerRequest(getContext(), this);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = 20;
        tv = new TextView(getContext());
        tv.setText("正在加载播放组件...");
        addView(tv, lp);
        tv.setVisibility(View.GONE);
    }

    @Override
    public void preLoad() {

    }

    @Override
    public boolean onSuccess(DownloadItem item) {
        return false;
    }

    @Override
    public boolean onError(DownloadItem item, Throwable t) {
        return false;
    }

    @Override
    public void afterLoaded() {
        post(new Runnable() {
            @Override
            public void run() {
                JCVideoPlayerAdapter.super.prepareVideo();
                hasInit = true;
                tv.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void prepareVideo() {
        SoPluginManager.getInstance().download(request);
        tv.setVisibility(View.VISIBLE);
    }

    public void releaseVideos() {
        if (hasInit) {
            JCMediaManager.instance().releaseMediaPlayer();
            cancelProgressTimer();
        }
    }
}
