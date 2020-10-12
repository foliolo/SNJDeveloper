package com.example.snjdeveloper.notification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.snjdeveloper.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class NotificationControllerActivity extends AppCompatActivity {
    private String from;
    private ArrayList<String> registrationIDs;
    private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private String contentType = "application/json";
    private String serverKey3 = "AAAAdry8nKQ:APA91bGKXfM2FelPkCfqh5T6bcRTnd8xs8efIPn9fzC4E6N6RBR0Tw2FG-SXxSX36jYN9afgRKtO8z99-CnrHwHbJMzTt3Lkci8BHztajfTiUNAiFgvmPZY18mKl1NQO21wfSr6_sNby";
    private String TAG = "Notification";
    private ArrayList<String> displayList;
    private RecyclerView recyclerView;
    private ViewPager2 viewPager2;
    private View layoutSelectorView;
    private HashMap<String, Object> selectedHtml;
    private View displayWebview;
    private View displayImageView;
    private Dialog dialog;
    private boolean goback = true;
    private ScrollView scrollView;
//    private Snackbar snackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_send_notification);
        from = getIntent().getStringExtra("from");
        displayList = new ArrayList<>();
        TextView textView = findViewById(R.id.title);
        textView.setText(from);
        Button button = findViewById(R.id.button);
        registrationIDs = new ArrayList<>();
        recyclerView = findViewById(R.id.listview);

        button.setOnClickListener(view -> selectContact());
        if (from.contains("popup")) {
            selectedHtml = new HashMap<>();
            layoutSelectorView = findViewById(R.id.selector_layout);
            MaterialButton button1 = findViewById(R.id.addLayoutBt);
            button1.setVisibility(View.VISIBLE);
            displayWebview = findViewById(R.id.display_html);
            displayImageView = findViewById(R.id.imageView_display);
            findViewById(R.id.del_webview).setOnClickListener(view -> {
                selectedHtml.clear();
                displayWebview.setVisibility(View.GONE);
            });
            findViewById(R.id.close_selector).setOnClickListener(view -> layoutSelectorView.setVisibility(View.GONE));
            button1.setOnClickListener(view -> {
                loadLayoutSelector();
            });
        }
    }

    public static boolean isNetworkAvailable(Context con) {
        try {
            ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (layoutSelectorView == null)
            finish();
        else if (layoutSelectorView.getVisibility() == View.VISIBLE)
            layoutSelectorView.setVisibility(View.GONE);
        else if (!goback) {

        } else finish();
    }

    private void loadLayoutSelector() {
        if (viewPager2==null) {
            viewPager2 = findViewById(R.id.viewPager);
            new ViewPagerInitializer(viewPager2, this, findViewById(R.id.selectorBt),
                    findViewById(R.id.progressbar_selector), selectedHtml
                    , findViewById(R.id.webview_display), displayWebview, displayImageView);
        }
            layoutSelectorView.setVisibility(View.VISIBLE);
    }


    private void selectContact() {
        String selector;
        if (from.contains("mail"))
            selector="USER_EMAIL";
        else if (from.contains("notif"))
            selector="FB_TOKEN";
        else selector="UID";

        BottomSheetDialogFragment fragment=
                new UserDataSelector(selector,registrationIDs,displayList,recyclerView);
        fragment.showNow(getSupportFragmentManager(),"");
    }

    private void initialize() {
        EditText titleEdit=findViewById(R.id.title_notify);
        EditText contentEdit=findViewById(R.id.desc);
        String content= Objects.requireNonNull(contentEdit.getText()).toString().trim();
        String title= Objects.requireNonNull(titleEdit.getText()).toString().trim();
        if (title.isEmpty())
            return;
        if (content.isEmpty())
            return;
        if (from.contains("mail")) sendMail(title, content);
        else if (from.contains("notif")) sendNotification(title, content);
        else sendPopup(title, content);
    }

    private void sendPopup(String title, String content) {
        Log.e("SendPopup", "->" + selectedHtml);
        try {
            if (isNetworkAvailable(this)) {
                if (!selectedHtml.isEmpty()) {
                    int style = ((int) selectedHtml.get("STYLE"));
                    if (style <= NotificationAction.NOTIFICATION_DIALOG) {
                        selectedHtml.put("TITLE", title);
                        selectedHtml.put("DESC", content);
                        selectedHtml.remove("HTML");
                    } else {
                        String html = (String) selectedHtml.get("HTML");
                        html = html.replace("insert_your_title", title);
                        html = html.replace("insert_your_content", content);
                        selectedHtml.put("HTML", html);
                        selectedHtml.put("ACTION", "ACTION_DELETE_NOTIFICATION");
//                selectedHtml.put("IS_DIALOG", true);
                    }
                    msgSend("Checking users please wait...", true, false, R.color.colorAccent);
                    FirebaseDatabase.getInstance().getReference().child("Notifications")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>)
                                            dataSnapshot.getValue();
                                    if (map != null) {
                                        msgSend("Stopping upload", false, false, R.color.colorAccent);
                                        if (displayList.toString().contains("All Customers")) {
                                            popupExitForAllCustomers();
                                        } else
                                            checkUsers(map);
                                    } else sendPopup();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                } else {
                    msgSend("Please select layout", false, false, (R.color.red));
                }
            } else
                msgSend("No network connectivity", false, false, (R.color.red));

        } catch (Exception e) {
            e.printStackTrace();
            msgSend("Error occurred", false, false, (R.color.red));

        }
    }

    private void popupExitForAllCustomers() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Users exist");
        alert.setMessage("Users already exist in previous created popup messages");
        alert.setPositiveButton("Delete Old popup messages", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                msgSend("Deleting old popup messages...", true, false, R.color.colorAccent);
                FirebaseDatabase.getInstance().getReference().child("Notifications").
                        setValue(null)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                                sendPopup();
                            else
                                msgSend("Error occurred", false, false, R.color.red);
                        });
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
        AlertDialog alertDialog = alert.create();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
    }

    void sendPopup() {
        if (registrationIDs.isEmpty())
            msgSend("User list is empty", false, false, (R.color.colorAccent));
        else {
            selectedHtml.put("USERS", registrationIDs.toString().replace("[", "")
                    .replace("]", ""));
            msgSend("Sending please wait...", true, false,
                    (R.color.colorAccent));
            FirebaseDatabase.getInstance().getReference().child("Notifications")
                    .child(String.valueOf(System.currentTimeMillis())).setValue(selectedHtml).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                        msgSend("Send successfully", false, true, (R.color.colorAccent));
                    else
                        msgSend("Error occurred", false, false, (R.color.red));

                }
            });
        }

    }

    void checkUsers(HashMap<String, HashMap<String, Object>> map) {
        HashMap<String, String> allUsers = new HashMap<>();
        HashMap<String, String> userData = new HashMap<>();
        try {
            for (String key : map.keySet()) {
                String tempUsers = (String) map.get(key).get("USERS");
                int i = 0;
                for (String userId : registrationIDs) {
                    if (tempUsers.contains(userId)) {
                        String oldValue = allUsers.get(key);
                        allUsers.put(key, oldValue == null ? userId : "," + userId);
//                    set.add(userId);
                        userData.put(userId, displayList.get(i).split(",")[1]);
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            msgSend("Error occurred", false, false, R.color.red);
        }
        if (userData.isEmpty())
            sendPopup();
        else {
            usersAlreadyExistInPopup(userData, allUsers, map);
        }
    }

    private void usersAlreadyExistInPopup(HashMap<String, String> userData, HashMap<String, String> allUsers, HashMap<String, HashMap<String, Object>> map) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Users already exist in the popup message created before\nUsers:\n " + userData.values().toString().replace("[", "")
                .replace("]", "").replace(",", "\n"));
        alert.setTitle("Users already exists in other message");
        msgSend("Stopping upload....", false, false, (R.color.colorAccent));
        alert.setPositiveButton("Remove from current list", (dialogInterface, i) -> {
//            registrationIDs.removeAll(userData.keySet());
            RecyclerView recyclerView = findViewById(R.id.listview);
            int size = registrationIDs.size();
            for (int j = 0; j < size; j++) {
                if (userData.containsKey(registrationIDs.get(j))) {
                    registrationIDs.remove(j);
                    displayList.remove(j);
                    if (recyclerView.getAdapter() != null)
                        recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
            sendPopup();
            dialogInterface.dismiss();
        });
        alert.setNegativeButton("Remove from old layout", (dialog, which) -> {
            msgSend("Removing users please wait...", true, false, (R.color.colorAccent));
            for (String key : allUsers.keySet()) {
                String tempUsers = (String) map.get(key).get("USERS");
                for (String userId : Objects.requireNonNull(allUsers.get(key)).split(",")) {
                    if (tempUsers.contains("," + userId))
                        tempUsers = tempUsers.replace("," + userId, "");
                    else if (tempUsers.contains(", " + userId))
                        tempUsers = tempUsers.replace(", " + userId, "");
                    else if (tempUsers.contains(userId + ", "))
                        tempUsers = tempUsers.replace(userId + ", ", "");
                    else
                        tempUsers = tempUsers.replace(userId, "");
                }
                //if    tempusers   is  empty   del layout
                if (tempUsers.isEmpty())
                    map.put(key, null);
                else {
                    HashMap<String, Object> tempMap = map.get(key);
                    tempMap.put("USERS", tempUsers.trim());
                    map.put(key, tempMap);
                }
            }
            msgSend("Sending please wait...", true, false,
                    (R.color.colorAccent));
            FirebaseDatabase.getInstance().getReference().child("Notifications")
                    .setValue(map).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    sendPopup();
                else
                    msgSend("Error occurred", false, false, (R.color.red));

            });
            dialog.dismiss();

        });
        alert.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAccent));
//        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
//        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
//        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }

    @Deprecated
    private void oldPopupNotification() {
        FirebaseFirestore.getInstance().collection("Extra")
                .document("Notification").set(selectedHtml)
                .addOnSuccessListener(documentSnapshot -> {
                    FirebaseDatabase.getInstance().getReference("Developers").child("UserNotification").child("USERS")
                            .setValue(registrationIDs.toString().replace("[", "")
                                    .replace("]", "")).addOnSuccessListener(
                            aVoid ->
                                    msgSend("Send successfully", false, true, (R.color.colorAccent)));
                });
    }

    private void sendNotification(String title, String content) {
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject notificationData = new JSONObject();
//        JSONObject ttlData = new JSONObject();

        try {
//            ttlData.put("ttl","5s");
            notification.put("time_to_live", 21600);
            notificationBody.put("title", title);
            notificationBody.put("body", content);   //Enter your notification message
//            registrationIDs.put(token);
            if (registrationIDs.contains("Select All")) {
                notification.put("to", "/topics/customers");
            } else {
                JSONArray jsonIds = new JSONArray(registrationIDs);
                notification.put("registration_ids", jsonIds);
            }
//                                notification.put("to", topic);
            notification.put("notification", notificationBody);
            notification.put("data", notificationData);
            Log.e("Request", notification.toString());
        } catch (JSONException e) {
            Log.e("TAG", "onCreate: " + e.getMessage());
        }
        Log.e("TAG", "sendNotification");
        ProgressDialog progressDialog = new ProgressDialog(NotificationControllerActivity.this);
        progressDialog.setMessage("Sending, please wait...");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                response -> {
                    progressDialog.hide();
                    Log.e(TAG, "Notification response " + response.toString());
                    try {
                        if (response.has("message_id") || (response.has("success") && response.getInt("success") >= 1))
                            msgSend("Message send successfully", false, true, (R.color.colorAccent));
                        else
                            msgSend("Message failed Error:" + response.getJSONArray("results"), false, false, (R.color.red));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        msgSend("Error occurred sending failed", false, false, (R.color.red));
                    }

                },
                error -> {
                    progressDialog.hide();
                    error.printStackTrace();
                    msgSend("Notification sending failed Error:" + error.getMessage(), false, false, (R.color.red));

                })
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String,String> params = new HashMap<>();
                params.put("Authorization","key=" +serverKey3);
                params.put("Content-Type",contentType);
                return params;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void sendMail(String title, String content) {
       try {
           SendMail mail = new SendMail(registrationIDs.toString().replace("[", "")
                   .replace("]", "").trim(), title, content);
           mail.execute();
       }catch (Exception e){e.printStackTrace();}
    }

    public void sendData(View view) {
        if (!registrationIDs.isEmpty())
            initialize();
        else msgSend("Please select users", false, false, (R.color.red));
    }

    public void backOnClicked(View view) {
        finish();
    }

    public void showHelpClicked(View view) {
        if (from.contains("notif"))
            showInfoDialog(R.string.notification_help_message, R.drawable.status_bar_notification);
        else if (from.contains("mail"))
            showInfoDialog(R.string.mail_help_message,R.drawable.ic_email_message);
        else
            showInfoDialog(R.string.popup_help_message,R.drawable.inapp_notification);

    }

        void showInfoDialog(int stringid,int iconId){
            setTheme(R.style.AppThemeBlack);
            if (dialog==null) {
                final View dialogView = View.inflate(this, R.layout.info_dialog, null);
                dialog = new Dialog(this, R.style.Dialog1);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setContentView(dialogView);
                dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                });
                TextView textView = dialog.findViewById(R.id.help_msg);
                textView.setText(stringid);
                scrollView = dialog.findViewById(R.id.scrollView);
                Glide.with(dialogView).load(iconId).into((ImageView) dialogView.findViewById(R.id.icon));
                dialogView.findViewById(R.id.okBt).setOnClickListener(view -> dialog.dismiss());
                dialog.setOnDismissListener(dialog -> setTheme(R.style.AppTheme));
                dialog.show();
            } else {
                TextView textView = dialog.findViewById(R.id.help_msg);
                textView.setText(stringid);
                if (scrollView != null)
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                dialog.show();
                Glide.with(dialog.getContext()).load(iconId).into((ImageView) dialog.findViewById(R.id.icon));
                dialog.show();
            }
        }

    @SuppressLint("StaticFieldLeak")
    public class SendMail extends AsyncTask<String, Integer, Integer> {

        ProgressDialog progressDialog;
        private String all_email;
        private String subject;
        private String content;

        SendMail(String mail_ids,String subject,String content) {
            all_email=mail_ids;
            this.subject=subject;
            this.content=content;
            Log.e("mail_ids","->"+mail_ids);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(NotificationControllerActivity.this);
            progressDialog.setMessage("Sending, please wait...");
            progressDialog.show();

        }

        @Override
        protected Integer doInBackground(String... strings) {

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");
//            props.put("mail.debug", "true");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                          try {
                              return new //___@9425
                                      PasswordAuthentication(
                                      "shreenathjichilledwater.com@gmail.com", "snj@9425");
                          }catch (Exception e){
                              return null;
                          }
                        }
                    });

            try {
                if (session==null) {
                    msgSend(getString(R.string.mail_authentication_failed)
                            , true, false, (R.color.red));
                    return 0;
                }
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("ShreeNath Ji Chilled Water <shreenathjichilledwater.com@gmail.com>"));
                message.setRecipients(Message.RecipientType.BCC,
                        InternetAddress.parse(all_email));
                message.setSubject(subject);
                message.setText(content);
                message.setReplyTo(new Address[]{new InternetAddress("shreenathjichilledwater.com@gmail.com")});
                message.setHeader("Content-type","text/html");
                Transport.send(message);


            } catch (Exception e) {
                progressDialog.dismiss();
                showErrorDialog(e.getMessage());
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressDialog.dismiss();
            if (integer==1)
                msgSend("Mail successfully send to " + all_email, false, true, (R.color.colorAccent));
        }
    }

    private void showErrorDialog(String message) {
        this.runOnUiThread(() -> new AlertDialog.Builder(NotificationControllerActivity.this)
                .setTitle("Email failed")
                .setMessage(getString(R.string.mail_authentication_failed)+"\n\nError: "+message)
                .setPositiveButton("OK",null)
                .show());

    }

    private void msgSend(String msg, boolean isIndefinite, boolean isFinish, int color) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), msg, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(color));
        if (isIndefinite) {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            findViewById(R.id.parent).animate().alpha(0.4f).setDuration(400);
            goback = false;
        } else {
            if (findViewById(R.id.parent).getAlpha() != 1)
                findViewById(R.id.parent).animate().alpha(1).setDuration(400);
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            goback = true;
        }

        if (isFinish)
            new Handler().postDelayed(() -> {
                snackbar.dismiss();
                finish();
            }, 2000);
        snackbar.show();
    }
}
