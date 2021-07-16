package ru.pfl.jvmtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ServiceControlActivity extends AppCompatActivity {

    Button buttonServiceStart;
    Button buttonServiceStop;
    Button buttonServiceBind;
    Button buttonServiceUnbind;
    Button buttonServiceTickerStart;
    Button buttonServiceTickerStop;
    Button buttonServiceForegroundStart;
    Button buttonServiceForegroundStop;
    TextView textViewServiceCount;

    Intent intent;
    Intent intentTickerService;
    Intent intentTickerService2;
    Intent intentBroadcast;

    ServiceConnection serviceConnection;
    TestService testService;
    TickerService tickerService;
    TickerService2 tickerService2;

    Thread myThread;
    Boolean threadEn = false;
    Boolean tickEn = false;

    int cnt = 0;
    int i = 0;
    boolean bound = false;

    TickBroadcastReceiver tickBroadcastReceiver = new TickBroadcastReceiver();


    //public static final String CHANNEL_ID = "101";
    private static final int NOTIFY_ID = 101;
    private static String CHANNEL_ID = "Cat channel";
    final String LOG_TAG = "myLogs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_control);

        buttonServiceStart = (Button) findViewById(R.id.buttonServiceStart);
        buttonServiceStop = (Button) findViewById(R.id.buttonServiceStop);
        buttonServiceBind = (Button) findViewById(R.id.buttonServiceBind);
        buttonServiceUnbind = (Button) findViewById(R.id.buttonServiceUnbind);
        buttonServiceTickerStart = (Button) findViewById(R.id.buttonServiceTickerStart);
        buttonServiceTickerStop = (Button) findViewById(R.id.buttonServiceTickerStop);
        buttonServiceForegroundStart = (Button) findViewById(R.id.buttonServiceForegroundStart);
        buttonServiceForegroundStop = (Button) findViewById(R.id.buttonServiceForegroundStop);
        textViewServiceCount = (TextView) findViewById(R.id.textViewServiceCount);

        intent = new Intent(this, TestService.class);
        intentTickerService = new Intent(this, TickerService.class);
        intentTickerService2 = new Intent(this, TickerService2.class);
        intent = new Intent(this, NotificationService.class);
        intentBroadcast = new Intent("ru.pfl.jvmtest.TickerService.Rx");



        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                tickerService2 = ((TickerService2.MyBinder) service).getService();
                Log.d(LOG_TAG, "onServiceConnected");
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
                Log.d(LOG_TAG, "onServiceDisconnected");
            }
        };



        buttonServiceStart.setOnClickListener(v -> {
            startService(intentTickerService2);
        });
        buttonServiceStop.setOnClickListener(v -> {
            stopService(intentTickerService2);
        });

        buttonServiceBind.setOnClickListener(v -> {
            bindService(intentTickerService2, serviceConnection, 0);
        });

        buttonServiceUnbind.setOnClickListener(v -> {
            unbindService(serviceConnection);
        });

        buttonServiceTickerStart.setOnClickListener(v -> {
            tickerService2.tickEn = true;
            tickerService2.startTicker();
        });

        buttonServiceTickerStop.setOnClickListener(v -> {
            tickerService2.tickEn = false;
        });
        buttonServiceForegroundStart.setOnClickListener(v -> {

            tickerService2.startServiceForeground();
        });
        buttonServiceForegroundStop.setOnClickListener(v -> {
            tickerService2.stopServiceForeground();
        });

    }



    void showNotification() {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        Intent ii =
                new Intent(getApplicationContext(), this.getClass());
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, ii, 0);

        //NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        //bigText.bigText("Text");
        //bigText.setBigContentTitle("Today's Bible Verse");
        //bigText.setSummaryText("Text in detail");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Your Title");
        mBuilder.setContentText("Your text");
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //mBuilder.setStyle(bigText);

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


        // === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }



        mNotificationManager.notify(0, mBuilder.build());
    }

    public void registerBroadcastReceiver() {
        this.registerReceiver(tickBroadcastReceiver, new IntentFilter("ru.pfl.jvmtest.TickerService.Tx"));
        Log.d(LOG_TAG, "Broadcast registered");

    }

    public void unregisterBroadcastReceiver() {
        this.unregisterReceiver(tickBroadcastReceiver);
        Log.d(LOG_TAG, "Broadcast unregistered");
    }

    void startTicker() {

        NotificationManager mNotificationManager;

        Intent notificationIntent =
                new Intent(getApplicationContext(), this.getClass());
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);




        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "Your_channel_id";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        //mBuilder.setChannelId(channelId);
        Notification notification =
                new Notification.Builder(getApplicationContext(), "notify_002")
                        .setContentTitle("Ticker")
                        .setContentText("Count")
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setTicker("Tick")
                        .setChannelId(channelId)
                        .build();


        //notification.notify();
        // Notification ID cannot be 0.
        tickerService.startForeground(1, notification);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver();
        //bindService(intent, serviceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterBroadcastReceiver();
        /*if(bound){
            unbindService(serviceConnection);
            bound = false;
        }*/
    }



    public class TickBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            textViewServiceCount.setText(intent.getStringExtra("TickVal"));
            tickEn = true;
        }
    }
}