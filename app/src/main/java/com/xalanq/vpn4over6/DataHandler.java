package com.xalanq.vpn4over6;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class DataHandler extends Handler {
    static private final int MSG_OFF = 0;
    static private final int MSG_LOG = 1;
    static private final int MSG_IP = 2;

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
}
