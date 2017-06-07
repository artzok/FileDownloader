package com.artzok.downloader.core;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public interface DataSource {
    InputStream fetchRangeData(String url, long start, long end) throws Exception;

    DataSource DEFAULT = new DataSource() {
        public InputStream fetchRangeData(String url, long start, long end) throws Exception {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            return conn.getInputStream();
        }
    };
}
