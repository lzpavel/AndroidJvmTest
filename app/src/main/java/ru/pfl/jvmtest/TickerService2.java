package ru.pfl.jvmtest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class TickerService2 extends Service {

    int tickCnt = 0;
    boolean tickEn = false;
    Thread myThread = null;
    final String LOG_TAG = "myLogs";
    public static final String TICK_BROADCAST_ID = "ru.pfl.jvmtest.TickerService.Tx";
    Intent intentTick = new Intent(TICK_BROADCAST_ID);
    TickerBroadcastReceiver broadcastReceiver = new TickerBroadcastReceiver();
    boolean broadcastReceiverRegistered = false;

    public TickerService2() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        tickEn = intent.getBooleanExtra("TickEnable", false);
        if (tickEn) {
            registerBroadcastReceiver();
            initTicker();
            startTicker();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void initTicker() {
        if (myThread == null) {
            myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (tickEn) {
                        tickCnt++;
                        intentTick.putExtra("TickVal", String.valueOf(tickCnt));
                        sendBroadcast(intentTick);
                        Log.d(LOG_TAG, "Tick!");
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    void startTicker() {
        if (myThread != null) {
            myThread.start();

        }
    }

    public void registerBroadcastReceiver() {
        if (!broadcastReceiverRegistered) {
            this.registerReceiver(broadcastReceiver, new IntentFilter("ru.pfl.jvmtest.TickerService.Rx"));
            Log.d(LOG_TAG, "Broadcast registered");
            broadcastReceiverRegistered = true;
        }
    }

    public void unregisterBroadcastReceiver() {
        if (broadcastReceiverRegistered) {
            this.unregisterReceiver(broadcastReceiver);
            Log.d(LOG_TAG, "Broadcast unregistered");
            broadcastReceiverRegistered = false;
        }
    }

    public class TickerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            tickEn = intent.getBooleanExtra("TickEnable", false);
            Log.d(LOG_TAG, "Broadcast cmd received");
        }
    }

}