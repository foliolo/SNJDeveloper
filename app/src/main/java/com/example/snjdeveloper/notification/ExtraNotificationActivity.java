package com.example.snjdeveloper.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snjdeveloper.CustomScreen;
import com.example.snjdeveloper.R;

public class ExtraNotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_notification);
        TextView textView = findViewById(R.id.title);
        textView.setText("Extra Notification");
        try {
            new CustomScreen(R.string.custom_notification_screen, this, findViewById(R.id.recyclerview));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void backOnClicked(View view) {
        finish();
    }

    public void sendBillClicked(View view) {
        startActivity(new Intent(this, SendBillActivity.class));
    }
}
