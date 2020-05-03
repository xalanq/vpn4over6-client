package com.xalanq.vpn4over6;

import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataHandler extends Handler {
    static private final int MSG_ON = 1;
    static private final int MSG_OFF = 2;
    static private final int MSG_IP = 3;
    static private final int MSG_LOG = 4;

    private WeakReference<MainActivity> activityWeakReference;

    DataHandler(MainActivity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        final MainActivity activity = activityWeakReference.get();
        if (activity != null) {
            final TextView view = activity.textViewLog;
            view.append(new SimpleDateFormat("hh:mm:ss", Locale.CHINA).format(new Date()));
            view.append(" ");
            switch (msg.what) {
                case MSG_ON:
                    view.append((String) msg.obj);
                    activity.trigger.setChecked(true);
                    NetworkState.getInstance().start();
                    activity.loader.start();
                    if (activity.timer != null)  {
                        activity.timer.cancel();
                    }
                    activity.startTimer();
                    break;
                case MSG_OFF:
                    view.append((String) msg.obj);
                    activity.trigger.setChecked(false);
                    NetworkState.getInstance().reset();
                    FlowState.getInstance().reset();
                    activity.loader.stop();
                    if (activity.timer != null)  {
                        activity.timer.cancel();
                    }
                    activity.startTimer();
                    break;
                case MSG_IP:
                    view.append(msg.obj.toString());
                    activity.loader.writeFd(11);
                    break;
                case MSG_LOG:
                    view.append((String) msg.obj);
                    break;
                default:
                    break;
            }
            view.append("\n");
            final Layout layout = view.getLayout();
            if (layout != null) {
                final int scrollAmount = layout.getLineTop(view.getLineCount()) - view.getHeight();
                if (scrollAmount > 0)
                    view.scrollTo(0, scrollAmount);
            }
        }
    }

    static Message on(String msg) {
        Message m = new Message();
        m.what = MSG_ON;
        m.obj = msg;
        return m;
    }

    static Message off(String msg) {
        Message m = new Message();
        m.what = MSG_OFF;
        m.obj = msg;
        return m;
    }

    static Message ip(String ip, String route, String dns1, String dns2, String dns3) {
        Message m = new Message();
        m.what = MSG_IP;
        m.obj = new FlowState.VPNState(ip, route, dns1, dns2, dns3);
        return m;
    }

    static Message log(String msg) {
        Message m = new Message();
        m.what = MSG_LOG;
        m.obj = msg;
        return m;
    }
}
