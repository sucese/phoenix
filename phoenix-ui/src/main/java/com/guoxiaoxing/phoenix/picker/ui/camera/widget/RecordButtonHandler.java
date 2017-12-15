package com.guoxiaoxing.phoenix.picker.ui.camera.widget;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class RecordButtonHandler extends Handler {

    public static final int WHAT_233=0;
    private long delayTimeInMils;
    private boolean freeNow;
    private Task task;
    private boolean shouldContinue;

    public RecordButtonHandler(Looper looper, Task task) {
        super(looper);
        this.task = task;
        freeNow=true;
        shouldContinue=false;
    }

    public void clearMsg() {
        while (hasMessages(WHAT_233)) {
            removeMessages(WHAT_233);
        }
        shouldContinue = false;
        freeNow = true;
    }

    public void sendSingleMsg(long timeDelayed) {
        clearMsg();
        freeNow = false;
        shouldContinue = false;
        sendEmptyMessageDelayed(0, timeDelayed);
    }

    public void sendLoopMsg(long timeDelayed, long timeDelayedInLoop) {
        clearMsg();
        freeNow = false;
        delayTimeInMils = timeDelayedInLoop;
        shouldContinue = true;
        sendEmptyMessageDelayed(0, timeDelayed);
    }

    public void handleMessage(Message paramMessage) {
        if (task != null) {
            task.run();
        }
        if (shouldContinue) {
            sendEmptyMessageDelayed(0, delayTimeInMils);
        }
    }

    public boolean isFreeNow() {
        return freeNow;
    }

    public interface Task {
        void run();
    }
}
