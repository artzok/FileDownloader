package com.artzok.downloader.core;


import com.artzok.downloader.FileDownloader;
import com.artzok.downloader.thread.Scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {
    /*下载路径*/
    private File mDownloadDir;

    /*文件扩展名*/
    private String mExtName;

    /*全局注册者*/
    private List<Registrable> mGlobalRegisters;

    /*最大并发数，也就是同时可以下载的文件数*/
    private int mMaxCount;

    /*数据源*/
    private DataSource mDataSource;

    /*进度更新方式*/
    private FileDownloader.ProgressType mProgressType;

    /**
     * 进度更新次数:
     * 当ProgressType等于BYTES时，该属性表示每多少KB更新一次进度
     * 当ProgressType等于TIMES时，该属性表示对于一个下载任务总共更新进度的次数
     */
    private int mProgressCount;

    /**
     * 任务状态、进度、动作改变时的回调方式
     */
    private Scheduler mScheduler;

    public Config() {
        mDownloadDir = new File(System.getProperty("user.download"));
        mExtName = "";
        mGlobalRegisters = new ArrayList<>(2);
        mMaxCount = 2 << 3;
        mDataSource = DataSource.DEFAULT;
        mProgressType = FileDownloader.ProgressType.TIMES;
        mProgressCount = 100;
        mScheduler = Scheduler.CURRENT_THREAD;
    }

    public void checkValid() {
        if(!mDownloadDir.exists()) {
            mDownloadDir.mkdirs();
        }
        if(!mDownloadDir.exists() || mDownloadDir.isFile())
            throw new RuntimeException("Download dir must be an existing directory.");
        // for ext
        if(mMaxCount >= 2 << 10) {
            throw new RuntimeException("The maximum number of concurrent connections must less 1024.");
        }
        if(mProgressType == FileDownloader.ProgressType.TIMES) {
            if(mProgressCount > 100) {
                throw new RuntimeException("The maximum update times must less 100 for a single task.");
            }
        }
        if(mProgressType == FileDownloader.ProgressType.BYTES) {
            if(mProgressCount < 100) {
                throw new RuntimeException("The minimum bytes must great than 100 kb for each update.");
            }
        }
    }

    public File getDownloadDir() {
        return mDownloadDir;
    }

    public void setDownloadDir(File downloadDir) {
        mDownloadDir = downloadDir;
    }

    public String getExtName() {
        return mExtName;
    }

    public void setExtName(String extName) {
        mExtName = extName;
    }

    public List<Registrable> getGlobalRegisters() {
        return mGlobalRegisters;
    }

    public void addGlobalRegister(Registrable globalRegisters) {
        mGlobalRegisters.add(globalRegisters);
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
    }

    public DataSource getDataSource() {
        return mDataSource;
    }

    public void setDataSource(DataSource dataSource) {
        mDataSource = dataSource;
    }

    public FileDownloader.ProgressType getProgressType() {
        return mProgressType;
    }

    public void setProgressType(FileDownloader.ProgressType progressType) {
        mProgressType = progressType;
    }

    public int getProgressCount() {
        return mProgressCount;
    }

    public void setProgressCount(int progressCount) {
        mProgressCount = progressCount;
    }

    public void setScheduler(Scheduler scheduler) {
        mScheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return mScheduler;
    }
}
