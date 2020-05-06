package com.xalanq.vpn4over6;

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

    private String parseBytes(long bytes) {
        if (bytes <= 1024) {
            return bytes + " B";
        } else if (bytes <= 1024 * 1024) {
            return String.format(Locale.CHINA, "%.1f KB", bytes / 1024.0);
        } else if (bytes <= 1024 * 1024 * 1024) {
            return String.format(Locale.CHINA, "%.1f MB", bytes / 1024.0 / 1024.0);
        }
        return String.format(Locale.CHINA, "%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0);
    }

    String getFlowStat(String runningTime, String IPv4) {
        return String.format(
            Locale.CHINA,
            "运行时间: %s\n" +
                "服务器 IPv6 地址: [%s]:%d\n" +
                "VPN IPv4 地址: %s\n" +
                "上传: %d 个包 / %s / %s/s\n" +
                "下载: %d 个包 / %s / %s/s\n",
            runningTime == null ? "未运行" : runningTime,
            Vpn4Over6Service.IPv6,
            Vpn4Over6Service.IPv6Port,
            IPv4 == null ? "未知" : IPv4,
            uploadTotalPackets,
            parseBytes(uploadTotalBytes),
            parseBytes(uploadBytesPerSec),
            downloadTotalPackets,
            parseBytes(downloadTotalBytes),
            parseBytes(downloadBytesPerSec)
        );
    }
}
