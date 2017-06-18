package com.artzok.downloader;

import com.artzok.downloader.core.Config;
import com.artzok.downloader.core.DataSource;
import com.artzok.downloader.core.Registrable;
import com.artzok.downloader.core.Task;
import com.artzok.downloader.core.TaskManager;

import java.io.File;
/**
 * 1. 断点续传
 * 2. 暂停、继续、失败重试
 * 3. 全局状态、动作监听
 * 4. ui动态更新
 */
public class FileDownloader {
    /*用户配置*/
    private Config mConfig;

    private TaskManager mTaskManager;

    private FileDownloader(Config config) {
        mConfig = config;
    }

    /*添加下载任务*/
    public void addTask(Task task, Registrable register) {
    }

    /*取消下载任务*/
    public void cancelTask(Task task) {
    }

    /*（强制）关闭所有任务*/
    public void shutDownAll(boolean force) {
    }

    public enum ProgressType {
        BYTES, TIMES
    }

    public TaskManager getTaskManager() {
        return mTaskManager;
    }

    public Config getConfig() {
        return mConfig;
    }

    public void setConfig(Config config) {
        mConfig = config;
    }

    public static class Builder {
        private Config mConfig;

        public Builder() {
            mConfig = new Config();
        }

        public Builder setDownloadDir(File downloadDir) {
            mConfig.setDownloadDir(downloadDir);
            return this;
        }

        public Builder setExtName(String extName) {
            mConfig.setExtName(extName);
            return this;
        }

        public Builder addGlobalRegister(Registrable register) {
            mConfig.addGlobalRegister(register);
            return this;
        }

        public Builder setMaxCount(int maxCount) {
            mConfig.setMaxCount(maxCount);
            return this;
        }

        public Builder setDataSource(DataSource dataSource) {
            mConfig.setDataSource(dataSource);
            return this;
        }

        public Builder setProgressType(FileDownloader.ProgressType progressType) {
            mConfig.setProgressType(progressType);
            return this;
        }

        public Builder setProgressCount(int progressCount) {
            mConfig.setProgressCount(progressCount);
            return this;
        }
    }
}
