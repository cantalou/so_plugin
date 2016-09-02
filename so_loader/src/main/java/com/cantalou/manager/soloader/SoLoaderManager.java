package com.cantalou.manager.soloader;

import android.os.Build;

import com.cantalou.android.util.Log;
import com.cantalou.android.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cantalou
 * @date 2016年08月29日 13:58
 */
public class SoLoaderManager implements RequestListener {

    private static class Holder {
        static final SoLoaderManager INSTANCE = new SoLoaderManager();
    }

    public static SoLoaderManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 第一选择的cpu类型
     */
    private String firstAbi = null;

    /**
     * 手机支持的cpu类型
     */
    private String[] supportedAbis = null;

    /**
     * 已加载的so文件
     */
    private ConcurrentHashMap<String, Object> loadedSo = new ConcurrentHashMap<String, Object>();

    private SoLoaderManager() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportedAbis = Build.SUPPORTED_ABIS;
            if (supportedAbis != null && supportedAbis.length > 0) {
                firstAbi = supportedAbis[0];
            }
        }

        if (supportedAbis == null || supportedAbis.length == 0) {
            supportedAbis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            firstAbi = StringUtils.isNotBlank(Build.CPU_ABI) ? Build.CPU_ABI : Build.CPU_ABI2;
        }
    }

    /**
     * so文件是否加载完成
     *
     * @param builder
     * @return
     */
    public boolean isAllLoaded(Request builder) {

        List<DownloadItem> items = builder.getDownloadItems();
        if (items.isEmpty()) {
            return true;
        }

        for (DownloadItem item : items) {
            if (!loadedSo.contains(item.getDest().getAbsolutePath())) {
                return false;
            }
        }
        return true;
    }

    /**
     * so文件是否都下载完成
     *
     * @param builder
     * @return
     */
    public boolean isAllDownloaded(Request builder) {
        for (DownloadItem item : builder.getDownloadItems()) {
            if (!item.getDest().exists()) {
                return false;
            }
        }
        return true;
    }

    public void download(Request builder) {

        RequestListener listener = builder.getRequestListener();

        for (String abi : supportedAbis) {
            if (abi.equals(builder.getDefaultPlatform().name)) {
                Log.i("So file had bind into apk");
                listener.afterLoaded();
                return;
            }
        }

        if (StringUtils.isBlank(firstAbi)) {
            Log.w("Not support cpu abi");
            return;
        }

        listener.preLoad();

        List<DownloadItem> items = builder.getDownloadItems();
        if (items.isEmpty()) {
            items = createDownloadItem(builder);
            builder.setDownloadItems(items);
        }

        if (isAllLoaded(builder)) {
            listener.afterLoaded();
            return;
        }

        ExecutorService es = Executors.newFixedThreadPool(items.size());
        for (DownloadItem item : items) {
            es.execute(new Downloader(item, this));
        }
    }

    private List<DownloadItem> createDownloadItem(Request builder) {

        String[] soFiles = builder.getSoFiles();
        ArrayList<DownloadItem> items = new ArrayList<DownloadItem>(soFiles.length);
        StringBuilder sb = new StringBuilder();
        File destDir = new File(builder.getContext().getFilesDir() + "/libs");
        destDir.mkdirs();
        for (int i = 0; i < builder.getSoFiles().length; i++) {

            sb.setLength(0);
            String fileName = sb.append("lib")
                    .append(soFiles[i])
                    .append(".so")
                    .toString();

            File dest = new File(destDir, fileName);
            if (loadedSo.contains(dest.getAbsoluteFile())) {
                continue;
            }

            sb.setLength(0);
            String url = sb.append(builder.getLibDirUrl())
                    .append('/')
                    .append(firstAbi)
                    .append('/')
                    .append(fileName)
                    .toString();
            items.add(new DownloadItem(url, dest, builder));
        }
        return items;
    }

    @Override
    public boolean onError(DownloadItem item, Throwable t) {
        RequestListener listener = item.getBuilder().getRequestListener();
        if (listener != null) {
            return listener.onError(item, t);
        }
        return false;
    }

    @Override
    public void afterLoaded() {
    }

    @Override
    public void preLoad() {
    }

    @Override
    public boolean onSuccess(DownloadItem item) {
        Request rb = item.getBuilder();
        RequestListener listener = rb.getRequestListener();
        if (listener != null && listener.onSuccess(item)) {
            return true;
        }
        synchronized (this) {
            if (!isAllDownloaded(rb)) {
                return false;
            }

            try {
                for (DownloadItem di : rb.getDownloadItems()) {
                    String path = di.getDest().getAbsolutePath();
                    if (loadedSo.contains(path)) {
                        continue;
                    }
                    System.load(path);
                    loadedSo.put(path, path);
                }
                if (listener != null) {
                    listener.afterLoaded();
                }
            } catch (Throwable e) {
                //item.getDest().delete();
                Log.w(e, "Loading lib error");
            }
        }
        return true;
    }
}
