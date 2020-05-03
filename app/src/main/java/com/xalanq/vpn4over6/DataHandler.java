package com.xalanq.vpn4over6;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataHandler extends Handler {
    static final int MSG_IP = 1;
    static final int MSG_LOG = 2;

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
                case MSG_IP:
                    break;
                case MSG_LOG:
                    view.append((String) msg.obj);
                    break;
                default:
                    break;
            }
            view.append("\n");
            final int scrollAmount = view.getLayout().getLineTop(view.getLineCount()) - view.getHeight();
            if (scrollAmount > 0)
                view.scrollTo(0, scrollAmount);
        }
    }

    static public Message log(String msg) {
        Message m = new Message();
        m.what = MSG_LOG;
        m.obj = msg;
        return m;
    }
}
