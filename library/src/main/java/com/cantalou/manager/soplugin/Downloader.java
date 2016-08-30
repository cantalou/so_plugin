package com.cantalou.manager.soplugin;

import com.cantalou.android.util.FileUtil;
import com.cantalou.android.util.Log;

import java.io.File;

/**
 * @author cantalou
 * @date 2016年08月29日 14:20
 */
public class Downloader implements Runnable {

    /**
     * 下载监听器
     */
    public static interface DownloadListener {
        /**
         * 下载成功
         */
        public void onSuccess();

        /**
         * 下载失败
         */
        public void onError(Throwable t);
    }

    private DownloadItem mDownloadItem;

    /**
     * 下载监听
     */
    private DownloadListener mListener;

    /**
     * 下载临时文件
     */
    private File temp;

    /**
     * @param downloadItem 下载信息
     * @param listener     下载监听
     */
    public Downloader(DownloadItem downloadItem, DownloadListener listener) {
        mDownloadItem = downloadItem;
        mListener = listener;
    }

    @Override
    public void run() {
        File dest = mDownloadItem.getDest();
        if (dest.exists()) {
            mDownloadItem.setReady(true);
            return;
        }

        temp = new File(dest.getAbsolutePath() + ".tmp");
        if (temp.exists()) {
            Log.w("File {} is downloading", mDownloadItem.getUrl());
            return;
        }

        HttpUrlFetcher fetcher = new HttpUrlFetcher(mDownloadItem);
        try {
            FileUtil.copyContent(fetcher.loadData(), temp);
            temp.renameTo(dest);
        } catch (Exception e) {
            mListener.onError(e);
            Log.e(e);
        } finally {
            temp.delete();

            if (fetcher != null) {
                fetcher.cleanup();
            }
        }
        mDownloadItem.setReady(true);
        mListener.onSuccess();
    }

    public void clear() {
        if (temp != null) {
            temp.delete();
        }
    }
}
