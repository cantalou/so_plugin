package com.cantalou.util.soplugin;

import android.content.Context;
import android.os.Build;

import com.cantalou.android.util.Log;
import com.cantalou.android.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cantalou
 * @date 2016年08月29日 13:58
 */
public class SoPlugin implements Downloader.DownloadListener
{
    public static final String[] IJKPLAYER_SO_FILE = new String[]{
            "ijkffmpeg", "ijksdl", "soplugin"
    };

    private static class Holder
    {
        static final SoPlugin INSTANCE = new SoPlugin();
    }

    public static SoPlugin getInstance()
    {
        return Holder.INSTANCE;
    }

    /**
     * 默认so文件的下载地址
     */
    public static final String DEFAULT_LIB_DIR_URL = "https://github.com/cantalou/ijkplayer_so_plugin/tree/master/libs";

    public static final String ON_SO_FILE_READY_ACTION = "com.cantalou.ON_SO_FILE_READY_ACTION";

    private SoPlugin()
    {
    }
    private boolean soFileReady = false;

    private ArrayList<DownloadItem> downloadItems;

    /**
     * ijkplayer的so文件是否可用
     *
     * @return
     */
    public boolean isSoReady()
    {
        return soFileReady;
    }

    public void install(Context context, Platform defaultPlatform)
    {
        install(context, defaultPlatform, DEFAULT_LIB_DIR_URL);
    }

    public void install(Context context, Platform defaultPlatform, String libDirUrl)
    {
        if (soFileReady)
        {
            return;
        }

        String firstAbi = null;
        String[] supportedAbis = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            supportedAbis = Build.SUPPORTED_ABIS;
            if (supportedAbis != null && supportedAbis.length > 0)
            {
                firstAbi = supportedAbis[0];
            }
        }

        if (supportedAbis == null || supportedAbis.length == 0)
        {
            supportedAbis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            firstAbi = StringUtils.isNotBlank(Build.CPU_ABI) ? Build.CPU_ABI : Build.CPU_ABI2;
        }

        for (String abi : supportedAbis)
        {
            if (defaultPlatform.name.equals(abi))
            {
                Log.i("So file had bind into apk");
                return;
            }
        }

        if (StringUtils.isBlank(firstAbi))
        {
            Log.w("Not support cpu abi");
            return;
        }

        downloadItems = new ArrayList<DownloadItem>(IJKPLAYER_SO_FILE.length);

        StringBuilder sb = new StringBuilder();
        File destDir = new File(context.getFilesDir() + "/libs");
        ExecutorService es = Executors.newFixedThreadPool(IJKPLAYER_SO_FILE.length);
        for (int i = 0; i < IJKPLAYER_SO_FILE.length; i++)
        {
            sb.setLength(0);
            String fileName = sb.append("lib")
                                .append(IJKPLAYER_SO_FILE[i])
                                .append(".so")
                                .toString();

            sb.setLength(0);
            String url = sb.append(libDirUrl)
                           .append('/')
                           .append(firstAbi)
                           .append('/')
                           .append(fileName)
                           .toString();
            DownloadItem item = new DownloadItem(url, new File(destDir, fileName));
            es.execute(new Downloader(item, this));
            downloadItems.add(item);
        }
    }

    @Override
    public void onError(DownloadItem item, Throwable t)
    {
    }

    @Override
    public void onSuccess(DownloadItem item)
    {
        for(DownloadItem di : downloadItems){

        }
    }
}
