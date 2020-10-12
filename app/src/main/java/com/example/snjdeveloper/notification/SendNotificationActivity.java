package com.example.snjdeveloper.notification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.snjdeveloper.FirebaseConfig;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ReceivedNodeAdapter;
import com.example.snjdeveloper.RecyclerUI.UserListAdapter;
import com.example.snjdeveloper.extra.Parameters;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

public class SendNotificationActivity extends AppCompatActivity {
    private String from;
    public ArrayList<String> registrationIDs;
    private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private String contentType = "application/json";
    private String serverKey3 = "AAAAdry8nKQ:APA91bGKXfM2FelPkCfqh5T6bcRTnd8xs8efIPn9fzC4E6N6RBR0Tw2FG-SXxSX36jYN9afgRKtO8z99-CnrHwHbJMzTt3Lkci8BHztajfTiUNAiFgvmPZY18mKl1NQO21wfSr6_sNby";
    private String TAG = "Notification";
    private ArrayList<String> displayList;
    private RecyclerView recyclerView;
    private Dialog dialog;
    private boolean goback = true;
    private ScrollView scrollView;
    private String selector;
    private AutoCompleteTextView actionView;
    private Parameters parameters=new Parameters(true);
    public ReceivedNodeAdapter adapter;
    private boolean comingFromCustomer;
    private View infoFrag;
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
        String extra = getIntent().getStringExtra("DIS_DATA");
//        if (extra != null)
//            initializeRecyclerview(extra, getIntent().getStringExtra("DATA"));
        if (extra != null)
        {    comingFromCustomer =true;
            button.setVisibility(View.GONE);
            initializeRecyclerview(extra, getIntent().getStringExtra("DATA"));
        }else button.setOnClickListener(view -> selectContact());
        setSelector();
//        button.setOnClickListener(view -> selectContact());
    }

    private void setSelector() {
        if (from.contains("mail"))
        {   findViewById(R.id.action).setVisibility(View.GONE);
            selector = "USER_EMAIL";
        }
        else
        {
            selector = "FB_TOKEN";
            initializeActionParameters();
        }

    }

    private void initializeActionParameters() {
        String[] actionParas=parameters.getAllActions();
        if (!comingFromCustomer)
        {  actionParas= removeElementFromArray(actionParas);
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                R.layout.textview_layout, actionParas);
        actionView=findViewById(R.id.actionAuto);
        actionView.setAdapter(adapter1);
        RecyclerView recyclerView=findViewById(R.id.recyclerview_parameters);
        actionView.setOnItemClickListener((parent, view, position, id) -> {
            ArrayList<HashMap<String, Object>> list=new ArrayList<>();
            setData(recyclerView, list);
            if (comingFromCustomer) {
                if (position == 0) {
                    initializeInfoParameters(list);
                }else {
                    if (infoFrag!=null)
                        infoFrag.setVisibility(View.GONE);
                    list.addAll( parameters.getActionParametersList(position));
                }
            }else
            {
                list .addAll(parameters.getActionParametersList(position+1));
            }
            parameters.removeFields(list,new String[]{"intent"});
            adapter.notifyDataSetChanged();
//            ArrayList<HashMap<String, Object>> list = parameters.getActionParametersList(position);
//            parameters.removeFields (list,new String[]{"ACTION"});
//            setData(recyclerView,list);
        });
    }
    private void initializeInfoParameters(ArrayList<HashMap<String, Object>> list) {
        Fragment fragmentClass =new InfoParaFragment(adapter,registrationIDs.get(0));
        FragmentTransaction fragmentTransaction2;
        fragmentTransaction2= getSupportFragmentManager().beginTransaction();
        if (infoFrag==null)
            infoFrag= findViewById(R.id.info_frag);
        infoFrag.setVisibility(View.VISIBLE);
        try{ fragmentTransaction2.replace(R.id.info_frag, Objects.requireNonNull(fragmentClass));
            fragmentTransaction2.commit();
        }catch (Exception ignored){}

    }
    private String[] removeElementFromArray(String[] actionParas) {
        List<String> list = new ArrayList<>(Arrays.asList(actionParas));
        list.remove(0);
        return list.toArray(new String[0]);

    }

    private void setData(RecyclerView recyclerView, ArrayList<HashMap<String, Object>> data) {
        Log.e("Data","Setting data\n"+data);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        adapter=new ReceivedNodeAdapter(data,this,getSupportFragmentManager());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private void initializeRecyclerview(String data, String id) {
        displayList.add(data);
        registrationIDs.add(id);
        UserListAdapter listAdapter = new UserListAdapter(this, registrationIDs, displayList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listAdapter);
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
        if (goback)
            finish();
    }


    private void selectContact() {
        BottomSheetDialogFragment fragment =
                new UserDataSelector(selector, registrationIDs, displayList, recyclerView);
        fragment.showNow(getSupportFragmentManager(), "");
    }

    private void initialize() {
        EditText titleEdit = findViewById(R.id.title_notify);
        EditText contentEdit = findViewById(R.id.desc);
        String content = Objects.requireNonNull(contentEdit.getText()).toString().trim();
        String title = Objects.requireNonNull(titleEdit.getText()).toString().trim();
        if (title.isEmpty())
            return;
        if (content.isEmpty())
            return;
        if (from.contains("mail")) {
//            sendMail(title, content);
            fetchHtmlEndSeparator(title, content);
        } else if (from.contains("notif")) sendNotification(title, content);

    }

    private void sendNotification(String title, String content) {
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject notificationData = new JSONObject();

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
            notification.put("priority", "high");
//                                notification.put("to", topic);
            notification.put("notification", notificationBody);
            notificationData.put("intent", "ACTION_OPEN_HOME");
            notificationData.put("TITLE", title);
            notificationData.put("DESC", content);
            if(!isAddingDataToJsonSuccessful(notificationData))
            {   Log.e("NotificationData","Failed->"+notificationData);
                return;
            }
            Log.e("NotificationData","Successful->"+notificationData);
            notification.put("data", notificationData);
            Log.e("Request", notification.toString());
        } catch (JSONException ignored) {
        }
        ProgressDialog progressDialog = new ProgressDialog(SendNotificationActivity.this);
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

                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + serverKey3);
                params.put("Content-Type", contentType);
                return params;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private boolean isAddingDataToJsonSuccessful(JSONObject notificationData) throws JSONException {
        HashMap<String,Object>finalData=setDataToUpload();
        if (finalData==null)
            return false;
        for (Map.Entry<String,Object>data:finalData.entrySet())
            notificationData.put(data.getKey(),data.getValue());
        Log.e("Extra Data","Processed \n "+finalData);
        return true;
    }
    private HashMap<String,Object> setDataToUpload() {
        ArrayList<HashMap<String, Object>> list =adapter.data;
        HashMap<String,Object>finalData = null;
        if (list!=null){
        Log.e("Data to save", "->" + list);
        int i = 0;
        finalData=new HashMap<>();
        for (HashMap<String, Object> record : list) {
            {String key;
                try {
                    TextInputLayout layout=(TextInputLayout) Objects.requireNonNull(record.get("key_edittext"));
                    key= (String) record.get("key");
                    if (record.get("value_edittext")!=null) {
                        String value = Objects.requireNonNull(((TextInputEditText) Objects.requireNonNull(record.get("value_edittext"))).getText()).toString();
                        Log.e("Data", i + " key->" + key + "\nvalue->" + value);
                        if (layout.getHelperText() != null && layout.getHelperText().equals("required") && (value.isEmpty())) {//case for required field
                            layout.setError(key.toLowerCase() + " required");
                            return null;
                        }
                        else if (!value.isEmpty())
                            configureDataToSave(key, value, finalData, true);
                    }else {
                        if (key!=null)
                        {   Object value= record.get("value");
                               configureDataToSave(key,value,finalData,false);
                        }
                    }

                } catch (Exception e){e.printStackTrace();}
                i++;
            }
        }
        Log.e("","-----------------------------------------------------------------------------------------------------------------------------------");
        Log.e("Final Data","->"+finalData);
        Log.e("","-----------------------------------------------------------------------------------------------------------------------------------");
        }
        return finalData;
    }
    private void configureDataToSave(String key, Object value, HashMap<String, Object> finalData, boolean requireCast) {
        if (requireCast) {
            if (value instanceof String){
                if (value.toString().toLowerCase().equals("true")||value.toString().toLowerCase().equals("false"))
                    value=Boolean.parseBoolean((String) value);
                else if (TextUtils.isDigitsOnly(key) && TextUtils.isDigitsOnly((CharSequence) value))//means if it a info data
                    value=String.valueOf(value);
                else if (TextUtils.isDigitsOnly((String) value))
                    value=Long.parseLong((String) value);
            }
        }

        finalData.put(key,value);
    }
    private void sendMail(String title, String content, HashMap<String, String> template) {
        try {
            content = formatStringContentToHtml(content, title, template);
            Log.e("FinalContent", "->" + content);
            msgSend("Sending started", false, false, R.color.colorAccent);
            SendMail mail = new SendMail(registrationIDs.toString().replace("[", "")
                    .replace("]", "").trim(), title, content);
            mail.execute();
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void fetchHtmlEndSeparator(String title, String content) {
        msgSend("Fetching details Please wait....", true, false, R.color.colorAccent);
        FirebaseConfig config = new FirebaseConfig(this);
        config.fetch()
                .addOnCompleteListener(this, task -> {
                    // If is successful, activated fetched
                    String template = config.mFirebaseRemoteConfig.getString(getString(R.string.email_template));
                    String email_initial_s = config.mFirebaseRemoteConfig.getString(getString(R.string.email_template_initial_s));
                    String email_initial_e = config.mFirebaseRemoteConfig.getString(getString(R.string.email_template_initial_e));
                    String email_s = config.mFirebaseRemoteConfig.getString(getString(R.string.email_template_s));
                    String email_e = config.mFirebaseRemoteConfig.getString(getString(R.string.email_template_e));
                    HashMap<String, String> map = new HashMap<>();
                    map.put(getString(R.string.email_template), template);
                    map.put(getString(R.string.email_template_initial_s), email_initial_s);
                    map.put(getString(R.string.email_template_initial_e), email_initial_e);
                    map.put(getString(R.string.email_template_s), email_s);
                    map.put(getString(R.string.email_template_e), email_e);
                    config.storeCacheExpiration(this, config.mFirebaseRemoteConfig.getLong(getString(R.string.fetch_interval)));
                    sendMail(title, content, map);
                });
    }

    private String formatStringContentToHtml(String content, String title, HashMap<String, String> htmlTemplate) {

        StringBuilder body = new StringBuilder();
        String startSeparator = htmlTemplate.get(getString(R.string.email_template_s));
        if (startSeparator.isEmpty())
            startSeparator = "<br>";
        String endSeparator = htmlTemplate.get(getString(R.string.email_template_e));
        int i = 0;
        for (String s : content.split("\n")) {
            if (i == 0)
                body.append(htmlTemplate.get(getString(R.string.email_template_initial_s)))
                        .append(s).append(htmlTemplate.get(getString(R.string.email_template_initial_e)));
            else
                body.append(startSeparator).append(s).append(endSeparator);
            i++;
        }

        if (htmlTemplate.get(getString(R.string.email_template)).isEmpty())
            content = body.toString();
        else
            content = htmlTemplate.get(getString(R.string.email_template)).replace("your_description_here", body);
        return content.replace("your_title_here", title);
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
            showInfoDialog(R.string.mail_help_message, R.drawable.ic_email_message);
        else
            showInfoDialog(R.string.popup_help_message, R.drawable.inapp_notification);

    }

    void showInfoDialog(int stringid, int iconId) {
        setTheme(R.style.AppThemeBlack);
        if (dialog == null) {
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

        SendMail(String mail_ids, String subject, String content) {
            all_email = mail_ids;
            this.subject = subject;
            this.content = content;
            Log.e("mail_ids", "->" + mail_ids);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SendNotificationActivity.this);
            progressDialog.setMessage("Sending, please wait...");
            progressDialog.show();

        }

        @Override
        protected Integer doInBackground(String... strings) {

            Properties props = new Properties();
//            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.host", "smtp-mail.outlook.com");
            props.put("mail.smtp.socketFactory.port", "587");
//            props.put("mail.smtp.socketFactory.class",
//                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.port", "587");
//            props.put("mail.smtp.port", "465");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            try {
                                return new //snj@9425
                                        PasswordAuthentication(
                                        "shreenathjichilledwater@outlook.com", "N@ture@9425");
                            } catch (Exception e) {
                                return null;
                            }
                        }
                    });

            try {
                if (session == null) {
                    FirebaseCrashlytics.getInstance().log(getString(R.string.mail_authentication_failed));
                    msgSend(getString(R.string.mail_authentication_failed)
                            , true, false, (R.color.red));
                    return 0;
                }

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("ShreeNath Ji Chilled Water <shreenathjichilledwater@outlook.com>"));
                message.setRecipients(Message.RecipientType.BCC,
                        InternetAddress.parse(all_email));
                message.setSubject(subject);
                message.setText(content);
                String mail = getSharedPreferences("CRED", MODE_PRIVATE).getString("PUBLIC_EMAIL", "shreenathjichilledwater.com@gmail.com");
                message.setReplyTo
                        (new Address[]{new InternetAddress(mail)});
                message.setHeader("Content-type", "text/html");
                Transport.send(message);


            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
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
            if (integer == 1)
                msgSend("Mail successfully send to " + all_email, false, true, (R.color.colorAccent));
        }
    }

    private void showErrorDialog(String message) {
        this.runOnUiThread(() -> new AlertDialog.Builder(SendNotificationActivity.this)
                .setTitle("Email failed")
                .setMessage(getString(R.string.mail_authentication_failed) + "\n\nError: " + message)
                .setPositiveButton("OK", null)
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
