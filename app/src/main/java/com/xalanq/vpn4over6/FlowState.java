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

    static class VPNState {
        String ip;
        String route;
        String dns1;
        String dns2;
        String dns3;

        VPNState(String ip, String route, String dns1, String dns2, String dns3) {
            this.ip = ip;
            this.route = route;
            this.dns1 = dns1;
            this.dns2 = dns2;
            this.dns3 = dns3;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.CHINA,
                "ip: %s, route: %s, dns1: %s, dns2: %s, dns3: %s", ip, route, dns1, dns2, dns3);
        }
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
