package com.xalanq.vpn4over6;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    static private final String TAG = MainActivity.class.getSimpleName();

    TextView textViewNetwork;
    TextView textViewFlow;
    private TextView textViewLog;
    private SwitchMaterial trigger;
    private ServiceConnection vpn4Over6ServiceConnection;
    private Vpn4Over6Service vpn4Over6Service;
    private long startTime;
    BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        textViewNetwork = findViewById(R.id.network);
        textViewFlow = findViewById(R.id.flow);
        textViewLog = findViewById(R.id.log);
        textViewLog.setMovementMethod(new ScrollingMovementMethod());
        textViewLog.setTextIsSelectable(true);
        textViewFlow.setText(new FlowStat().getFlowStat(null, null));
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                textViewNetwork.setText(NetworkState.getNetworkState(context));
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(br, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem item = menu.findItem(R.id.item_trigger);
        item.setActionView(R.layout.trigger);
        trigger = item.getActionView().findViewById(R.id.trigger);
        trigger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try_connect();
                } else {
                    disconnect();
                }
            }
        });
        return true;
    }

    void log(String msg) {
        final TextView view = textViewLog;
        view.append(new SimpleDateFormat("hh:mm:ss", Locale.CHINA).format(new Date()));
        view.append(" ");
        view.append(msg);
        view.append("\n");
        final Layout layout = view.getLayout();
        if (layout != null) {
            final int scrollAmount = layout.getLineTop(view.getLineCount()) - view.getHeight();
            if (scrollAmount > 0)
                view.scrollTo(0, scrollAmount);
        }
    }

    void try_connect() {
        Log.d(TAG, "try_connect");;
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            Log.d(TAG, "try_connect request vpn");;
            startActivityForResult(intent, 0);
        } else {
            Log.d(TAG, "try_connect already requested");;
            onActivityResult(0, RESULT_OK, null);
        }
    }

    void connect() {
        Log.d(TAG, "connect");;
        log("启动！");
        startTime = System.currentTimeMillis();
        vpn4Over6Service.connect();
    }

    void disconnect() {
        Log.d(TAG, "disconnect");
        vpn4Over6Service.disconnect();
        textViewFlow.setText(new FlowStat().getFlowStat(null, null));
        startTime = 0;
        unbindService(vpn4Over6ServiceConnection);
        log("关闭...");
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == 0 && result == RESULT_OK) {
            Log.d(TAG, "onActivityResult");
            vpn4Over6ServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(TAG, "onServiceConnected");

                    Vpn4Over6Service.Binder binder = (Vpn4Over6Service.Binder) service;
                    vpn4Over6Service = binder.getService();
                    vpn4Over6Service.setListener(new Vpn4Over6Service.Listener() {
                        @Override
                        public void log(String msg) {
                            Log.d(TAG, "listener log: " + msg);
                            if (startTime != 0) {
                                MainActivity.this.log(msg);
                            }
                        }

                        @Override
                        public void off(String msg) {
                            Log.d(TAG, "listener off: " + msg);
                            if (startTime != 0) {
                                MainActivity.this.log(msg);
                                trigger.setChecked(false);
                            }
                        }

                        @Override
                        public void stat(FlowStat stat) {
                            Log.d(TAG, "listener stat: " + stat.toString());
                            if (startTime != 0) {
                                String runningTime = "未运行";
                                long diff = (System.currentTimeMillis() - startTime) / 1000;
                                long H = diff / 60 / 60;
                                diff -= H * 60 * 60;
                                long M = diff / 60;
                                diff -= M * 60;
                                long S = diff;
                                runningTime = String.format(Locale.CHINESE, "%02d:%02d:%02d", H, M, S);
                                textViewFlow.setText(stat.getFlowStat(runningTime, vpn4Over6Service.VpnIPv4));
                            }
                        }
                    });
                    connect();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "onServiceDisconnected");
                }
            };
            Intent intent = new Intent(this, Vpn4Over6Service.class);
            bindService(intent, vpn4Over6ServiceConnection, Service.BIND_AUTO_CREATE);
        } else {
            super.onActivityResult(request, result, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vpn4Over6ServiceConnection != null) {
            unbindService(vpn4Over6ServiceConnection);
        }
    }
}
