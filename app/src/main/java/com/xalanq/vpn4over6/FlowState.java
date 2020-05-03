package com.xalanq.vpn4over6;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Locale;

class FlowState {
    private static FlowState instance;

    static class State {
        long totalBytes;
        long totalPackets;
        long bytesPerSecond;
    }

    private State upload;
    private State download;

    private FlowState() {}

    static FlowState getInstance() {
        if (instance == null) {
            instance = new FlowState();
            instance.reset();
        }
        return instance;
    }

    void reset() {
        upload = new State();
        download = new State();
    }

    FlowState update(@NonNull Context context) {
       return this;
    }

    void updateUI(@NonNull TextView textView) {
        textView.setText(
            String.format(
                Locale.CHINA,
                "上传: %d 个包 / %d 字节 / %d B/s\n" +
                    "下载: %d 个包 / %d 字节 / %d B/s\n",
                upload.totalPackets,
                upload.totalBytes,
                upload.bytesPerSecond,
                download.totalPackets,
                download.totalBytes,
                download.bytesPerSecond
            )
        );
    }
}
