package com.cantalou.manager.jcvideoplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cantalou.android.manager.lifecycle.ActivityLifecycleCallbacksAdapter;
import com.cantalou.android.util.Log;
import com.cantalou.manager.soloader.DownloadItem;
import com.cantalou.manager.soloader.RequestListener;
import com.cantalou.manager.soloader.SoLoaderManager;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @author cantalou
 * @date 2016年08月31日 14:30
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class JCVideoPlayerAdapter extends JCVideoPlayerStandard implements RequestListener
{

    private Application.ActivityLifecycleCallbacks activityLifecycleAdapter;

    private String mediaUrl;

    private IjkplayerRequest request;

    private TextView tv;

    private static SoLoaderManager soLoaderManager = SoLoaderManager.getInstance();

    public JCVideoPlayerAdapter(Context context)
    {
        super(context);
    }

    public JCVideoPlayerAdapter(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void init(Context context)
    {
        super.init(context);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = 10;
        tv = new TextView(context);
        tv.setText("正在加载播放组件...");
        addView(tv, lp);
        tv.setVisibility(View.GONE);

        activityLifecycleAdapter = new ActivityLifecycleCallbacksAdapter()
        {
            @Override
            public void onActivityPaused(Activity activity)
            {
                if (activity == getContext())
                {
                    releaseResource();
                }
            }
        };
        ((Activity) context).getApplication()
                            .registerActivityLifecycleCallbacks(activityLifecycleAdapter);
    }

    @Override
    public boolean setUp(String url, int screen, Object... objects)
    {
        if (url.indexOf('?') > -1)
        {
            mediaUrl = url + "&code=" + hashCode();
        }
        else
        {
            mediaUrl = url + "?code=" + hashCode();
        }
        return super.setUp(url, screen, objects);
    }

    @Override
    public void preLoad()
    {
    }

    @Override
    public boolean onSuccess(DownloadItem item)
    {
        return false;
    }

    @Override
    public boolean onError(DownloadItem item, Throwable t)
    {
        return false;
    }

    @Override
    public void afterLoaded()
    {
        post(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d("start playing media target:{}", this);
                JCVideoPlayerAdapter.super.prepareVideo();
                tv.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void prepareVideo()
    {
        if (request != null)
        {
            request.setRequestListener(this);
        }
        else
        {
            request = new IjkplayerRequest(getContext(), this);
        }
        soLoaderManager.download(request);
        tv.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.GONE);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        cancelProgressTimer();
        releaseResource();
        ((Activity) getContext()).getApplication()
                                 .unregisterActivityLifecycleCallbacks(activityLifecycleAdapter);
    }

    public void releaseResource()
    {
        if (request == null)
        {
            return;
        }
        request.setRequestListener(null);
        cancelProgressTimer();
        Log.d("release mediaPlayer resource ");
        if (soLoaderManager.isAllLoaded(request))
        {
            IjkMediaPlayer player = JCMediaManager.instance().mediaPlayer;
            if (player != null && mediaUrl.equals(player.getDataSource()))
            {
                releaseAllVideos();
            }
            surface.release();
        }
    }
}
