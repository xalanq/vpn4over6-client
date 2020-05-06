package com.xalanq.vpn4over6;

import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.StringReader;
import java.util.Scanner;

public class Vpn4Over6Service extends VpnService {
    static private final String TAG = Vpn4Over6Service.class.getSimpleName();
    static final String IPv6 = "2402:f000:4:72:808::9a47";
    static final int IPv6Port = 5678;

    private ParcelFileDescriptor tunnel;
    private DataHandler handler;
    private DataLoader loader;
    String VpnIPv4;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        handler = new DataHandler(this);
        loader = new DataLoader(handler);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        disconnect();
        loader.destroy();
    }

    void connect() {
        Log.d(TAG, "connect");
        loader.connect();
    }

    void disconnect() {
        Log.d(TAG, "disconnect: close tunnel");
        if (tunnel != null) {
            try {
                tunnel.close();
            } catch (Exception e) {
                Log.e(TAG, "disconnect: unable to close tunnel");
            }
            tunnel = null;
        }
        VpnIPv4 = null;
        loader.disconnect();
    }

    int buildTunnel(String msg) {
        Log.d(TAG, "connect: get tun fd");

        Scanner in = new Scanner(new StringReader(msg));
        String ip = in.next();
        String route = in.next();
        String dns1 = in.next();
        String dns2 = in.next();
        String dns3 = in.next();
        VpnIPv4 = ip;
        int fd = in.nextInt();

        protect(fd);

        Builder builder = new Builder();
        tunnel = builder
            .addAddress(ip , 24)
            .addRoute(route, 0)
            .addDnsServer(dns1)
            .addDnsServer(dns2)
            .addDnsServer(dns3)
            .establish();

        try {
            fd = tunnel.getFd();
            Log.d(TAG, "connect: fd is " + fd);
            return fd;
        } catch (Exception e) {
            Log.e(TAG, "buildTunnel: " + e.toString());
            handler.sendMessage(DataHandler.off("无法开启 VPN"));
        }
        return -1;
    }

    void writeFd(String data) {
        int fd = buildTunnel(data);
        if (fd >= 0) {
            loader.writeFd(fd);
        }
    }

    void run() {
        loader.run();
    }

    ///////// Listener

    public interface Listener {
        void off(String msg);
        void log(String msg);
        void stat(FlowStat stat);
    }

    Listener listener;

    void setListener(Listener listener) {
        this.listener = listener;
    }

    ///////// Binder

    class Binder extends android.os.Binder {
        Vpn4Over6Service getService() {
            return Vpn4Over6Service.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        listener = null;
        return super.onUnbind(intent);
    }
}
