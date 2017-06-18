package com.artzok.downloader.core;

import com.artzok.downloader.FileDownloader;
import com.artzok.downloader.defaults.Register;
import com.artzok.downloader.thread.Scheduler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Task {
    /*用于保存所有任务，方便快速查询*/
    private static Map<String, Task> sTasks =
            Collections.synchronizedMap(new HashMap<String, Task>());

    /*用于查询单个任务*/
    public static Task getTask(String uuid) {
        return sTasks.get(uuid);
    }

    /*获取所有任务的副本*/
    public static Collection<Task> copyAllTask() {
        return new ArrayList<>(sTasks.values());
    }

    /*一个任务的必要信息（元信息）*/
    private TaskMeta mTaskMeta;

    /*用于保存自定参数*/
    private Map<String, Object> mParams;

    /*当前任务的最新状态*/
    private Status mStatus;
    /*当前已经下载大小*/
    private long mSoFarSize;
    /*任务创建时间*/
    private long mCreateTime;
    /*开始下载时间*/
    private long mStartTime;
    /*下载完成时间*/
    private long mFinishTime;

    /*本任务的所有注册者(监听进度、行为、状态的注册者)*/
    private HashMap<Integer, WeakReference<? extends Registrable>> mRegisters;

    private FileDownloader mFileDownloader;

    public Task(TaskMeta meta, FileDownloader downloader) {
        mTaskMeta = meta;
        mParams = new HashMap<>();
        mStatus = Status.UNDEF;
        mSoFarSize = 0;
        mCreateTime = System.currentTimeMillis();
        mStartTime = 0L;
        mFinishTime = 0L;
        mFileDownloader = downloader;
        sTasks.put(getUUID(), this);
    }

    /*注册者与关联的任务建立观察者关系*/
    public void bind(final Register register) {
        if (register == null) {
            throw new RuntimeException("参数不能为null");
        }

        // 注册者与前任脱离并与现任xxoo
        register.bind(this);

        // 任务与注册者绑定
        mRegisters.put(register.hashCode(),
                new WeakReference<Registrable>(register));

        // 新添加的任务需要初始化一下状态
        if (mStatus == Status.UNDEF) {
            mStatus = Status.query(this);
        }

        // 对刚刚绑定的注册者回调一下，更新ui
        register.onStatusChanged(this, Action.CHANGED);
    }

    /*注册者与任务脱离*/
    public void unBind(final Register register) {
        if (register == null) {
            throw new RuntimeException("参数不能为null");
        }
        mRegisters.remove(register.hashCode());
    }

    /*在指定线程通知所有注册者*/
    void notifyChanged(Scheduler scheduler, final Action action) {
        if (action == Action.CHANGED) mStatus = Status.query(this);
        scheduler.execute(new Runnable() {
            public void run() {
                for (int hasCode : mRegisters.keySet()) {
                    WeakReference<? extends Registrable> ref
                            = mRegisters.get(hasCode);
                    Registrable register = ref.get();
                    if (register != null) {
                        register.onStatusChanged(Task.this, action);
                    } else {
                        mRegisters.remove(hasCode);
                    }
                }
            }
        });
    }

    public long getSoFarSize() {
        return mSoFarSize;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public TaskMeta getTaskMeta() {
        return mTaskMeta;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getFinishTime() {
        return mFinishTime;
    }

    public void setSoFarSize(long soFarSize) {
        mSoFarSize = soFarSize;
    }

    public void setCreateTime(long createTime) {
        mCreateTime = createTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public void setFinishTime(long finishTime) {
        mFinishTime = finishTime;
    }

    public void addParams(String key, Object obj) {
        mParams.put(key, obj);
    }

    public Object getParams(String key) {
        return mParams.get(key);
    }

    public Object removeParams(String key) {
        return mParams.remove(key);
    }

    public Status getStatus() {
        return mStatus;
    }

    public String getUUID() {
        return mTaskMeta.getDownloadUrl();
    }
}
