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

class NetworkState {
    static final private String TAG = "NetworkState";
    private static NetworkState instance;

    private boolean hasIPV6;
    private NetworkInfo networkInfo;
    private String ipv4;
    private String ipv6;

    private NetworkState() {}

    static NetworkState getInstance() {
        if (instance == null) {
            instance = new NetworkState();
            instance.ipv4 = "未知";
        }
        return instance;
    }

    NetworkState update(@NonNull Context context) {
        updateNetworkState(context);
        return this;
    }

    private void updateNetworkState(@NonNull Context context) {
        networkInfo = null;
        hasIPV6 = false;
        ipv6 = "未知";
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
                            ipv6 = address.getAddress().toString();
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
                "网络状态: %s   是否支持 IPV6: %s\n" +
                    "下联 IPV4 地址: %s\n" +
                    "上联 IPV6 地址: %s\n",
                networkState,
                hasIPV6 ? "是" : "否",
                ipv4,
                ipv6
            )
        );
    }
}
