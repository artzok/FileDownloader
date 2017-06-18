package com.artzok.downloader.core;

import com.artzok.downloader.FileDownloader;
import com.artzok.downloader.thread.Scheduler;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Future;

class TaskRunnable implements Runnable {
    private Future mFuture;
    private Task mTask;
    private TaskMeta mTaskMeta;
    private int mProgressCount;
    private DataSource mDownload;
    private FileDownloader.ProgressType mType;
    private File mDownloadDir;
    private Scheduler mScheduler;

    TaskRunnable(Task task, Config config) {
        mTask = task;
        mTaskMeta = task.getTaskMeta();
        mDownload = config.getDataSource();
        mType = config.getProgressType();
        mProgressCount = config.getProgressCount();
        mDownloadDir = config.getDownloadDir();
        mScheduler = config.getScheduler();
    }

    void setFuture(Future f) {
        this.mFuture = f;
    }

    public Task getTask() {
        return mTask;
    }

    @Override
    public void run() {
        File file = null;
        InputStream is = null;
        RandomAccessFile accessFile = null;
        // 开始下载
        mTask.notifyChanged(mScheduler, Action.START);

        try {
            // 禁止响应
//            mTask.setLockSelfResp(true);
            // 更新文件大小
            long cLength = Utils.getDataLength(mTaskMeta.getDownloadUrl());
            mTaskMeta.setFileSize(cLength);
            if (mType == FileDownloader.ProgressType.TIMES)
                mProgressCount = Math.max((int) // update times
                        (cLength * 1.0f / mProgressCount), 100 * 1024);

            // 创建文件
            file = new File(mDownloadDir, mTaskMeta.getFileName());
            if (!mDownloadDir.exists()) mDownloadDir.mkdirs();

            if (file.exists()) {
                mTask.setSoFarSize(file.length());  // 更新已经下载的大小
                if (mTask.getSoFarSize() > mTaskMeta.getFileSize()) {
                    if (!file.delete()) {
                        throw new RuntimeException("Can't delete invalid file.");
                    }
                    mTask.setSoFarSize(0); // 删除并重置大小
                } else if (mTask.getSoFarSize() == mTaskMeta.getFileSize()) {
                    // 通知下载已经完成
                    mTask.notifyChanged(mScheduler, Action.CHANGED);
                    return;
                }
                // 文件存在性已经改变，状态也可能发生改变
                mTask.notifyChanged(mScheduler, Action.CHANGED);
            } else {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Can't create new file.");
                }
                mTask.setSoFarSize(0);
            }

            // 获取数据源之前检查是否已经中断
            if (Thread.interrupted()) return;

            // 获取数据源
            is = mDownload.fetchRangeData(mTaskMeta.getDownloadUrl(),
                    mTask.getSoFarSize(), mTaskMeta.getFileSize());

            // 创建随机读写文件
            accessFile = new RandomAccessFile(file, "rwd");
            accessFile.seek(mTask.getSoFarSize());

            int length = 0;
            long interval = 0L;
            byte[] buffer = new byte[4096];
//            mTask.setLockSelfResp(false);

            while (true) {
                // 读取数据之前检查是否已经中断
                if (Thread.interrupted()) return;

                length = is.read(buffer);

                // -1表明下载完成
                if (length == -1) break;

                // 写入数据
                accessFile.write(buffer, 0, length);

                //　递增更新间隔
                interval += length;

                // 更新
                if (interval > mProgressCount) {
                    mTask.setSoFarSize(mTask.getSoFarSize() + interval);
                    mTask.notifyChanged(mScheduler, Action.UPDATE);
                    interval = 0L;
                }
            }

            if (mTask.getSoFarSize() != mTaskMeta.getFileSize()) {
                mTaskMeta.setFileSize(mTask.getSoFarSize());
            }

            // isDone可用
            mFuture.cancel(true);

            // 下载成功通知
            if (file.exists()) {
                mTask.setFinishTime(System.currentTimeMillis());
                mTask.notifyChanged(mScheduler, Action.DOWNLOAD_SUCCEED);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            // notify all register when occur some error
//            mTask.setLockSelfResp(false);
            mTask.notifyChanged(mScheduler, Action.DOWNLOAD_FAILED);
        } finally {
            safeRelease(is, accessFile);
        }
    }

    private void safeRelease(InputStream is, RandomAccessFile accessFile) {
        try {
            if (is != null)
                is.close();
            if (accessFile != null)
                accessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
