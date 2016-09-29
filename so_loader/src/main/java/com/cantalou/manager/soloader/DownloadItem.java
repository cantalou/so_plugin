package com.cantalou.manager.soloader;

import com.cantalou.manager.soloader.fetcher.HttpUrlFetcher;

import java.util.Collections;
import java.util.Map;

/**
 * @author cantalou
 * @date 2016年08月29日 17:48
 */
public class DownloadItem {

    private String url;

    private String dest;

    private String cacheDest;

    private Request builder;

    private Map<String, String> headers = Collections.emptyMap();

    private HttpUrlFetcher fetcher;

    public DownloadItem(String url, String dest, String cacheDest, Request builder) {
        this.url = url;
        this.dest = dest;
        this.cacheDest = cacheDest;
        this.builder = builder;
    }

    public String getUrl() {
        return url;
    }

    public String getDest() {
        return dest;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Request getBuilder() {
        return builder;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setFetcher(HttpUrlFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public HttpUrlFetcher getFetcher() {
        return fetcher;
    }

    public String getCacheDest() {
        return cacheDest;
    }
}
