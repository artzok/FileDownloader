package com.artzok.downloader.core;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

    public static long getDataLength(String url) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            long contentLength = conn.getContentLength();
            if (contentLength != -1) {
                return contentLength;
            } else {
                throw new RuntimeException("无法获取文件长度");
            }
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }
}
