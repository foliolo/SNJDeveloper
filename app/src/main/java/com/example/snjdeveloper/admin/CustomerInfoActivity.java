package com.example.snjdeveloper.admin;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.snjdeveloper.Constants;
import com.example.snjdeveloper.ImageViewerActivity;
import com.example.snjdeveloper.LoadRecentUsersFragment;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.StatsActivity;
import com.example.snjdeveloper.StatsBottleActivity;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.newdata.NewOrderActivity;
import com.example.snjdeveloper.newdata.NewReturnBottlesActivity;
import com.example.snjdeveloper.newdata.NewTransactionActivity;
import com.example.snjdeveloper.notification.NewScheduledNotification;
import com.example.snjdeveloper.notification.SendNotificationActivity;
import com.example.snjdeveloper.notification.SendPopupNotificationActivity;
import com.example.snjdeveloper.user.DeleteUserActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class CustomerInfoActivity extends AppCompatActivity {
    private String uid;
    private String mobile;
    private String name;
    private long wallet;
    View progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_info);
        TextView textView = findViewById(R.id.title);
        textView.setText("User Information");
        initializeProgress();
        Intent intent = getIntent();
        if (intent.getStringExtra("0") == null)
            fetchDataFromIntent(intent);
        else fetchDataFromFirebase(intent);
        fetchOrderAndTransaction();
        if (intent.getBooleanExtra("IS_BLOCKED", false)) {
            CardView blockCardInfo = findViewById(R.id.blockUserCard);
            blockCardInfo.setVisibility(View.VISIBLE);
            blockCardInfo.setOnClickListener(v -> delUserClicked(null));
            findViewById(R.id.newOrder).setVisibility(View.GONE);
            findViewById(R.id.newTransaction).setVisibility(View.GONE);
            findViewById(R.id.newBottle).setVisibility(View.GONE);
            findViewById(R.id.sendBill).setVisibility(View.GONE);
            findViewById(R.id.rate_change).setVisibility(View.GONE);
            findViewById(R.id.sendPopup).setVisibility(View.GONE);
            findViewById(R.id.sendInfoBox).setVisibility(View.GONE);
        }
        setNotificationListener(findViewById(R.id.sendPopup), "", uid, SendPopupNotificationActivity.class);
        setNotificationListener(findViewById(R.id.sendInfoBox), "", uid, NewScheduledNotification.class);
    }

    private void initializeProgress() {
        progress = findViewById(R.id.progressbar_dialog);
        TextView textView1 = findViewById(R.id.progressTitle);
        textView1.setText("Sending bill");
        findViewById(R.id.background_Bt).setOnClickListener(v ->
                progress.animate().setDuration(500).alpha(0).withEndAction(() -> progress.setVisibility(View.GONE)));
    }

    private void setNotificationListener(View view, String from, String disData, Class className) {
        view.setOnClickListener(v -> {
            setNotificationIntentExtra(disData, from, className);
        });
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            startActivity(new Intent(this, HomeAdmin.class));
            finish();
        } else if (progress.getVisibility() != View.VISIBLE)
            super.onBackPressed();
    }

    private void fetchDataFromFirebase(Intent intent) {
        uid = intent.getStringExtra("UID");
        mobile = intent.getStringExtra("MOBILE");
        FirebaseDatabase.getInstance().getReference("Customers/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String address;
                long rate;
                String email;
                getUserNotificationToken(dataSnapshot);
                try {
                    email = ((String) dataSnapshot.child("USER_EMAIL").getValue());
                    if (email == null)
                        email = "Not Found";
                } catch (Exception e) {
                    email = "Not Found";
                }
                try {
                    name = (String) dataSnapshot.child("NAME").getValue();
                } catch (Exception e) {
                    name = "Not Found";
                }
                try {
                    address = (String) dataSnapshot.child("USER_ADDRESS").getValue();
                } catch (Exception e) {
                    address = "Not Found";
                }
                try {
                    wallet = (long) dataSnapshot.child("WALLET").getValue();
                } catch (Exception e) {
                    wallet = 0;
                }

                try {
                    rate = (long) dataSnapshot.child("RATE").getValue();
                } catch (Exception e) {
                    rate = 0;
                }

                try {
                    TextView textView = findViewById(R.id.total_remaining);
                    Object bottles = dataSnapshot.child("WATER_BOTTLE_PENDING").getValue();
                    if (bottles == null)
                        textView.setText(", remaining 0 bottles");
                    else
                        textView.setText(String.format(Locale.UK, ", remaining %d bottles",
                                dataSnapshot.child("WATER_BOTTLE_PENDING").getValue()));
                } catch (Exception ignored) {
                }
                findViewById(R.id.delUser).setVisibility(View.VISIBLE);
                setData(uid, mobile, name, address, rate, wallet, email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserNotificationToken(DataSnapshot snapshot) {
        if (snapshot == null) {
            FirebaseDatabase.getInstance().getReference("Customers/" + uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild("FB_TOKEN")) {
                                findViewById(R.id.sendNotification).setVisibility(View.VISIBLE);
                                setNotificationListener(findViewById(R.id.sendNotification),
                                        "", (String) snapshot.child("FB_TOKEN").getValue(), SendNotificationActivity.class);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            if (snapshot.hasChild("FB_TOKEN")) {
                findViewById(R.id.sendNotification).setVisibility(View.VISIBLE);
                setNotificationListener(findViewById(R.id.sendNotification), "Send notification", (String) snapshot.child("FB_TOKEN").getValue(), SendNotificationActivity.class);
            }
        }
    }

    private void saveInLocal(String temp) {
        if (temp != null) {
            if (!temp.isEmpty()) {
                String[] d = temp.split(" ");
                if (d.length > 1)
                    temp = d[0].substring(0, 1) + d[1].substring(0, 1);
                else temp = temp.substring(0, 1);
            }
            new LoadRecentUsersFragment()
                    .addNewRecent(mobile, uid, temp.toUpperCase(), CustomerInfoActivity.this);

        }

    }

    private void fetchOrderAndTransaction() {
        Log.e("CustomerData", "UID " + uid);
        FirebaseDatabase.getInstance().getReference("Orders")
                .orderByChild("UID").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String total = String.valueOf(dataSnapshot.getChildrenCount());
                    if (total.equals("0"))
                        findViewById(R.id.order).setVisibility(View.GONE);
                    TextView textView1 = findViewById(R.id.total_order);
                    textView1.setText(total);
                    long tot=0;
                    for(DataSnapshot d:dataSnapshot.getChildren()) {
                        tot += (long) d.child("QUANTITY").getValue();
                    }
                    if (tot==0)
                        findViewById(R.id.bottle).setVisibility(View.GONE);
                    TextView textView=findViewById(R.id.total_bottle);
                    textView.setText(tot+" bottles taken ");
                }catch (Exception e){e.printStackTrace();}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        new TransactionDb().getReference(this)
                .orderByChild("UID").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    TextView textView=findViewById(R.id.total_transaction);
                    long size= dataSnapshot.getChildrenCount();
                    if (size==0)
                        findViewById(R.id.see_trans).setVisibility(View.GONE);

                    long total=0;
                    for(DataSnapshot d:dataSnapshot.getChildren()) {
                        Log.e("DataTrans",""+d.child("UID").getValue());
                        total += (long) d.child("PAID_AMOUNT").getValue();
                    }

                    textView.setText(String.format(Locale.UK,"%s from %d transactions", getFormatedAmount(total), size));
                }catch (Exception e){e.printStackTrace();}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void fetchDataFromIntent(Intent intent) {
        if (!intent.getBooleanExtra("IS_BLOCKED", false))
            findViewById(R.id.delUser).setVisibility(View.VISIBLE);
        uid = intent.getStringExtra("1");
        mobile = intent.getStringExtra("3");
        name = (intent.getStringExtra("2"));
        getUserNotificationToken(null);
        String email = getIntent().getStringExtra("7");
        if (email == null)
            email = "Not found";
        String address = (intent.getStringExtra("5"));
        long rate;
        try {
            rate = intent.getLongExtra("6", 0);
        } catch (Exception e) {
            e.printStackTrace();
            rate = 0;
        }

        try {
            wallet = intent.getLongExtra("4", 0);
        } catch (Exception e) {
            wallet = 0;
            e.printStackTrace();
        }

        setData(uid, mobile, name, address, rate, wallet, email);

    }

    void setData(String uid, String mobile, String name, String address, long rate, long wallet, String email) {
        saveInLocal(name);
        try {
            ImageView imageView = findViewById(R.id.imageView);
            String img = "https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F" +
                    uid + "_pr" + "?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d";
            Glide.with(this).load(img)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setOnClickListener(v -> {
                                Intent intent = new Intent(CustomerInfoActivity.this, ImageViewerActivity.class);
                                intent.putExtra("IMG", img);
                                startActivity(intent);
                            });
                            return false;
                        }
                    }).

                    into(imageView);
        } catch (Exception ignored) {
        }
        TextView textView = findViewById(R.id.mobilenumber);
        textView.setText(mobile);
        textView = findViewById(R.id.name);
        textView.setText(name);
        textView = findViewById(R.id.shop_address);
        textView.setText(address);
        TextView textView1 = findViewById(R.id.rate);
        textView1.setText("₹ " + rate);
        TextView emailText = findViewById(R.id.email_address);
        if (email.contains("@"))
            setEmailListener(email);
        emailText.setText(email);
        Constants.loadWallet(this, wallet, findViewById(R.id.wallet));
    }

    private void setEmailListener(String email) {
        findViewById(R.id.sendEmail).setOnClickListener(v -> {
            setNotificationIntentExtra(email, "Send email message", SendNotificationActivity.class);

        });

    }

    private void setNotificationIntentExtra(String data, String from, Class className) {
        Intent intent = new Intent(this, className);
        intent.putExtra("DATA", data);
        intent.putExtra("from", from);
        if (data.contains("@"))
            data = mobile + "\n" + data;
        else data = mobile;
        String disData = uid + "," + data + "," + name + "," + mobile;
        intent.putExtra("DIS_DATA", disData);
        startActivity(intent);
    }

    private String getFormatedAmount(long amount) {
        return "₹ " + NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }

    public void changeRateOnClick(View view) {
        findViewById(R.id.rate).setVisibility(View.INVISIBLE);
        findViewById(R.id.rate_edit_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.rate_edit).requestFocus();
        view.setVisibility(View.GONE);
    }

    public void seeOrderOnClick(View view) {
        Intent intent=new Intent(this, StatsActivity.class);
        intent.putExtra("UID",uid);
        intent.putExtra("MOBILE",mobile);
        intent.putExtra("CLASS_NAME","Orders");
        startActivity(intent);
    }

    public void seeTransOnClick(View view) {
        Intent intent=new Intent(this, StatsActivity.class);
        intent.putExtra("UID",uid);
        intent.putExtra("MOBILE",mobile);
        intent.putExtra("CLASS_NAME","Transactions");
        startActivity(intent);
    }

    public void seeBottleOnClick(View view) {
        Intent intent = new Intent(this, StatsBottleActivity.class);
        intent.putExtra("UID", uid);
        intent.putExtra("MOBILE", mobile);
        intent.putExtra("CLASS_NAME", "Bottles");
        startActivity(intent);
    }

    void showSnackbarMsg(String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.order_text), msg, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setBackgroundTint(getResources().getColor(R.color.red));
        snackbar.show();
    }

    public void saveRateOnClick(View view) {
        EditText rate_edit = findViewById(R.id.rate_edit);
        String rate = rate_edit.getText().toString().trim();
        if (rate.isEmpty()) {
            showSnackbarMsg("Enter rate");
        } else if (rate.equals("0")) {
            showSnackbarMsg("Rate should be greater than 0");
        } else if (rate.startsWith("0")) {
            showSnackbarMsg("Rate should not start with 0");
        } else {
            rate_edit.setText("");
            findViewById(R.id.rateProgress).setVisibility(View.VISIBLE);
            Button bt = (Button) view;
            bt.setText("");
            FirebaseDatabase.getInstance().
                    getReference("Customers/" + uid)
                    .child("RATE").
                    setValue(Long.parseLong(rate)).addOnSuccessListener(aVoid -> {

                findViewById(R.id.rateProgress).setVisibility(View.GONE);
                        TextView textView=findViewById(R.id.rate);
                        textView.setText(getFormatedAmount(Long.parseLong(rate)));
                        bt.setText("SAVE");
                        cancelRateOnClick(new View(this));
                    }).addOnFailureListener(e -> {
                findViewById(R.id.rateProgress).setVisibility(View.GONE);
                TextView textView=findViewById(R.id.rate);
                textView.setText(String.format(Locale.UK,"%s .Failed to modify the rate.", textView.getText().toString()));
                cancelRateOnClick(new View(this));
                bt.setText("SAVE");
            });
        }
    }

    public void cancelRateOnClick(View view) {
        findViewById(R.id.rate_edit_layout).setVisibility(View.GONE);
        findViewById(R.id.rate).setVisibility(View.VISIBLE);
        findViewById(R.id.rate_change).setVisibility(View.VISIBLE);
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }

    public void newOrderClick(View view) {
        Intent intent=new Intent(this, NewOrderActivity.class);
        intent.putExtra("MOBILE",mobile);
        intent.putExtra("UID",uid);
        startActivity(intent);
    }

    public void newTransactionOnClick(View view) {
        Intent intent=new Intent(this, NewTransactionActivity.class);
        intent.putExtra("MOBILE", mobile);
        intent.putExtra("UID", uid);
        startActivity(intent);
    }

    public void newBottleClick(View view) {
        Intent intent = new Intent(this, NewReturnBottlesActivity.class);
        intent.putExtra("MOBILE", mobile);
        intent.putExtra("UID", uid);
        startActivity(intent);
    }

    public void delUserClicked(View view) {
        Intent intent = new Intent(this, DeleteUserActivity.class);
        intent.putExtra("IS_BLOCKED", view == null);
        intent.putExtra("NAME", name);
        intent.putExtra("MOBILE", mobile);
        intent.putExtra("WALLET", wallet);
        intent.putExtra("UID", uid);
        startActivity(intent);
    }

    public void sendBillClicked(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Send bill")
                .setMessage("Are you sure you want to send bill to " + name)
                .setPositiveButton("yes", (dialog, which) -> {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("uid", uid);
                        Constants.makeHttpCall(CustomerInfoActivity.this, "bill_report-sendBill", jsonObject, progress);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
