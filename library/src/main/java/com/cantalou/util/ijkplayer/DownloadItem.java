package com.cantalou.util.ijkplayer;

import java.io.File;

/**
 * @author cantalou
 * @date 2016年08月29日 17:48
 */
public class DownloadItem
{
    private String url;

    private File dest;

    private boolean ready;

    public DownloadItem(String url, File dest)
    {
        this.url = url;
        this.dest = dest;
    }

    public boolean isReady()
    {
        return ready;
    }

    public void setReady(boolean ready)
    {
        this.ready = ready;
    }

    public String getUrl()
    {
        return url;
    }

    public File getDest()
    {
        return dest;
    }
}
