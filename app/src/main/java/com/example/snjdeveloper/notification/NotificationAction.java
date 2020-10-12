package com.example.snjdeveloper.notification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.snjdeveloper.InfoActivity;
import com.example.snjdeveloper.LoginActivity;
import com.example.snjdeveloper.StatsActivity;
import com.example.snjdeveloper.StatsBottleActivity;
import com.example.snjdeveloper.admin.CustomerInfoActivity;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.example.snjdeveloper.settings.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class NotificationAction {
    public static int   INFO_BOX=0;
    public static int BOTTOM_BAR = 1;
    public static int NOTIFICATION_DIALOG = 2;
    public static int WEB_DIALOG = 3;
    public static int WEB_SCREEN = 4;

    public Class<?> getClassName(String intent, Context context) {
        if (intent.equalsIgnoreCase("ACTION_OPEN_INFO"))
            return InfoActivity.class;
//        else if (intent.equalsIgnoreCase("ACTION_OPEN_UPDATE"))
//            return UpdateActivity.class;
        else if (intent.equalsIgnoreCase("ACTION_OPEN_SETTING"))
            return SettingsActivity.class;
        else if (intent.equalsIgnoreCase("ACTION_OPEN_TRANSACTIONS") ||
                intent.equalsIgnoreCase("ACTION_OPEN_ORDERS"))
            return StatsActivity.class;
        else if (intent.equalsIgnoreCase("ACTION_OPEN_BOTTLES"))
            return StatsBottleActivity.class;
        else if (intent.equalsIgnoreCase("ACTION_OPEN_USER_INFO"))
            return CustomerInfoActivity.class;
//        else if (intent.equalsIgnoreCase("ACTION_CHANGE_MOBILE"))
//            return LoginActivity.class;
        else if (intent.equalsIgnoreCase("ACTION_LOGIN")) {
            clearPreferences("CRED", context);
            clearPreferences("RECENT", context);
            clearPreferences("RECENT_SEARCH", context);
            FirebaseMessaging.getInstance().unsubscribeFromTopic("admin");
            FirebaseAuth.getInstance().signOut();
            return LoginActivity.class;
        }
        return HomeAdmin.class;
    }

    void clearPreferences(String name, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
    }

    Intent addDataInIntent(Intent intent, Map<String, String> data) {
        if (!data.isEmpty()) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        return intent;
    }

    public Intent addDataInIntent(Intent intent, Bundle bundle) {
        if (bundle != null && !bundle.isEmpty()) {
            for (Object key : bundle.keySet()) {
                try {
                    intent.putExtra((String) key, bundle.getString((String) key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return intent;
    }
    public Intent addDataInIntent( Map<String,Object> bundle,Intent intent) {
        if (bundle != null && !bundle.isEmpty()) {
            for (Object key : bundle.keySet()) {
                try {
                    intent.putExtra((String) key, (String) bundle.get(key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return intent;
    }

}
