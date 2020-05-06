package com.xalanq.vpn4over6;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.Scanner;

public class DataHandler extends Handler {
    static final int MSG_OFF = 0;
    static final int MSG_LOG = 1;
    static final int MSG_IP = 2;
    static final int MSG_RUN = 3;
    static final int MSG_STAT = 4;

    private WeakReference<Vpn4Over6Service> serviceWeakReference;

    DataHandler(Vpn4Over6Service service) {
        this.serviceWeakReference = new WeakReference<>(service);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        final Vpn4Over6Service service = serviceWeakReference.get();
        if (service != null) {
            switch (msg.what) {
                case MSG_OFF:
                    service.listener.off((String) msg.obj);
                    break;
                case MSG_LOG:
                    service.listener.log((String) msg.obj);
                    break;
                case MSG_IP:
                    String data = (String) msg.obj;
                    service.listener.log(data);
                    service.writeFd(data);
                    break;
                case MSG_RUN:
                    service.listener.log((String) msg.obj);
                    service.run();
                    break;
                case MSG_STAT:
                    String s = (String)msg.obj;
                    Scanner in = new Scanner(new StringReader(s));
                    service.listener.stat(new FlowStat(
                        in.nextLong(),
                        in.nextLong(),
                        in.nextLong(),
                        in.nextLong(),
                        in.nextLong(),
                        in.nextLong()
                    ));
                    break;
                default:
                    break;
            }
        }
    }

    static Message off(String msg) {
        Message m = new Message();
        m.what = MSG_OFF;
        m.obj = msg;
        return m;
    }

    static Message log(String msg) {
        Message m = new Message();
        m.what = MSG_LOG;
        m.obj = msg;
        return m;
    }

    static Message ip(String msg) {
        Message m = new Message();
        m.what = MSG_IP;
        m.obj = msg;
        return m;
    }

    static Message run(String msg) {
        Message m = new Message();
        m.what = MSG_RUN;
        m.obj = msg;
        return m;
    }

    static Message stat(String msg) {
        Message m = new Message();
        m.what = MSG_STAT;
        m.obj = msg;
        return m;
    }
}
