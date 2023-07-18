package com.bakiatmaca.poc.embedded.nodejs.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.bakiatmaca.poc.embedded.nodejs.R;
import com.bakiatmaca.poc.embedded.nodejs.embed.NodejsService;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private Messenger nodeJsServiceMessenger = null;
    private boolean bound;

    private TextView txt_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_info = findViewById(R.id.txt_info);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (nodeJsServiceMessenger != null) {
                        nodeJsServiceMessenger.send(Message.obtain(null, NodejsService.ACTION_EXTRACT_AND_RUN, 0, 0));
                        setINfo("Node.js fired up!");
                    }
                } catch (RemoteException e) {
                    setINfo("Error occurred err:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 4 *1000);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            nodeJsServiceMessenger = new Messenger(service);
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            nodeJsServiceMessenger = null;
            bound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //bind from the service
        bindService(new Intent(this, NodejsService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        //Unbind from the service
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }

        super.onStop();
    }

    private void setINfo(final String info) {
        runOnUiThread(() -> txt_info.setText(info));
    }
}