package com.cantalou.util.ijkplayer;

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
        public void onSuccess(String url);

        /**
         * 下载失败
         */
        public void onError(String url);
    }

    /**
     * 下载地址
     */
    private String mUrl;

    /**
     * 文件保存地址
     */
    private String mDest;

    /**
     * 下载监听
     */
    private DownloadListener mListener;

    /**
     * @param url      下载地址
     * @param dest     文件保存地址
     * @param listener 下载监听
     */
    public Downloader(String url, String dest, DownloadListener listener)
    {
        mUrl = url;
        mDest = dest;
        mListener = listener;
    }

    @Override
    public void run()
    {
        File destFile = new File(mDest);
        if (destFile.exists())
        {
            mListener.onSuccess(mUrl);
            return;
        }

        File temp = new File(mDest + ".tmp");
        if (temp.exists())
        {
            Log.w("File {} is downloading", mUrl);
            return;
        }

        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) new URL(mUrl).openConnection();
            FileUtil.copyContent(conn.getInputStream(), temp);
            temp.renameTo(new File(mDest));
        }
        catch (Exception e)
        {
            mListener.onError(mUrl);
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
        mListener.onSuccess(mUrl);
    }
}
