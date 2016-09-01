package com.cantalou.manager.soplugin;

/**
 * @author cantalou
 * @date 2016年08月30日 17:07
 */
public interface RequestListener {

    /**
     * 载入so文件前回调
     */
    public void preLoad();

    /**
     * 下载成功
     *
     * @param item 下载的内容
     * @return true 已处理
     */
    public boolean onSuccess(DownloadItem item);

    /**
     * 下载失败
     *
     * @param item 下载的内容
     * @param t
     * @return true 已处理
     */
    public boolean onError(DownloadItem item, Throwable t);

    /**
     * 载入so文件成功后回调
     */
    public void afterLoaded();
}
