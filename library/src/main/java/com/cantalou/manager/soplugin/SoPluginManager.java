package com.cantalou.manager.soplugin;

import android.os.Build;

import com.cantalou.android.util.Log;
import com.cantalou.android.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cantalou
 * @date 2016年08月29日 13:58
 */
public class SoPluginManager implements RequestListener {

    private static class Holder {
        static final SoPluginManager INSTANCE = new SoPluginManager();
    }

    public static SoPluginManager getInstance() {
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

    private SoPluginManager() {

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
    public boolean isReady(Request builder) {

        if (builder.isSoFileReady()) {
            return true;
        }
        for (DownloadItem item : builder.getDownloadItems()) {
            if (!item.isDownloaded()) {
                return false;
            }
        }
        builder.setSoFileReady(true);
        return true;
    }

    public void download(Request builder) {

        for (String abi : supportedAbis) {
            if (builder.getDefaultPlatform().name.equals(abi)) {
                Log.i("So file had bind into apk");
                return;
            }
        }

        if (StringUtils.isBlank(firstAbi)) {
            Log.w("Not support cpu abi");
            return;
        }

        builder.getRequestListener().preLoad();

        String[] soFiles = builder.getSoFiles();
        List<DownloadItem> items = builder.getDownloadItems();
        if (items.isEmpty()) {
            items = new ArrayList<DownloadItem>(soFiles.length);
            StringBuilder sb = new StringBuilder();
            File destDir = new File(builder.getContext().getFilesDir() + "/libs");
            for (int i = 0; i < builder.getSoFiles().length; i++) {
                sb.setLength(0);
                String fileName = sb.append("lib")
                        .append(soFiles[i])
                        .append(".so")
                        .toString();

                sb.setLength(0);
                String url = sb.append(builder.getLibDirUrl())
                        .append('/')
                        .append(firstAbi)
                        .append('/')
                        .append(fileName)
                        .toString();
                items.add(new DownloadItem(url, new File(destDir, fileName), builder));
            }
            builder.setDownloadItems(items);
        }

        ExecutorService es = Executors.newFixedThreadPool(soFiles.length);
        for (DownloadItem item : items) {
            es.execute(new Downloader(item, this));
        }
    }

    @Override
    public boolean onError(DownloadItem item, Throwable t) {
        return item.getBuilder().getRequestListener().onError(item, t);
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
        if (!listener.onSuccess(item)) {
            try {
                System.load(item.getDest().getAbsolutePath());
                item.setLoaded(true);
                if (isReady(rb)) {
                    listener.afterLoaded();
                }
            } catch (Throwable e) {
                item.getDest().delete();
                Log.w("Loading lib error", e);
            }
        }
        return true;
    }
}
