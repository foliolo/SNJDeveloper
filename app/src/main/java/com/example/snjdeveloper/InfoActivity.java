package com.example.snjdeveloper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.admin.CustomerInfoActivity;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.example.snjdeveloper.search.Recent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class InfoActivity extends AppCompatActivity {
    String uid,mobile;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        receive();
    }
    String getFormatedTime(String time){
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy hh:mm aa", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(time));
        return dateFormat.format(calendar.getTime());
    }

    void receive(){
        Intent i=getIntent();
        TextView textView=findViewById(R.id.title);
        textView.setText(i.getStringExtra("1"));
        String qr;
        TextView textView1=findViewById(R.id.oneTitle);
        String type;
        if(Objects.equals(i.getStringExtra("6"), "null"))
        {   type = "Bottles";
            qr = "re";
            textView1.setText(R.string.bottles_id);
            TextView textView2=findViewById(R.id.quant_text);
            textView2.setText("Total Returned Bottles");
            findViewById(R.id.amtLinear).setVisibility(View.GONE);
            findViewById(R.id.paid_amount_linear).setVisibility(View.GONE);
            findViewById(R.id.paidBy_Linar).setVisibility(View.GONE);

        } else if (Objects.equals(i.getStringExtra("3"), "0")) {
            qr = "tr";
            new Recent().addNewRecent(i.getStringExtra("1"), "Transactions", this);
            textView1.setText(R.string.trans_no);
            type = "Transactions";
        } else {
            type = "Orders";
            new Recent().addNewRecent(i.getStringExtra("1"), "Orders", this);
            qr = "or";
            textView1.setText(R.string.order_number);
        }
        if (i.getStringExtra("0") == null)
            fetchDataFromIntent(i, qr, type);
        else fetchDataFromFirebase(i, qr);
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            startActivity(new Intent(this, HomeAdmin.class));
            finish();
        } else
            super.onBackPressed();
    }

    private void fetchDataFromFirebase(Intent intent, String qr) {
        String id = intent.getStringExtra("DATA");
        String type = intent.getStringExtra("1");
        if (id != null) {
            loadImage("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F" +
                    id + "_" + qr + "?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d");
            if (type.equalsIgnoreCase("Bottles")) {
                FirebaseFirestore.getInstance().collection(type).document(id).get().addOnSuccessListener(documentSnapshot -> {
                    String quant, note, time;
                    try {
                        uid = (String) documentSnapshot.get("UID");
                        fetchUerInfo();
                    } catch (Exception ignored) {
                    }
                    try {
                        quant = String.valueOf(documentSnapshot.get("QUANTITY"));
                    } catch (Exception e) {
                        quant = null;
                    }
                    try {
                        note = (String) documentSnapshot.get("NOTE");
                    } catch (Exception e) {
                        note = null;
                    }
                    try {
                        time = String.valueOf(documentSnapshot.get("TIME"));
//                       time = (String) documentSnapshot.get("DATE");
                    } catch (Exception e) {
                        time = null;
                    }
                    setData(id, getFormatedTime(time), quant, note, null, null, null);
                });
            } else {
                DatabaseReference query;
                if (type.contains("Trans"))
                    query = new TransactionDb().getReference(this);
                else query = FirebaseDatabase.getInstance().getReference(type);

                query.child(id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String quant, note, amt, via, paidamt, time;
                                try {
                                    uid = (String) dataSnapshot.child("UID").getValue();
                                    fetchUerInfo();
                                } catch (Exception ignored) {
                                }
                                try {
                                    quant = String.valueOf(dataSnapshot.child("QUANTITY").getValue());
                                } catch (Exception e) {
                                    quant = null;
                                }
                                try {
                                    note = (String) dataSnapshot.child("NOTE").getValue();
                                } catch (Exception e) {
                                    note = null;
                                }
                                try {
                                    amt = String.valueOf(dataSnapshot.child("AMOUNT").getValue());
                                } catch (Exception e) {
                                    amt = null;
                                }

                                try {
                                    paidamt = String.valueOf(dataSnapshot.child("PAID_AMOUNT").getValue());
                                } catch (Exception e) {
                                    paidamt = null;
                                }
                                try {
                                    via = String.valueOf(dataSnapshot.child("PAID_VIA").getValue());
                                } catch (Exception e) {
                                    via = null;
                                }
                                try {
//                                   time = String.valueOf(dataSnapshot.child("TIME").getValue());
                                    time = (String) dataSnapshot.child("DATE").getValue();

                                } catch (Exception e) {
                                    time = null;
                                }
                                setData(id, getFormatedTime(time), quant, note, amt, via, paidamt);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        } else {

            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show();
            onBackPressed();
        }
    }
    private void fetchUerInfo(){
        FirebaseDatabase.getInstance().getReference("Customers").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    findViewById(R.id.user_arrow).setVisibility(View.VISIBLE);
                mobile = (String) dataSnapshot.child("MOBILE").getValue();
                TextView textView = findViewById(R.id.username);
                textView.setText((String) dataSnapshot.child("NAME").getValue());
                findViewById(R.id.cred).setClickable(true);
                textView = findViewById(R.id.mobile);
                textView.setVisibility(View.VISIBLE);
                textView.setText(mobile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fetchDataFromIntent(Intent i, String qr, String type) {
        loadImage("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F" +
                i.getStringExtra("1") + "_" + qr + "?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d");
        setData(i.getStringExtra("1"), getFormatedTime(i.getStringExtra("2")), i.getStringExtra("3"),
                i.getStringExtra("4"), i.getStringExtra("5"), i.getStringExtra("6")
                , i.getStringExtra("7"));

        if (type.equalsIgnoreCase("Bottles")) {
            FirebaseFirestore.getInstance().collection(type).document(i.getStringExtra("1")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    try {
                        uid = (String) documentSnapshot.get("UID");
                        fetchUerInfo();
                    } catch (Exception ignored) {
                    }
                }
            });
        } else {
            DatabaseReference query;
            if (type.contains("Trans"))
                query = new TransactionDb().getReference(this);
            else query = FirebaseDatabase.getInstance().getReference(type);
            query.child(i.getStringExtra("1"))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                uid = (String) dataSnapshot.child("UID").getValue();
                                fetchUerInfo();
                            } catch (Exception ignored) {
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    void loadImage(String link){
        ImageView imageView=findViewById(R.id.qrimage);
        Glide.with(this).load(link).
                into(imageView);
    }

    private void setData(String no, String time,
                         String quant, String note, String amt,
                         String via, String paidamt) {
        TextView textView;
        if(Objects.equals(quant, "null")|| Objects.equals(quant, "0")){
            TextView textView1=findViewById(R.id.oneTitle);
            textView1.setText(R.string.trans_no);
        }else {
            textView=findViewById(R.id.quantity);
            findViewById(R.id.qntLinear).setVisibility(View.VISIBLE);
            textView.setText(quant);
        }
        textView=findViewById(R.id.orderno);
        textView.setText(no);
        if(amt!=null && !amt.equals("null") && !Objects.equals(amt, "0")) {
            textView = findViewById(R.id.amount);
            findViewById(R.id.amtLinear).setVisibility(View.VISIBLE);
            textView.setText(String.format("₹ %s", amt));
        }
        if(note!=null) {
            textView = findViewById(R.id.note);
            findViewById(R.id.noteLinear).setVisibility(View.VISIBLE);
            textView.setText(note);
        }

        textView=findViewById(R.id.date);
        textView.setText(time);
        if(via!=null) {
            textView = findViewById(R.id.paidBy);
            textView.setText(via);
        }
        if(paidamt!=null) {
            textView = findViewById(R.id.paidAmt);
            textView.setText(String.format("₹ %s", paidamt));
        }
    }

    public void backOnClicked(View view) {
//        finish();
        onBackPressed();
    }


    public void userOnClick(View view) {
        if(mobile!=null && !mobile.isEmpty()) {
            Intent intent = new Intent(this, CustomerInfoActivity.class);
            intent.putExtra("0", "BarCode");
            intent.putExtra("UID", uid);
            intent.putExtra("MOBILE", mobile);
            startActivity(intent);
        }
    }
}
