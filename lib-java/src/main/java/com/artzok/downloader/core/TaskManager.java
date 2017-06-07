package com.artzok.downloader.core;

import com.artzok.downloader.defaults.Register;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskManager {

    private Config mConfig;

    private ThreadPoolExecutor mThreadPool;
    private BlockingQueue<Runnable> mWaitingTaskQueue;

    private Map<String, Task> mDownloading;
    private Map<String, Task> mPause;
    private Map<String, Task> mFailed;

    TaskManager(Config conf) {
        mConfig = conf;
        mThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(mConfig.getMaxCount());
        mWaitingTaskQueue = mThreadPool.getQueue();
        mDownloading = getSyncMap();
        mPause = getSyncMap();
        mFailed = getSyncMap();
    }

    void executeTask(Task task, Action action) {
        Status status = task.getStatus();
        if (action == null) {
            action = Action.guessUserAction(status);
        }
        switch (action) {
            case RETRY:
            case CONTINUE:
            case DOWNLOAD:
                download(task, status);
                break;
            case CANCEL:
                cancel(task, status);
                break;
            case PAUSE:
                pause(task, status);
                break;
            case OPEN:
                open(task, status);
                break;
        }

        // notify
        task.notifyChanged(mConfig.getScheduler(), action, true);
    }

    private void download(Task task, Status status) {
        if (!status.checkoutDownload()) return;
    }

    private void cancel(Task task, Status status) {
        if (!status.checkoutCancel()) return;
    }

    private void pause(Task task, Status status) {
        if (!status.checkoutPause()) return;
    }

    private void open(Task task, Status status) {
        if (!status.checkoutOpen()) return;
    }

    private Map<String, Task> getSyncMap() {
        return Collections.synchronizedMap(new HashMap<String, Task>());
    }

}
