package com.xalanq.vpn4over6;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

class NetworkState {
    private static final String TAG = NetworkState.class.getSimpleName();

    private static NetworkState instance;

    private boolean hasIPV6;
    private NetworkInfo networkInfo;
    private String ipv6;
    private int ipv6port;
    private long startTime;

    private NetworkState() {}

    static NetworkState getInstance() {
        if (instance == null) {
            instance = new NetworkState();
            instance.reset();
        }
        return instance;
    }

    void reset() {
        networkInfo = null;
        hasIPV6 = false;
        ipv6 = "2402:f000:4:72:808::9a47";
        ipv6port = 5678;
        startTime = 0;
    }

    void start() {
        startTime = System.currentTimeMillis();
    }

    NetworkState update(@NonNull Context context) {
        updateNetworkState(context);
        return this;
    }

    private void updateNetworkState(@NonNull Context context) {
        networkInfo = null;
        hasIPV6 = false;
        final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            networkInfo = cm.getActiveNetworkInfo();
        }
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            if (networkInterfaces != null) {
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface ni = networkInterfaces.nextElement();
                    for (InterfaceAddress address: ni.getInterfaceAddresses()) {
                        if (address.getAddress() instanceof Inet6Address) {
                            hasIPV6 = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "updateNetworkState: " + e.toString());
        }
    }

    void updateUI(@NonNull TextView textView) {
        String runningTime = "未运行";
        if (startTime != 0) {
            long diff = (System.currentTimeMillis() - startTime) / 1000;
            long H = diff / 60 / 60;
            diff -= H * 60 * 60;
            long M = diff / 60;
            diff -= M * 60;
            long S = diff;
            runningTime = String.format(Locale.CHINESE, "%02d:%02d:%02d", H, M, S);
        }

        String networkState = "未知";
        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                switch (networkInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        networkState = "WIFI";
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        networkState = "数据流量";
                        break;
                    default:
                        networkState = "未知网络";
                        break;
                }
            } else {
                networkState = "网络未连接";
            }
        }
        textView.setText(
            String.format(
                Locale.CHINA,
                "运行时间: %s\n" +
                    "网络状态: %s   是否支持 IPV6: %s\n" +
                    "服务器 IPV6 地址: [%s]:%d\n",
                runningTime,
                networkState,
                hasIPV6 ? "是" : "否",
                ipv6,
                ipv6port
            )
        );
    }

    String getIpv6() { return ipv6; }
    int getIpv6port() { return ipv6port; }
}
