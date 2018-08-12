package com.example.satya_05.instagram;

/**
 * Created by satya_05 on 17/4/18.
 */

import android.app.NotificationManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {

    String title;
    String text;
    TextView txttitle;
    TextView txttext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.cancel(0);
        Intent i = getIntent();

        title = i.getStringExtra("title");
        text = i.getStringExtra("text");
        txttitle = findViewById(R.id.title);
        txttext = findViewById(R.id.text);

        txttitle.setText(title);
        txttext.setText(text);
    }
}
