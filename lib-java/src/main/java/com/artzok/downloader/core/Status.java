package com.artzok.downloader.core;


public enum Status {
    DOWNLOADABLE,       // 下载
    PENDING,            // 阻塞
    DOWNLOADING,        // 下载中
    PAUSE,              // 暂停
    FAILED,             // 失败
    COMPLETED,          // 完成
    UNDEF;

    static Status query(Task task) {
        return COMPLETED;
    }

    public static Status getStatus(int i) {
        return values()[i];
    }

    public static int indexOf(Status status) {
        if (status == null) return 0;
        return status.ordinal();
    }
}


