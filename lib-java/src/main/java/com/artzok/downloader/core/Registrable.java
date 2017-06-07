package com.artzok.downloader.core;

/*可注册对象，用于监听任务动作和状态变化*/
public interface Registrable {
    void onStatusChanged(Task task, Action action);
}
