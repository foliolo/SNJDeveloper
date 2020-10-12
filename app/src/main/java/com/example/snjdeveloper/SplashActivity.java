package com.example.snjdeveloper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snjdeveloper.admin.HomeAdmin;
import com.example.snjdeveloper.notification.NotificationAction;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private Intent activityIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getUid() == null)
            activityIntent = new Intent(this, LoginActivity.class);
        else {
            try {
                Intent receivedIntent = getIntent();
                String classname = receivedIntent.getStringExtra("intent");
                if (classname == null)
                    activityIntent = new Intent(this, HomeAdmin.class);
                else {
                    NotificationAction action = new NotificationAction();
                    activityIntent = new Intent(this, action.getClassName(classname, this));
                    receivedIntent.removeExtra("intent");
                    activityIntent = action.addDataInIntent(activityIntent, receivedIntent.getExtras());
                }
            } catch (Exception e) {
                activityIntent = new Intent(this, HomeAdmin.class);
            }
//            activity= HomeAdmin.class;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isFinishing())
            new Handler().postDelayed(() -> {
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(activityIntent);
            }, 2000);
    }
}
