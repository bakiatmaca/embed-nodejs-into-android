package com.bakiatmaca.poc.embedded.nodejs.embed;

import android.util.Log;

public class NativeNode {

    private static final String TAG = "NativeNode";
    private volatile static boolean loaded = false;
    private volatile static boolean running = false;

    public NativeNode() {

    }

    public void init() {
        try {
            if (loaded)
                return;

            System.loadLibrary("native-lib");
            System.loadLibrary("node");

            Log.d(TAG, "native-lib loaded");

            loaded = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Integer startWithArguments(String[] arguments) {
        running = true;
        Integer res =  startNodeWithArguments(arguments);

        Log.d(TAG, "exit-code: " + res);

        running = false;
        return res;
    }

    private native Integer startNodeWithArguments(String[] arguments);

}
