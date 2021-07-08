package ru.pfl.jvmtest;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;


public class TickerService extends IntentService {

    final String LOG_TAG = "myLogs";

    public static final String TICK_BROADCAST_ID = "ru.pfl.jvmtest.action.tick";
    Intent intentTick = new Intent(TICK_BROADCAST_ID);

    Timer timer;
    TimerTask timerTask;

    Thread myThread = null;
    Boolean threadEn = false;

    int cnt = 0;

    public TickerService() {
        super("TickerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        if (intent != null) {

            threadEn = intent.getBooleanExtra("TickEnable", false);



            if (threadEn) {
                if (myThread == null) {
                    initThread();
                }
                startThread();
            }


            /*Boolean en = intent.getBooleanExtra("TickEnable", false);
            if (en) {
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                startTimer();
                Log.d(LOG_TAG, "startTimer");
            } else {
                stopTimer();
                Log.d(LOG_TAG, "stopTimer");
            }*/

        }
    }



    void initThread() {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (threadEn) {
                    cnt++;
                    intentTick.putExtra("CntVal", String.valueOf(cnt));
                    sendBroadcast(intentTick);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(LOG_TAG, "Thread tick!");
                }
                Log.d(LOG_TAG, "Thread loop exit");
            }
        });
    }

    void startThread() {

        myThread.start();
    }

    void startTimer() {
        if (timer == null) {
            timer = new Timer();

            if (timerTask == null) {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        cnt++;
                        intentTick.putExtra("CntVal", String.valueOf(cnt));
                        sendBroadcast(intentTick);
                        Log.d(LOG_TAG, "Tick!");
                    }
                };
            }
            timer.scheduleAtFixedRate(timerTask, 1000, 1000);
        }

    }

    void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        if(timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        stopSelf();
        Log.d(LOG_TAG, "Timer stopped");
    }

}