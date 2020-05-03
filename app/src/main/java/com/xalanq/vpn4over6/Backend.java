package com.xalanq.vpn4over6;

public class Backend {
    public static native int serve(String ipv6, int port);
    public static native int connectLocalSocket(String socketName);
    public static native void stop();

    static {
        System.loadLibrary("corevpn");
    }
}
