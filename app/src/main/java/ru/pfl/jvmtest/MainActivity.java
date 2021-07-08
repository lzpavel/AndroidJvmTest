package ru.pfl.jvmtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView[] textViews;
    OnClickListener clickListener;
    Intent[] intents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViews = new TextView[]{findViewById(R.id.textViewCameraActivity), findViewById(R.id.textViewCameraV2Activity), findViewById(R.id.textViewFileRwActivity),
                findViewById(R.id.textViewHardCamActivity), findViewById(R.id.textViewStockCamActivity), findViewById(R.id.textViewServiceControl),
                findViewById(R.id.textViewNotificationActivity)};

        intents = new Intent[]{new Intent(this, CameraActivity.class), new Intent(this, CameraV2Activity.class), new Intent(this, FileRwActivity.class),
                new Intent(this, HardwareCameraActivity.class), new Intent(this, StockCameraActivity.class), new Intent(this, ServiceControlActivity.class),
                new Intent(this, NotificationActivity.class)};

        clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id){
                    case R.id.textViewCameraActivity:
                        startActivity(intents[0]);
                        break;
                    case R.id.textViewCameraV2Activity:
                        startActivity(intents[1]);
                        break;
                    case R.id.textViewFileRwActivity:
                        startActivity(intents[2]);
                        break;
                    case R.id.textViewHardCamActivity:
                        startActivity(intents[3]);
                        break;
                    case R.id.textViewStockCamActivity:
                        startActivity(intents[4]);
                        break;
                    case R.id.textViewServiceControl:
                        startActivity(intents[5]);
                        break;
                    case R.id.textViewNotificationActivity:
                        startActivity(intents[6]);
                        break;
                }
            }
        };
        for (TextView tv : textViews){
            tv.setOnClickListener(clickListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
        System.exit(0);
    }
}