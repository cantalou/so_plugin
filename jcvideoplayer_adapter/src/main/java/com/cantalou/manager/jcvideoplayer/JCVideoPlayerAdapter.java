package com.cantalou.manager.jcvideoplayer;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.cantalou.manager.soloader.DownloadItem;
import com.cantalou.manager.soloader.RequestListener;
import com.cantalou.manager.soloader.SoLoaderManager;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * @author cantalou
 * @date 2016年08月31日 14:30
 */
public class JCVideoPlayerAdapter extends JCVideoPlayerStandard implements RequestListener,
                                                                           AbsListView.RecyclerListener
{

    private IjkplayerRequest request;

    private static boolean soFileUnLoaded = true;

    private TextView tv;

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
        request = new IjkplayerRequest(getContext(), this);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = 10;
        tv = new TextView(getContext());
        tv.setText("正在加载播放组件...");
        addView(tv, lp);
        tv.setVisibility(View.GONE);
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
                JCVideoPlayerAdapter.super.prepareVideo();
                soFileUnLoaded = false;
                tv.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void prepareVideo()
    {
        SoLoaderManager.getInstance()
                       .download(request);
        tv.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.GONE);
    }

    public static void releaseAll(Context context)
    {
        if (context == null || soFileUnLoaded || !SoLoaderManager.getInstance()
                                                                 .isAllLoaded(new IjkplayerRequest(context, null)))
        {
            return;
        }
    }

    public void releaseAllVideo()
    {
        if (soFileUnLoaded || !SoLoaderManager.getInstance()
                                              .isAllLoaded(new IjkplayerRequest(getContext(), null)))
        {
            return;
        }
        super.releaseAllVideos();
        cancelProgressTimer();
    }

    @Override
    public void onMovedToScrapHeap(View view)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            return;
        }
        surface.release();

    }
}
