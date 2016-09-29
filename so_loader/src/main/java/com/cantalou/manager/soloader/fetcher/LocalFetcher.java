package com.cantalou.manager.soloader.fetcher;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author cantalou
 */
public class LocalFetcher implements Fetcher {

    private String dest;

    public LocalFetcher(String dest) {
        this.dest = dest;
    }

    @Override
    public InputStream loadData() throws Exception {
        return new FileInputStream(dest);
    }
}
