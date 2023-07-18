package com.bakiatmaca.poc.embedded.nodejs.embed;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.io.File;

public class NodejsService extends Service {

    private static final String TAG = "NodejsService";

    public static final int ACTION_EXTRACT = 1400;
    public static final int ACTION_RUN = 4000;
    public static final int ACTION_EXTRACT_AND_RUN = 5000;

    private static NativeNode nativeNode;
    private File extractPath;

    private static final String VersionFile = "app_2.0.7";
    private ServiceHandler serviceHandler;
    private Looper serviceLooper;
    private Messenger messenger;

    public NodejsService() {

    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case ACTION_EXTRACT_AND_RUN:
                        Log.i(TAG, "ACTION_EXTRACT_AND_RUN");
                    case ACTION_EXTRACT:
                        Log.i(TAG, "ACTION_EXTRACT");

                        /*
                            if (!(new File(extractPath, "app/meta/" + VersionFile)).exists()) {
                                ContentExtractor.copyAssets(getApplicationContext(), extractPath);
                                sendState(ACTION_EXTRACT);
                            }
                         */

                        ContentExtractor.copyAssets(getApplicationContext(), extractPath);
                        sendState(ACTION_EXTRACT);


                        if (msg.what == ACTION_EXTRACT)
                            break;
                    case ACTION_RUN:
                        Log.i(TAG, "ACTION_RUN");
                        File f = new File(extractPath, "app/index.js");
                        if (f.exists()) {
                            sendState(ACTION_RUN);
                            if (nativeNode.isRunning()) {
                                Log.w(TAG, "node instance already running");
                            } else {
                                Log.i(TAG, "node running " + f.getAbsolutePath());
                                nativeNode.startWithArguments(new String[]{"node", f.getAbsolutePath()});
                                Log.d(TAG, "service de-attach");
                            }
                        } else {
                            Log.w(TAG, "file not exists " + f.getAbsolutePath());
                        }
                        break;
                    default:
                        Log.d(TAG, "default NO ACTION");
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //Log.d(TAG, "service stopSelf!");
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "service onCreate");

        extractPath = getFilesDir();

        HandlerThread thread = new HandlerThread("NodejsService", Thread.NORM_PRIORITY);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        messenger = new Messenger(serviceHandler);

        //create native instance
        if (nativeNode == null) {
            nativeNode = new NativeNode();

            //load native libs
            nativeNode.init();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service starting");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        /*
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);
        */

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "service binding");
        return messenger.getBinder();
    }

    /*
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "service onTaskRemoved");

        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }
    */

    @Override
    public void onDestroy() {
        Log.d(TAG, "service onDestroy");
    }

    private void sendState(final int state) {
        /*
        Intent intent = new Intent();
        intent.setAction(ACTION_ON_CHANGED_STATE);
        intent.putExtra("ON_CHANGED_STATE", state);
        LocalBroadcastManager.getInstance(NodejsService.this).sendBroadcast(intent);
        */
    }
}