package com.xalanq.vpn4over6;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Locale;

class FlowState {
    private static FlowState instance;
    class State {
        long totalBytes;
        long totalPackets;
        long bytesPerSecond;
    }

    private State upload;
    private State download;

    private FlowState() {}

    public static FlowState getInstance() {
        if (instance == null) {
            instance = new FlowState();
            instance.reset();
        }
        return instance;
    }

    FlowState reset() {
        upload = new State();
        download = new State();
        return this;
    }

    FlowState start(@NonNull Context context) {
        return this;
    }

    FlowState update(@NonNull Context context) {
       return this;
    }

    FlowState updateUI(@NonNull TextView textView) {
        textView.setText(
            String.format(
                Locale.CHINESE,
                "上传: %d 个包  %d 字节  %d B/s\n" +
                    "下载: %d 个包  %d 字节  %d B/s\n",
                upload.totalPackets,
                upload.totalPackets,
                upload.bytesPerSecond,
                download.totalPackets,
                download.totalPackets,
                download.bytesPerSecond
            )
        );
        return this;
    }
}
