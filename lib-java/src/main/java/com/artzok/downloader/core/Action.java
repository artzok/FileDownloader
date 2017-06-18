package com.artzok.downloader.core;


public enum Action {
    // user action
    DOWNLOAD,           // 点击下载
    CANCEL,             // 取消下载
    PAUSE,              // 点击暂停
    CONTINUE,           // 继续下载
    RETRY,              // 点击重试
    OPEN,               // 下载完成后用户再次点击

    // downloader action
    // 开始下载(当用户点击下载之后、
    // 点击继续之后、点击重试之后，
    // 一但downloader请求到数据将会通知该行为)
    START,

    // 一旦连接中数据请求完毕，将发送该通知
    FINISHED,

    // 当任务的状态一旦发生改变时发送该通知
    CHANGED,

    // 当任务状态未发生改变，但下载任务的其他信息发生了改变
    UPDATE,

    // 下载成功后通知一次
    DOWNLOAD_SUCCEED,

    DOWNLOAD_FAILED;

    public static Action guessUserAction(Status status) {
        switch (status) {
            case DOWNLOADABLE:      // 点击下载
                return DOWNLOAD;
            case PENDING:           // 点击取消
                return CANCEL;
            case DOWNLOADING:       // 点击暂停
                return PAUSE;
            case COMPLETED:          //点击打开
                return OPEN;
            case FAILED:            // 点击重试
                return RETRY;
            case PAUSE:             // 点击继续
                return CONTINUE;
        }
        return CHANGED;
    }
}