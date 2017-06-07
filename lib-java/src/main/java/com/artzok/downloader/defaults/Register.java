package com.artzok.downloader.defaults;

import com.artzok.downloader.core.Registrable;
import com.artzok.downloader.core.Task;

public abstract class Register implements Registrable {
    private Task mTask;

    public final void bind(Task task) {
        if(mTask != null) {
            mTask.unBind(this);
        }
        mTask = task;
    }
}
