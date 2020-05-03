package com.xalanq.vpn4over6;

import android.os.Handler;

class DataLoader {
    private Thread thread;

    void start(final Handler handler) {
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(1000);
                        handler.sendMessage(DataHandler.log("test"));
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        };
        thread.start();
    }

    void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}
