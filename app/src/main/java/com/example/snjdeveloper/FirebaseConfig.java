package com.example.snjdeveloper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class FirebaseConfig {
    public FirebaseRemoteConfig mFirebaseRemoteConfig;

    public FirebaseConfig(Activity activity) {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(getCacheExpiration(activity)).build();
//                .setMinimumFetchIntervalInSeconds(1).build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

    }

    public Task<Boolean> fetch() {
        return mFirebaseRemoteConfig.fetchAndActivate();
    }

    public long getCacheExpiration(Activity activity) {
        return activity.getSharedPreferences("CRED", Context.MODE_PRIVATE).getLong(activity.getString(R.string.fetch_interval), 21600);
    }

    public void storeCacheExpiration(Activity activity, long time) {
        SharedPreferences preferences = activity.getSharedPreferences("CRED", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(activity.getString(R.string.fetch_interval), time);
        editor.apply();
    }
}
