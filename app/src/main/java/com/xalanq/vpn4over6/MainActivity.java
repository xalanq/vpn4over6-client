package com.xalanq.vpn4over6;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView textViewNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        textViewNetwork = findViewById(R.id.network);

        NetworkState.getInstance().update(this).updateUI(textViewNetwork);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem item = menu.findItem(R.id.item_trigger);
        item.setActionView(R.layout.trigger);
        return true;
    }
}
