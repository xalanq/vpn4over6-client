package com.xalanq.vpn4over6;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

class DataLoader {
    static private final String TAG = "DataLoader";
    static private final String SOCKET_NAME = "com.xalanq.vpn4over6";
    static private final int TYPE_OFF = 0;
    static private final int TYPE_LOG = 1;
    static private final int TYPE_IP = 2;

    private Thread threadLocal;
    private Thread threadBackend;
    private boolean running;
    private LocalServerSocket serverSocket;
    private LocalSocket socket;
    private Handler handler;

    DataLoader(Handler handler) {
        this.handler = handler;
        try {
            serverSocket = new LocalServerSocket(SOCKET_NAME);
        } catch (IOException e) {
            Log.d(TAG, "DataHandler: server socket failed" + e.toString());
        }
    }

    void start() {
        running = true;
        threadLocal = new Thread() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "run: listening");
                    socket = serverSocket.accept();
                    Log.d(TAG, "run: accepted!");
                    Scanner in = new Scanner(socket.getInputStream());
                    while (running) {
                        int type = in.nextInt();
                        Log.d(TAG, String.format("run: type: %d", type));
                        switch (type) {
                            case TYPE_OFF:
                                handler.sendMessage(DataHandler.off(in.nextLine().substring(1)));
                                break;
                            case TYPE_LOG:
                                handler.sendMessage(DataHandler.log(in.nextLine().substring(1)));
                                break;
                            case TYPE_IP:
                                String ip = in.next();
                                String route = in.next();
                                String dns1 = in.next();
                                String dns2 = in.next();
                                String dns3 = in.next();
                                handler.sendMessage(DataHandler.ip(ip, route, dns1, dns2, dns3));
                                break;
                            default:
                        }
                    }
                } catch (Exception e) {
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
                    Log.e(TAG, "run: " + e.toString());
                    try {
                        sleep(500);
                    } catch (Exception ee) {
                        Log.e(TAG, "run: " + ee.toString());
                    }
                    handler.sendMessage(DataHandler.off(e.getMessage()));
                }
            }
        };
        threadBackend.start();
    }

    void writeFd(final int fd) {
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "run: write fd");
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.write(String.format("0x%08x", fd));
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.e(TAG, "run: " + e.toString());
                    handler.sendMessage(DataHandler.off(e.getMessage()));
                }
                Log.d(TAG, "run: write fd done");
            }
        }.start();
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
