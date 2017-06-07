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

    public  boolean checkoutDownload() {
        switch (this) {
            case DOWNLOADABLE:
            case PAUSE:
            case FAILED:
                return true;
        }
        return false;
    }

    public  boolean checkoutCancel() {
        switch (this) {
            case PENDING:
            case DOWNLOADING:
            case PAUSE:
            case FAILED:
                return true;
        }
        return false;
    }

    public  boolean checkoutPause() {
        switch (this) {
            case PENDING:
            case DOWNLOADING:
                return true;
        }
        return false;
    }

    public  boolean checkoutOpen() {
        return this == COMPLETED;
    }
}


