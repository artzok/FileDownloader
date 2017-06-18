package com.artzok.downloader.core;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskManager {

    private Config mConfig;

    private ThreadPoolExecutor mThreadPool;
    private BlockingQueue<Runnable> mWaitingTaskQueue;

    private Map<String, Task> mDownloading;
    private Map<String, Task> mPause;
    private Map<String, Task> mFailed;

    private Map<String, WeakReference<Future>> mAllRunningTasks;

    TaskManager(Config conf) {
        mConfig = conf;
        mThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(mConfig.getMaxCount());
        mWaitingTaskQueue = mThreadPool.getQueue();
        mDownloading = getSyncMap();
        mPause = getSyncMap();
        mFailed = getSyncMap();
        mAllRunningTasks = new HashMap<>();
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
                download(action, task, status);
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
        task.notifyChanged(mConfig.getScheduler(), action);
    }

    private void download(Action action, Task task, Status status) {
        if (!status.checkoutDownload()) return;
        if(action == Action.DOWNLOAD) {
            task.setSoFarSize(0);
            task.setStartTime(System.currentTimeMillis());
            TaskRunnable runnable = new TaskRunnable(task, mConfig);
            final Future<?> submit = mThreadPool.submit(runnable);
            runnable.setFuture(submit);
            mAllRunningTasks.put(task.getUUID(), new WeakReference<Future>(submit));
            mPause.remove(task.getUUID());
            mFailed.remove(task.getUUID());
        }
    }

    private void cancel(Task task, Status status) {
        if (!status.checkoutCancel()) return;
        mPause.remove(task.getUUID());
        mFailed.remove(task.getUUID());
        removeTask(task);
        deleteFile(mConfig, task);
    }

    private void pause(Task task, Status status) {
        if (!status.checkoutPause()) return;
        mPause.put(task.getUUID(), task);
        mFailed.remove(task.getUUID());
        removeTask(task);
    }

    private void open(Task task, Status status) {
        if (!status.checkoutOpen()) return;
        task.notifyChanged(mConfig.getScheduler(), Action.OPEN);
    }

    private Map<String, Task> getSyncMap() {
        return Collections.synchronizedMap(new HashMap<String, Task>());
    }

    private void removeTask(Task task) {
        WeakReference<Future> ref = mAllRunningTasks.get(task.getUUID());
        if (ref != null && ref.get() != null) {
            if (!ref.get().isCancelled()) {
                ref.get().cancel(true);
                mWaitingTaskQueue.remove(ref.get());
            }
        }
    }

    private void deleteFile(Config config, Task task) {
        File file = new File(config.getDownloadDir(), task.getTaskMeta().getFileName());
        if(file.exists()) {
            if(file.delete()) {
                task.setSoFarSize(0);
                task.notifyChanged(config.getScheduler(), Action.CHANGED);
            }
        }
    }

    public boolean isPending(Task task) {
        Future f = getFuture(task.getUUID());
        return f != null && mWaitingTaskQueue.contains(f);
    }

    private Future getFuture(String uuid) {
        WeakReference<Future> ref = mAllRunningTasks.get(uuid);
        if(ref != null && ref.get() != null) {
            return ref.get();
        }
        return null;
    }

    public boolean isDownloading(Task task) {
       Future f = getFuture(task.getUUID());
        return f != null && !f.isDone() && !isPending(task) &&
                !isFailed(task.getUUID());
    }

    private boolean isFailed(String uuid) {
        return mFailed.get(uuid) != null;
    }
}
