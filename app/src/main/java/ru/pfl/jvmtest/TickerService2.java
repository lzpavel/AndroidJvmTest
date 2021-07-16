package ru.pfl.jvmtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class TickerService2 extends Service {

    MyBinder myBinder = new MyBinder();

    int tickCnt = 0;
    boolean tickEn = false;
    Thread myThread = null;
    final String LOG_TAG = "myLogs";
    Intent intentTick = new Intent("ru.pfl.jvmtest.TickerService.Tx");


    public TickerService2() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Ticker onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Ticker onStartCommand");


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.d(LOG_TAG, "Ticker onBind");
        return myBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(LOG_TAG, "Ticker onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Ticker onUnbind");
        return super.onUnbind(intent);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Ticker onDestroy");
    }

    void startServiceForeground() {

        NotificationManager mNotificationManager;

        Intent notificationIntent = new Intent(this, this.getClass());
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);


            String channelId = "TickerNotify";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Ticker notification",
                    NotificationManager.IMPORTANCE_HIGH);
            //mNotificationManager.createNotificationChannel(channel);
            //mBuilder.setChannelId(channelId);

            Notification notification =
                    new Notification.Builder(this, channelId)
                            .setContentTitle("Ticker service")
                            .setContentText("Running")
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentIntent(pendingIntent)
                            //.setTicker(getText(R.string.ticker_text))
                            .setChannelId(channelId)
                            .build();
        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel);

        /*Notification notification =
                new Notification.Builder(this, "notify_002")
                        .setContentTitle("Ticker service")
                        .setContentText("Running")
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        //.setTicker(getText(R.string.ticker_text))
                        .build();*/



        // Notification ID cannot be 0.
        startForeground(102, notification);


    }

    void stopServiceForeground() {
        stopForeground(true);
    }

    void startTicker() {

        initTicker();
        if (myThread != null) {
            if (myThread.getState() == Thread.State.NEW) {
                myThread.start();
            } else {
                Log.d(LOG_TAG, "Ticker thread state no new");
            }
        } else {
            Log.d(LOG_TAG, "Ticker thread is null");
        }

    }

    void initTicker() {

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
                Log.d(LOG_TAG, "Ticker exit loop");
            }
        });
    }

    class MyBinder extends Binder {
        TickerService2 getService() {
            return TickerService2.this;
        }
    }

}