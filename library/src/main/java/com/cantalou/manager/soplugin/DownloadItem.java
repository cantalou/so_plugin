package com.cantalou.manager.soplugin;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * @author cantalou
 * @date 2016年08月29日 17:48
 */
public class DownloadItem {

    private String url;

    private File dest;

    private boolean ready;

    private Map<String, String> headers = Collections.emptyMap();

    public DownloadItem(String url, File dest) {
        this.url = url;
        this.dest = dest;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getUrl() {
        return url;
    }

    public File getDest() {
        return dest;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
