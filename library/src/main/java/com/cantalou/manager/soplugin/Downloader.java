package com.cantalou.manager.soplugin;

import com.cantalou.android.util.FileUtil;
import com.cantalou.android.util.Log;

import java.io.File;
import java.util.HashSet;

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
        public void onSuccess(DownloadItem item);

        /**
         * 下载失败
         */
        public void onError(DownloadItem item, Throwable t);
    }

    private static HashSet<String> downloadingUrl = new HashSet<String>();

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
        File dest = mDownloadItem.dest;
        if (dest.exists()) {
            mDownloadItem.setDownloaded(true);
            mListener.onSuccess(mDownloadItem);
            return;
        }

        String url = mDownloadItem.url;
        temp = new File(dest.getAbsolutePath() + ".tmp");
        if (temp.exists()) {
            if (downloadingUrl.contains(url)) {
                Log.i("File {} is downloading", url);
                return;
            } else {
                Log.i("Delete tmp file :{}", temp);
                temp.delete();
            }
        }

        downloadingUrl.add(url);
        HttpUrlFetcher fetcher = new HttpUrlFetcher(mDownloadItem);
        try {
            FileUtil.copyContent(fetcher.loadData(), temp);
            temp.renameTo(dest);
            mDownloadItem.setDownloaded(true);
            mListener.onSuccess(mDownloadItem);
        } catch (Exception e) {
            mListener.onError(mDownloadItem, e);
            Log.e(e);
        } finally {
            temp.delete();
            downloadingUrl.remove(url);
            if (fetcher != null) {
                fetcher.cleanup();
            }
        }
    }
}
