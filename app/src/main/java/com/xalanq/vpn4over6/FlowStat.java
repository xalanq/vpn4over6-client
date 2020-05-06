package com.xalanq.vpn4over6;

import androidx.annotation.NonNull;

import java.util.Locale;

class FlowStat {
    private long downloadTotalBytes;
    private long downloadTotalPackets;
    private long downloadBytesPerSec;
    private long uploadTotalBytes;
    private long uploadTotalPackets;
    private long uploadBytesPerSec;

    FlowStat() {}

    FlowStat(
        long downloadTotalBytes,
        long downloadTotalPackets,
        long downloadBytesPerSec,
        long uploadTotalBytes,
        long uploadTotalPackets,
        long uploadBytesPerSec) {

        this.downloadTotalBytes = downloadTotalBytes;
        this.downloadTotalPackets = downloadTotalPackets;
        this.downloadBytesPerSec = downloadBytesPerSec;
        this.uploadTotalBytes = uploadTotalBytes;
        this.uploadTotalPackets = uploadTotalPackets;
        this.uploadBytesPerSec = uploadBytesPerSec;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
            Locale.CHINA,
            "上传: %d 个包 / %d 字节 / %d B/s\n" +
                "下载: %d 个包 / %d 字节 / %d B/s\n",
            uploadTotalPackets,
            uploadTotalBytes,
            uploadBytesPerSec,
            downloadTotalPackets,
            downloadTotalBytes,
            downloadBytesPerSec
        );
    }
}
