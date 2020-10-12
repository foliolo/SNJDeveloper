package com.example.snjdeveloper.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.SplashActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG = "CloudMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        try {
            Log.e("Data From Notification","->"+remoteMessage.getData());
            sendNotification(Objects.requireNonNull(remoteMessage.getNotification()).getTitle(),
                    remoteMessage.getNotification().getBody(),remoteMessage.getData());
        }catch (Exception e){Log.e("ServiceError","Error "+remoteMessage.getNotification().getTitle()+
                " body "+remoteMessage.getNotification().getBody());
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NotNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        String uid=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).getString("UID","not found");
        FirebaseDatabase.getInstance().getReference("Customers").child(uid).child("FB_TOKEN").setValue(token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param body
     * @param data
     */
    private void sendNotification(String title, String body, Map<String, String> data) {

        Intent intent;
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Log.e(TAG, "Inside send notification title " + title + " body " + body);
                if (data == null || data.get("intent") == null)
                    intent = new Intent(this, SplashActivity.class);
                else {
                    NotificationAction action = new NotificationAction();
                    intent = new Intent(this, action.getClassName(data.get("intent"), this));
                    data.remove("intent");
                    intent = action.addDataInIntent(intent, data);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                String channelId = getString(R.string.default_notification_channel_id);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setColor(getResources().getColor(R.color.colorAccent))
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            "Admin",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 }
