package com.cantalou.manager.soloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author cantalou
 * @date 2016年08月30日 16:14
 */
public class Request {

    /**
     *
     */
    protected File destDir;

    /**
     * Cache dir for storing so file. Make sure this must not be deleted when app uninstall.
     */
    protected File cacheDir;

    protected List<DownloadItem> downloadItems = Collections.emptyList();

    /**
     * Default cpu type. Manager will load library from app lib directly and skip to download so file when the defaultPlatform matches device. Make sure you had put so file in lib.
     */
    protected Platform defaultPlatform = Platform.NULL;

    /**
     * Root dir for different arch. This dir contains arch dir ,such as : x86, armeabi , armeabi-v7a
     */
    protected String libDirUrl;

    /**
     * So file name without prefix "lib" and suffix ".so", such as "name" in "libname.so"
     */
    protected String[] soFiles;

    /**
     * Request so file version
     */
    protected int version;

    protected RequestListener requestListener;

    protected SharedPreferences preference;

    public Request(Context context, String libDirUrl, String[] soFiles, RequestListener requestListener) {

        this.destDir = context.getFilesDir();
        this.libDirUrl = libDirUrl;
        this.soFiles = soFiles;
        this.requestListener = requestListener;
        this.preference = PreferenceManager.getDefaultSharedPreferences(context);
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (cacheDir != null) {
                cacheDir.mkdirs();
            }
        }
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

    public File getCacheDir() {
        return cacheDir;
    }

    public File getDestDir() {
        return destDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public SharedPreferences getPreference() {
        return preference;
    }
}
