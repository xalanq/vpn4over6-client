package com.xalanq.vpn4over6;

public class Backend {
    public static native int loggerConnect(String socketName); // 连接到本地日志的 socket
    public static native void clientConnect(String IPv6, int port); // 连接到服务器，并请求得到 ip、route 和 dns
    public static native void listeningServer(); // 监听服务器发的消息
    public static native void listeningClient(); // 监听客户端发的消息
    public static native void schedule(); // 周期返回信息并发送心跳包，注意加锁
    public static native void disconnect();

    static {
        System.loadLibrary("corevpn");
    }
}
