package ru.pfl.jvmtest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class TestService extends Service {

    MyBinder binder = new MyBinder();

    Timer timer;
    TimerTask timerTask;

    int cnt = 0;

    public TestService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Service created", Toast.LENGTH_SHORT).show();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Service start command", Toast.LENGTH_SHORT).show();
        startTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service destroy", Toast.LENGTH_SHORT).show();
        stopTimer();
        super.onDestroy();

    }

    void startTimer() {
        if (timer == null) {
            timer = new Timer();

            if (timerTask == null) {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        cnt++;
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
    }

    class MyBinder extends Binder {
        TestService getService() {
            return TestService.this;
        }
    }
}