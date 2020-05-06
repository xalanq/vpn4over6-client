package com.xalanq.vpn4over6;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

class NetworkState {
    private static final String TAG = NetworkState.class.getSimpleName();

    static String getNetworkState(final Context context) {
        String IPv6 = null;
        try {
            for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                networkInterfaces != null && networkInterfaces.hasMoreElements(); ) {
                NetworkInterface ni = networkInterfaces.nextElement();
                for (Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                    inetAddresses != null && inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                        && !inetAddress.isSiteLocalAddress() && inetAddress instanceof Inet6Address) {
                        IPv6 = inetAddress.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "updateNetworkState: " + e.toString());
        }

        String networkState = "未知";
        final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
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

        return String.format(
            Locale.CHINA,
            "网络状态: %s\n本地 IPv6 地址: %s\n",
            networkState,
            IPv6 == null ? "不支持" : IPv6
        );
    }
}
