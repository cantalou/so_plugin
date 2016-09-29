package com.cantalou.manager.soloader.fetcher;

import java.io.InputStream;

/**
 * @author cantalou
 */
public interface Fetcher {

    public InputStream loadData() throws Exception;
}
