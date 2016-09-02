package com.cantalou.manager.soloader;

import com.cantalou.android.util.FileUtil;
import com.cantalou.android.util.Log;

import java.io.File;
import java.util.HashSet;

import static com.cantalou.android.util.ReflectUtil.forName;
import static com.cantalou.android.util.ReflectUtil.invoke;

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
            mListener.onSuccess(mDownloadItem);
            Log.i("File :{} was downloaded", dest);
            return;
        }
        String url = mDownloadItem.getUrl();

        Log.d("Start download file , url:{}", url);

        mTemp = new File(dest.getAbsolutePath() + ".tmp");
        if (mTemp.exists()) {
            if (mDownloadingUrl.contains(url)) {
                Log.i("File {} is downloading return", url);
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
            invoke(forName("android.system.Os"), "chmod", new Class<?>[]{}, mTemp.getAbsolutePath(), 00755);
            mTemp.renameTo(dest);
            mListener.onSuccess(mDownloadItem);
            Log.d("File url:{} download success, size:{}", url, dest.length());
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
