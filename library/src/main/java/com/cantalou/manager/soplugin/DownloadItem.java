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

    private boolean downloaded;

    private boolean loaded;

    private RequestBuilder builder;

    private Map<String, String> headers = Collections.emptyMap();

    public DownloadItem(String url, File dest, RequestBuilder builder) {
        this.url = url;
        this.dest = dest;
        this.builder = builder;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
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

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public RequestBuilder getBuilder() {
        return builder;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDest(File dest) {
        this.dest = dest;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setBuilder(RequestBuilder builder) {
        this.builder = builder;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }


}
