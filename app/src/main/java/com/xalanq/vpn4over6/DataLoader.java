package com.xalanq.vpn4over6;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Scanner;

class DataLoader {
    static private final String TAG = "DataLoader";
    static private final String SOCKET_NAME = "com.xalanq.vpn4over6";
    static private final int TYPE_CLOSE = 0;
    static private final int TYPE_LOG = 1;
    static private final int TYPE_IP = 2;

    private Thread threadLocal;
    private Thread threadBackend;
    private boolean running;
    private LocalServerSocket serverSocket;

    DataLoader() {
        try {
            serverSocket = new LocalServerSocket(SOCKET_NAME);
        } catch (IOException e) {
            Log.d(TAG, "DataHandler: server socket failed" + e.toString());
        }
    }

    void start(final Handler handler) {
        running = true;
        threadLocal = new Thread() {
            @Override
            public void run() {
                LocalSocket socket = null;
                try {
                    Log.d(TAG, "run: listening");
                    socket = serverSocket.accept();
                    Log.d(TAG, "run: accepted!");
                    Scanner in = new Scanner(socket.getInputStream());
                    while (running) {
                        int type = in.nextInt();
                        Log.d(TAG, String.format("run: type: %d", type));
                        switch (type) {
                            case TYPE_LOG:
                                handler.sendMessage(DataHandler.log(in.nextLine()));
                                break;
                            case TYPE_IP:
                                String ip = in.next();
                                String route = in.next();
                                String dns1 = in.next();
                                String dns2 = in.next();
                                String dns3 = in.next();
                                handler.sendMessage(DataHandler.ip(ip, route, dns1, dns2, dns3));
                                break;
                            case TYPE_CLOSE:
                                running = false;
                                break;
                            default:
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "run: exception" + e.toString());
                    handler.sendMessage(DataHandler.off(e.toString()));
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                            Log.d(TAG, "run: close socket");
                        } catch(Exception e){
                            Log.e(TAG, "run: close socket " + e.toString());
                            handler.sendMessage(DataHandler.off(e.toString()));
                        }
                    }
                }
            }
        };
        threadLocal.start();

        threadBackend = new Thread() {
            @Override
            public void run() {
                try {
                    int ret = Backend.connectLocalSocket(SOCKET_NAME);
                    if (ret != 0) {
                        throw new RuntimeException("日志初始化出错");
                    }
                    final NetworkState s = NetworkState.getInstance();
                    ret = Backend.serve(s.getIpv6(), s.getIpv6port());
                    if (ret != 0) {
                        throw new RuntimeException("后台终止");
                    }
                } catch (Exception e) {
                    handler.sendMessage(DataHandler.off(e.toString()));
                }
            }
        };
        threadBackend.start();
    }

    void destroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                Log.d(TAG, "destroy: close server socket");
            } catch (Exception e) {
                Log.e(TAG, "destroy: " + e.toString());
            }
        }
    }

    void stop() {
        running = false;
        Log.d("xalanq", "stop: ");
        if (threadLocal != null) {
            threadLocal.interrupt();
            threadLocal = null;
        }
        if (threadBackend != null) {
            threadBackend.interrupt();
            threadBackend = null;
            Backend.stop();
        }
    }
}
