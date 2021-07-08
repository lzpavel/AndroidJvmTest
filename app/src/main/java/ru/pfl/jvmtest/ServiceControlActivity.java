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

    int i = 0;

    boolean bound = false;
    Intent intent;
    Intent intentTickerService;
    Intent intentTickerService2;
    ServiceConnection serviceConnection;
    TestService testService;
    TickerService tickerService;

    Thread myThread;
    Boolean threadEn = false;

    int cnt = 0;

    TickBroadcastReceiver tickBroadcastReceiver = new TickBroadcastReceiver();

    public TextView textViewServiceCount = null;

    //public static final String CHANNEL_ID = "101";
    private static final int NOTIFY_ID = 101;
    private static String CHANNEL_ID = "Cat channel";
    final String LOG_TAG = "myLogs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_control);

        Button buttonStartService = (Button) findViewById(R.id.buttonStartService);
        Button buttonStopService = (Button) findViewById(R.id.buttonStopService);
        Button buttonUpdateService = (Button) findViewById(R.id.buttonUpdateService);
        textViewServiceCount = (TextView) findViewById(R.id.textViewServiceCount);
        Button buttonServiceTest = (Button) findViewById(R.id.buttonServiceTest);

        intent = new Intent(this, TestService.class);
        intentTickerService = new Intent(this, TickerService.class);
        intentTickerService2 = new Intent(this, TickerService2.class);
        intent = new Intent(this, NotificationService.class);



        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                testService = ((TestService.MyBinder) service).getService();

                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };

        buttonStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentTickerService2.putExtra("TickEnable", true);
                startService(intentTickerService2);
                //i++;
                //textViewServiceCount.setText(String.valueOf(i));
                //startService(intent);
                //bindService(intent, serviceConnection, 0);
            }
        });
        buttonStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentTickerService2.putExtra("TickEnable", false);
                startService(intentTickerService2);
                //i--;
                //textViewServiceCount.setText(String.valueOf(i));
                //unbindService(serviceConnection);
                //stopService(intent);
            }
        });
        buttonUpdateService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewServiceCount.setText(String.valueOf(testService.cnt));
            }
        });
        buttonServiceTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                threadEn = !threadEn;

                if(threadEn) {
                    startThread();
                }

                //SystemClock.sleep(1000);




                //startService(intentNotification);
                //startTicker();
                //intentTickerService.putExtra("TickEnable", true);
                //startService(intentTickerService);

            }
        });


    }

    void startThread() {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (threadEn) {
                    cnt++;
                    textViewServiceCount.setText(String.valueOf(cnt));
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        myThread.start();
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
        }
    }
}