package com.mega.megagps;

import android.os.Handler;
import com.mega.graphics.Threads.MainThread;

public class WatchThread extends MainThread{
    public WatchThread(Handler handler) {
        super(handler);
    }
    @Override
    public void ThreadBody(Handler handler) {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {

        }
        super.ThreadBody(handler);
    }
}
