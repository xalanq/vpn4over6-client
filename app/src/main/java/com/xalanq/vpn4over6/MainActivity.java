package com.xalanq.vpn4over6;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView textViewNetwork;
    TextView textViewFlow;
    TextView textViewLog;
    SwitchMaterial trigger;
    Timer timer;
    DataHandler handler;
    DataLoader loader;

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
        handler = new DataHandler(this);
        loader = new DataLoader();
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
                    handler.sendMessage(DataHandler.on("启动！"));
                } else {
                    handler.sendMessage(DataHandler.off("关闭..."));
                }
                if (timer != null)  {
                    timer.cancel();
                }
                startTimer();
            }
        });
        return true;
    }

    void updateUI() {
        NetworkState.getInstance().update(this).updateUI(textViewNetwork);
        FlowState.getInstance().update(this).updateUI(textViewFlow);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer == null) {
            startTimer();
        }
    }

    void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loader.destroy();
    }
}
