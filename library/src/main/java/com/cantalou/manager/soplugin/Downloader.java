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
     * 正在下载的文件url
     */
    private static HashSet<String> mDownloadingUrl = new HashSet<String>();

    private DownloadItem mDownloadItem;

    /**
     * 下载监听
     */
    private RequestListener mListener;

    /**
     * 下载临时文件
     */
    private File mTemp;

    /**
     * @param downloadItem 下载信息
     */
    public Downloader(DownloadItem downloadItem, RequestListener listener) {
        mDownloadItem = downloadItem;
        mListener = listener;
    }

    @Override
    public void run() {
        File dest = mDownloadItem.getDest();
        if (dest.exists()) {
            mDownloadItem.setDownloaded(true);
            mListener.onSuccess(mDownloadItem);
            return;
        }

        String url = mDownloadItem.getUrl();
        mTemp = new File(dest.getAbsolutePath() + ".tmp");
        if (mTemp.exists()) {
            if (mDownloadingUrl.contains(url)) {
                Log.i("File {} is downloading", url);
                return;
            } else {
                Log.i("Delete tmp file :{}", mTemp);
                mTemp.delete();
            }
        }

        mDownloadingUrl.add(url);
        HttpUrlFetcher fetcher = new HttpUrlFetcher(mDownloadItem);
        try {
            FileUtil.copyContent(fetcher.loadData(), mTemp);
            mTemp.renameTo(dest);
            mDownloadItem.setDownloaded(true);
            mListener.onSuccess(mDownloadItem);
        } catch (Exception e) {
            mListener.onError(mDownloadItem, e);
            Log.e(e);
        } finally {
            mTemp.delete();
            mDownloadingUrl.remove(url);
            if (fetcher != null) {
                fetcher.cleanup();
            }
        }
    }
}
