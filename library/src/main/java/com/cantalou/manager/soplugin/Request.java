package com.cantalou.manager.soplugin;

import android.content.Context;

import java.util.Collections;
import java.util.List;

/**
 * @author cantalou
 * @date 2016年08月30日 16:14
 */
public class Request {

    protected Context context;

    protected boolean soFileReady = false;

    protected List<DownloadItem> downloadItems = Collections.emptyList();

    /**
     * 默认以支持的平台类型
     */
    protected Platform defaultPlatform;

    /**
     * so文件根目录, 该目录下要包含 x86,armeabi等平台目录
     */
    protected String libDirUrl;

    /**
     * 要下载的so文件名称, 不包含前缀"lib"和后缀".so"
     */
    protected String[] soFiles;

    protected RequestListener requestListener;

    public Request(Context context, String libDirUrl, String[] soFiles, RequestListener requestListener) {
        this.context = context.getApplicationContext();
        this.libDirUrl = libDirUrl;
        this.soFiles = soFiles;
        this.requestListener = requestListener;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isSoFileReady() {
        return soFileReady;
    }

    public void setSoFileReady(boolean soFileReady) {
        this.soFileReady = soFileReady;
    }

    public List<DownloadItem> getDownloadItems() {
        return downloadItems;
    }

    public void setDownloadItems(List<DownloadItem> downloadItems) {
        this.downloadItems = downloadItems;
    }

    public Platform getDefaultPlatform() {
        return defaultPlatform;
    }

    public void setDefaultPlatform(Platform defaultPlatform) {
        this.defaultPlatform = defaultPlatform;
    }

    public String getLibDirUrl() {
        return libDirUrl;
    }

    public void setLibDirUrl(String libDirUrl) {
        this.libDirUrl = libDirUrl;
    }

    public String[] getSoFiles() {
        return soFiles;
    }

    public void setSoFiles(String[] soFiles) {
        this.soFiles = soFiles;
    }

    public RequestListener getRequestListener() {
        return requestListener;
    }

    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }
}
