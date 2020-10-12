package com.example.snjdeveloper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.example.snjdeveloper.notification.SendNotificationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Constants {
    public static String searchKey = "";
    public static boolean isListLoaded = false;

    public static void setBackgroundColor(CardView cardView, Context context, int... value) {
        try {
            int position;
            if (value.length > 0 && value[0] <= 49)
                position = value[0];
            else {
                Random r = new Random();
                position = r.nextInt(49);
            }
            setColor(cardView, context, getColor(position));
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            setColor(cardView, context, R.color.pink);
        }
    }
   public static String getFormattedTime(Object time){
     try {
         if (time instanceof String)
             time = Long.parseLong((String) time);
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.UK);
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis((Long) time);
         return dateFormat.format(calendar.getTime());
     }catch (Exception e){return String.valueOf(time);}
    }
    static int getColor(int position) {
        switch (position) {
            case 0:
                return R.color.color1;

            case 1:
                return R.color.color2;

            case 2:
                return R.color.color3;

            case 3:
                return R.color.color4;

            case 4:
                return R.color.color5;

            case 5:
                return R.color.color6;

            case 6:
                return R.color.color7;

            case 7:
                return R.color.color8;

            case 8:
                return R.color.color9;

            case 9:
                return R.color.color10;

            case 10:
                return R.color.color11;

            case 11:
                return R.color.color12;

            case 12:
                return R.color.color13;

            case 13:
                return R.color.color14;

            case 14:
                return R.color.color15;

            case 15:
                return R.color.color16;

            case 16:
                return R.color.color17;

            case 17:
                return R.color.color18;

            case 18:
                return R.color.color19;

            case 19:
                return R.color.color20;

            case 20:
                return R.color.color21;

            case 21:
                return R.color.color22;

            case 22:
                return R.color.color23;

            case 23:
                return R.color.color24;

            case 24:
                return R.color.color25;

            case 25:
                return R.color.color26;

            case 26:
                return R.color.color27;

            case 27:
                return R.color.color28;

            case 28:
                return R.color.color29;

            case 29:
                return R.color.color30;

            case 30:
                return R.color.color31;

            case 31:
                return R.color.color32;

            case 32:
                return R.color.color33;

            case 33:
                return R.color.color34;

            case 34:
                return R.color.color35;

            case 35:
                return R.color.color36;

            case 36:
                return R.color.color37;

            case 37:
                return R.color.color38;

            case 38:
                return R.color.color39;

            case 39:
                return R.color.color40;

            case 40:
                return R.color.color41;

            case 41:
                return R.color.color42;

            case 42:
                return R.color.color43;

            case 43:
                return R.color.color44;

            case 44:
                return R.color.color45;

            case 45:
                return R.color.color46;

            case 46:
                return R.color.color47;

            case 47:
                return R.color.color48;

            case 48:
                return R.color.color49;

            case 49:
                return R.color.color50;

            default:
                return R.color.deeppurple;
        }
    }

    static void setColor(CardView cardView, Context context, int color) {
        try {
            cardView.setCardBackgroundColor(context.getResources().getColor(color));
        } catch (NullPointerException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            cardView.setCardBackgroundColor(context.getResources().getColor(R.color.green));
        }
    }

    public static String capitalize(String searchKey) {
        String[] temp = searchKey.split(" ");
        StringBuilder searchKeyBuilder = new StringBuilder();
        for (String key : temp)
            searchKeyBuilder.append(key.substring(0, 1).toUpperCase()).append(key.substring(1).toLowerCase()).append(" ");
        searchKey = searchKeyBuilder.toString();
        return searchKey.trim();
    }

    public static void loadWallet(Context context, long wallet, TextView textView) {
        String w = String.valueOf(wallet);
        if (wallet < 0) {
            w = w.replace("-", "Pending ₹ ");
            textView.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            w = "₹ " + w;
            textView.setTextColor(context.getResources().getColor(R.color.green_light));
        }

        textView.setText(String.format(Locale.UK, " %s", w));
    }

    public static void makeHttpCall(Context context, String apiName, JSONObject request, View progress) {
        assert progress != null;
        if (SendNotificationActivity.isNetworkAvailable(context)) {
            progress.animate().setDuration(500).alpha(1).withEndAction(() -> progress.setVisibility(View.VISIBLE));
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://us-central1-billrec.cloudfunctions.net/" + apiName, request,
                    response -> {
                        try {
                            if (response.getString("status").equals("200")) {
                                if (progress.getVisibility() != View.VISIBLE) {
                                    storeStatusNotification("Successfully executed " + apiName + " process\n" + response.getString("message") + "\n" + response.getString("extra")
                                            , 1, context);
                                } else progress.setVisibility(View.GONE);
                                Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show();
                            } else {
                                if (progress.getVisibility() != View.VISIBLE) {
                                    storeStatusNotification("Failed to execute " + apiName + " process\n" + response.getString("message") + "\n" + response.getString("extra")
                                            , 0, context);
                                } else {
                                    progress.setVisibility(View.GONE);
                                    makeAlertDialog(context, response.getString("message"), response.getString("extra"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progress.setVisibility(View.GONE);
                        Log.e("BillAlert", "Notification response " + response.toString());
                    },
                    error -> {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(context, "Request pending " + apiName + ", check alert for the status of the request", Toast.LENGTH_LONG).show();
                        Log.e("Volley Error", "error " + error.getMessage() + " cause " + error.getCause());
                        error.printStackTrace();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    long length = 0;
                    if (request != null)
                        length = request.toString().length();
                    HashMap<String, String> params = new HashMap<>();
                    params.put("Content-Length", "" + length);
                    params.put("Content-Type", "application/json");
                    return params;
                }

            };
            jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 60000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 0;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    if (error instanceof TimeoutError) {
                        ((Activity) context).runOnUiThread(() -> {
                            if (progress.getVisibility() == View.VISIBLE) {
                                progress.setVisibility(View.GONE);
                                makeAlertDialog(context, "Timeout", "Process taking longer time to complete. We will notify you when the process completed in the alert screen. Please before retry wait for another 5 minutes if no alert is shown in the alert screen.\nAlert Screen\nNotification > Bell icon > Alert Screen");
                            }
                        });

                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
        } else {
            makeAlertDialog(context, "No internet", "Cannot connect with the server. Please use a stable internet connection");
        }
    }

    public void makeHttpCall(Context context, String apiName, JSONObject request, String from, View progressbar, String uid) {
        progressbar.animate().setDuration(500).alpha(1).withEndAction(() -> progressbar.setVisibility(View.VISIBLE));
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://us-central1-billrec.cloudfunctions.net/" + apiName, request,
                response -> {
                    try {
                        if (response.getString("status").equals("200"))
                            executeFunction(from, progressbar, context, uid);
                        else {
                            if (progressbar.getVisibility() == View.VISIBLE) {
                                progressbar.setVisibility(View.GONE);
                                makeAlertDialog(context, response.getString("message"), response.getString("extra"));
                            } else
                                storeStatusNotification(response.getString("message") + "\n" + "Error: " + response.getString("extra"), 0, context);
                        }
                        //progressbar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        progressbar.setVisibility(View.GONE);
                        Toast.makeText(context, "Failed Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Log.e("BillAlert", "Notification response " + response.toString());
                },
                error -> {
                    if (progressbar.getVisibility() == View.VISIBLE) {
                        Toast.makeText(context, "Failed Error:" + error.getMessage(), Toast.LENGTH_LONG).show();
                        progressbar.setVisibility(View.GONE);

                    } else {
                        String msg = "Error occurred in " + apiName + " process";
                        if (error.getMessage() != null)
                            msg += "\nError: " + error.getMessage();
                        if (error.getCause() != null)
                            msg += "\nCause: " + error.getCause();
                        storeStatusNotification(msg, 0, context);
                        Log.e("Volley Error", "error " + error.getMessage());
                    }
                    error.printStackTrace();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                long length = 0;
                if (request != null)
                    length = request.toString().length();
                HashMap<String, String> params = new HashMap<>();
                params.put("Content-Length", "" + length);
                params.put("Content-Type", "application/json");
                return params;
            }

        };
        requestQueue.add(jsonObjectRequest);
    }

    private void executeFunction(String extraFunction, View progressbar, Context context, String uid) {

        switch (extraFunction) {
            case "enableUser":
                enableUser(progressbar, context, uid);
                break;
            case "disableUser":
                disableUser(progressbar, context, uid);
                break;
        }
    }


    private void disableUser(View progressbar, Context context, String uid) {
        new TransactionDb().getDatabaseReference(context).child("Extra/LastAccessed").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //undo-here
        FirebaseDatabase.getInstance().getReference("Customers")
//            new TransactionDb().getDatabaseReference(context,"test1-b0b2c").child("Customers")
                .child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                new TransactionDb().getDatabaseReference(context).child("Blocked")
                        .child(uid).setValue(snapshot.getValue()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new TransactionDb().getDatabaseReference(context).child("Blocked")
                                .child(uid).child("NOTIFY_ADMIN").setValue(false);
                        snapshot.getRef().setValue(null);
                        progressbar.setVisibility(View.GONE);
                        //TODO add block user api
                        String message = "User disabled\n" + snapshot.child("NAME").getValue() + "\n" + snapshot.child("MOBILE").getValue();
                        storeStatusNotification(message, 1, context);
                        Toast.makeText(context, "User blocked", Toast.LENGTH_LONG).show();
                        endActivity(context);
                    } else {
                        Toast.makeText(context, "Error occurred", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error occurred " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    static public void storeStatusNotification(String message, int status, Context context) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status);
        map.put("time", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance(new TransactionDb().loadFirebase(context)).collection("Saved Notification").document().set(map);
    }

    private void enableUser(View progressbar, Context context, String uid) {
        //undo-here
        FirebaseDatabase.getInstance().getReference().child("Extra/LastAccessed").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        new TransactionDb().getDatabaseReference(context).child("Blocked")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //undo-here
                        HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
                        map.put("NOTIFY_ADMIN", false);
                        FirebaseDatabase.getInstance().getReference("Customers")
//                new TransactionDb().getDatabaseReference(context,"test1-b0b2c").child("Customers")
                                .child(uid)
                                .setValue(map).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                snapshot.getRef().setValue(null);
                                progressbar.setVisibility(View.GONE);
                                String message = "User enabled \n" + snapshot.child("NAME").getValue() + "\n" + snapshot.child("MOBILE").getValue();
                                storeStatusNotification(message, 1, context);
                                //TODO add unblock user api
//                        Toast.makeText(context,"User blocked",Toast.LENGTH_LONG).show();
                                endActivity(context);
                            } else {
                                if (progressbar.getVisibility() == View.VISIBLE)
                                    progressbar.setVisibility(View.GONE);
                                else {
                                    String message = "User not enabled" + snapshot.child("NAME").getValue() + "\n" + snapshot.child("Mobile").getValue() + "\nError: " + task.getException().getMessage();
                                    storeStatusNotification(message, 0, context);
                                }
                                Toast.makeText(context, "Error occurred", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error occurred " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    void endActivity(Context context) {
        Intent intent = new Intent(context, HomeAdmin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void makeAlertDialog(Context context, String title, String message) {

        try {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            storeStatusNotification(message, 3, context);
        }
    }
}
