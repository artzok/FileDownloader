package com.artzok.downloader.thread;

public interface Scheduler {
    void execute(Runnable runnable);

    Scheduler CURRENT_THREAD = new CurrentThread();
    Scheduler NEW_THREAD = new NewThread();

    class CurrentThread implements Scheduler {
        public void execute(Runnable runnable) {
            runnable.run();
        }
    }

    class NewThread implements Scheduler {
        public void execute(Runnable runnable) {
            new Thread(runnable).start();
        }
    }
}
