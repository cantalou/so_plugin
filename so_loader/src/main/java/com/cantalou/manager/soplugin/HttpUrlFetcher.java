package com.cantalou.manager.soplugin;

import android.text.TextUtils;
import android.util.Log;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * A DataFetcher that retrieves an {@link java.io.InputStream} for a Url.
 *
 * @author cantalou
 * @date 2016年08月30日 11:45
 */
public class HttpUrlFetcher {

    private static final String TAG = "HttpUrlFetcher";
    private static final int MAXIMUM_REDIRECTS = 5;
    private static final int MAX_RETRY = 3;
    private static final HttpUrlConnectionFactory DEFAULT_CONNECTION_FACTORY = new DefaultHttpUrlConnectionFactory();

    private HttpUrlConnectionFactory connectionFactory;

    private HttpURLConnection urlConnection;
    private InputStream stream;
    private volatile boolean isCancelled;
    private DownloadItem item;

    public HttpUrlFetcher(DownloadItem item) {
        this(item, DEFAULT_CONNECTION_FACTORY);
    }

    public HttpUrlFetcher(DownloadItem item, HttpUrlConnectionFactory connectionFactory) {
        this.item = item;
        this.connectionFactory = connectionFactory;
    }

    public InputStream loadData() throws Exception {
        int i = 1;
        while (i < MAX_RETRY) {
            try {
                return loadDataWithRedirects(new URL(item.getUrl()), 0, null, item.getHeaders());
            } catch (SocketTimeoutException e) {
                i++;
            }
        }
        return null;
    }

    private InputStream loadDataWithRedirects(URL url, int redirects, URL lastUrl, Map<String, String> headers)
            throws IOException {
        if (redirects >= MAXIMUM_REDIRECTS) {
            throw new IOException("Too many (> " + MAXIMUM_REDIRECTS + ") redirects!");
        } else {
            // Comparing the URLs using .equals performs additional network I/O and is generally broken.
            // See http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html.
            try {
                if (lastUrl != null && url.toURI().equals(lastUrl.toURI())) {
                    throw new IOException("In re-direct loop");
                }
            } catch (URISyntaxException e) {
                // Do nothing, this is best effort.
            }
        }
        return loadData(url, redirects, lastUrl, headers);
    }

    private InputStream loadData(URL url, int redirects, URL lastUrl, Map<String, String> headers) throws IOException {
        urlConnection = connectionFactory.build(url);
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }
        urlConnection.setConnectTimeout(2500);
        urlConnection.setReadTimeout(2500);
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);

        // Connect explicitly to avoid errors in decoders if connection fails.
        urlConnection.connect();
        if (isCancelled) {
            return null;
        }
        final int statusCode = urlConnection.getResponseCode();
        if (statusCode / 100 == 2) {
            return getStreamForSuccessfulRequest(urlConnection);
        } else if (statusCode / 100 == 3) {
            String redirectUrlString = urlConnection.getHeaderField("Location");
            if (TextUtils.isEmpty(redirectUrlString)) {
                throw new IOException("Received empty or null redirect url");
            }
            URL redirectUrl = new URL(url, redirectUrlString);
            return loadDataWithRedirects(redirectUrl, redirects + 1, url, headers);
        } else {
            if (statusCode == -1) {
                throw new IOException("Unable to retrieve response code from HttpUrlConnection.");
            }
            throw new IOException("Request failed " + statusCode + ": " + urlConnection.getResponseMessage());
        }
    }

    private InputStream getStreamForSuccessfulRequest(HttpURLConnection urlConnection)
            throws IOException {
        if (TextUtils.isEmpty(urlConnection.getContentEncoding())) {
            int contentLength = urlConnection.getContentLength();
            stream = ContentLengthInputStream.obtain(urlConnection.getInputStream(), contentLength);
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got non empty content encoding: " + urlConnection.getContentEncoding());
            }
            stream = urlConnection.getInputStream();
        }
        return stream;
    }

    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    public void cancel() {
        // TODO: we should consider disconnecting the url connection here, but we can't do so directly because cancel is
        // often called on the main thread.
        isCancelled = true;
    }

    interface HttpUrlConnectionFactory {
        HttpURLConnection build(URL url) throws IOException;
    }

    private static class DefaultHttpUrlConnectionFactory implements HttpUrlConnectionFactory {
        @Override
        public HttpURLConnection build(URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    }

    /**
     * Uses the content length as the basis for the return value of {@link #available()} and verifies
     * that at least content length bytes are returned from the various read methods.
     */
    public static class ContentLengthInputStream extends FilterInputStream {
        private static final String TAG = "ContentLengthStream";
        private static final int UNKNOWN = -1;

        private final long contentLength;
        private int readSoFar;

        public static InputStream obtain(InputStream other, String contentLengthHeader) {
            return obtain(other, parseContentLength(contentLengthHeader));
        }

        public static InputStream obtain(InputStream other, long contentLength) {
            return new ContentLengthInputStream(other, contentLength);
        }

        private static int parseContentLength(String contentLengthHeader) {
            int result = UNKNOWN;
            if (!TextUtils.isEmpty(contentLengthHeader)) {
                try {
                    result = Integer.parseInt(contentLengthHeader);
                } catch (NumberFormatException e) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "failed to parse content length header: " + contentLengthHeader, e);
                    }
                }
            }
            return result;
        }

        ContentLengthInputStream(InputStream in, long contentLength) {
            super(in);
            this.contentLength = contentLength;
        }

        @Override
        public synchronized int available() throws IOException {
            return (int) Math.max(contentLength - readSoFar, in.available());
        }

        @Override
        public synchronized int read() throws IOException {
            return checkReadSoFarOrThrow(super.read());
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            return read(buffer, 0 /*byteOffset*/, buffer.length /*byteCount*/);
        }

        @Override
        public synchronized int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            return checkReadSoFarOrThrow(super.read(buffer, byteOffset, byteCount));
        }

        private int checkReadSoFarOrThrow(int read) throws IOException {
            if (read >= 0) {
                readSoFar += read;
            } else if (contentLength - readSoFar > 0) {
                throw new IOException("Failed to read all expected data"
                        + ", expected: " + contentLength
                        + ", but read: " + readSoFar);
            }
            return read;
        }
    }
}

