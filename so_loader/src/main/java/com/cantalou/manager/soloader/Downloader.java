package com.cantalou.manager.soloader;

import android.os.Handler;
import android.os.Message;

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
public class Downloader implements Runnable
{

    /**
     * The file is downloading
     */
    private static HashSet<String> mDownloadingUrl = new HashSet<String>();

    private DownloadItem mDownloadItem;

    private Handler mHandler;

    private File mTemp;

    public Downloader(DownloadItem downloadItem, Handler handler)
    {
        mDownloadItem = downloadItem;
        mHandler = handler;
    }

    @Override
    public void run()
    {
        String dest = mDownloadItem.getDest();
        File destPath = new File(dest);
        if (destPath.exists())
        {
            downloadSuccess();
            Log.i("File :{} was downloaded", dest);
            return;
        }

        String url = mDownloadItem.getUrl();
        Log.d("Download file starting url:{}", url);

        mTemp = new File(dest + ".tmp");
        if (mTemp.exists())
        {
            if (mDownloadingUrl.contains(url))
            {
                Log.i("File {} is downloading now return", url);
                return;
            }
            else
            {
                Log.i("Delete tmp file :{}", mTemp);
                mTemp.delete();
            }
        }

        mDownloadingUrl.add(url);
        HttpUrlFetcher fetcher = mDownloadItem.getFetcher();
        if (fetcher == null)
        {
            fetcher = new HttpUrlFetcher(url, mDownloadItem.getHeaders());
        }

        try
        {
            FileUtil.copyContent(fetcher.loadData(), mTemp);
            invoke(forName("android.system.Os"), "chmod", new Class<?>[]{}, mTemp.getAbsolutePath(), 00755);
            mTemp.renameTo(destPath);
            downloadSuccess();
            Log.d("File url:{} download success, size:{}", url, dest.length());
        }
        catch (Exception e)
        {
            downloadError(e);
        }
        finally
        {
            mTemp.delete();
            mDownloadingUrl.remove(url);
            if (fetcher != null)
            {
                fetcher.cleanup();
            }
        }
    }

    private void downloadSuccess()
    {
        Message msg = Message.obtain();
        msg.what = SoLoaderManager.DOWNLOAD_SUCCESS;
        msg.obj = mDownloadItem;
        mHandler.sendMessage(msg);
    }

    private void downloadError(Object reason)
    {
        Message msg = Message.obtain();
        msg.what = SoLoaderManager.DOWNLOAD_ERROR;
        msg.obj = reason;
        mHandler.sendMessage(msg);
    }
}
