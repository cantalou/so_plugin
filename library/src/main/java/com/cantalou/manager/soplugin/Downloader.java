package com.cantalou.util.soplugin;

import com.cantalou.android.util.FileUtil;
import com.cantalou.android.util.Log;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author cantalou
 * @date 2016年08月29日 14:20
 */
public class Downloader implements Runnable
{
    /**
     * 下载监听器
     */
    public static interface DownloadListener
    {
        /**
         * 下载成功
         */
        public void onSuccess(DownloadItem item);

        /**
         * 下载失败
         */
        public void onError(DownloadItem item, Throwable t);
    }

    private DownloadItem mDownloadItem;

    /**
     * 下载监听
     */
    private DownloadListener mListener;

    /**
     * @param downloadItem   下载信息
     * @param listener 下载监听
     */
    public Downloader(DownloadItem downloadItem, DownloadListener listener)
    {
        mDownloadItem = downloadItem;
        mListener = listener;
    }

    @Override
    public void run()
    {
        File dest = mDownloadItem.getDest();
        if (dest.exists())
        {
            mDownloadItem.setReady(true);
            return;
        }

        File temp = new File(dest.getAbsolutePath() + ".tmp");
        if (temp.exists())
        {
            Log.w("File {} is downloading", mDownloadItem.getUrl());
            return;
        }

        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) new URL(mDownloadItem.getUrl()).openConnection();
            FileUtil.copyContent(conn.getInputStream(), temp);
            temp.renameTo(dest);
        }
        catch (Exception e)
        {
            mListener.onError(mDownloadItem, e);
            Log.e(e);
        }
        finally
        {
            temp.delete();

            if (conn != null)
            {
                conn.disconnect();
            }
        }
        mDownloadItem.setReady(true);
        mListener.onSuccess(mDownloadItem);
    }
}
