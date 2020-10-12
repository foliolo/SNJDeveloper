
package com.example.snjdeveloper.settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snjdeveloper.ExtraActivity;
import com.example.snjdeveloper.LoginActivity;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private String developer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        TextView textView = findViewById(R.id.title);
        textView.setText("Settings");
        loadData();

    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            startActivity(new Intent(this, HomeAdmin.class));
            finish();
        } else
            super.onBackPressed();
    }

    private void loadData() {
        FirebaseDatabase.getInstance().getReference("Customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    TextView textView = findViewById(R.id.total_users);
                    textView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    TextView textView = findViewById(R.id.total_order);
                    textView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    long total_bottles_given = 0;
                    if (dataSnapshot.hasChildren())
                        for (DataSnapshot d : dataSnapshot.getChildren())
                            total_bottles_given += (long) d.child("QUANTITY").getValue();
                    textView = findViewById(R.id.total_bottle);

                    TextView finalTextView = textView;
                    long finalTotal_bottles_given = total_bottles_given;
                    FirebaseFirestore.getInstance().collection("Bottles").get().addOnSuccessListener(queryDocumentSnapshots -> {
                        long return_bottle = 0;
                        try {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments())
                                    return_bottle += document.getLong("QUANTITY");
                            }
                            finalTextView.setText(String.format(Locale.UK, "Total %d bottles sold, %d bottles returned",
                                    finalTotal_bottles_given, return_bottle));

                        } catch (Exception e) {
                            finalTextView.setText("Failed to load");
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        FirebaseDatabase.getInstance().getReference("Transactions")
        new TransactionDb().getReference(this)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            TextView textView = findViewById(R.id.total_transaction);
                            textView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                            long total = 0;
                            for (DataSnapshot d : dataSnapshot.getChildren())
                                total += (long) d.child("PAID_AMOUNT").getValue();

                            textView = findViewById(R.id.total_earnings);
                            textView.setText(getFormatedAmount(total));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
        });
        TextView textView=findViewById(R.id.last_login);
        SharedPreferences sharedPreferences=getSharedPreferences("CRED",MODE_PRIVATE);
        String date=sharedPreferences.getString("LAST_LOGIN","Not Available");
        textView.setText(date);

        FirebaseDatabase.getInstance().getReference("Developers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    developer= (String) dataSnapshot.child("EMAIL").getValue();
                    long dblimit=(long)dataSnapshot.child("DBLIMIT").getValue();
                    double dbsize=(double)dataSnapshot.child("DBSIZE").getValue();
                    long stlimit=(long)dataSnapshot.child("STLIMIT").getValue();
                    double stsize=(double)dataSnapshot.child("STSIZE").getValue();
                    int perdb= (int) Math.abs((Math.abs(dbsize)/dblimit)*100);
                    int perst=(int) Math.abs((Math.abs(stsize)/stlimit)*100);
                    if (perst==0)
                        perst=1;
                    if (perdb==0)
                        perdb=1;

                    findViewById(R.id.database).setVisibility(View.VISIBLE);
                    findViewById(R.id.storage).setVisibility(View.VISIBLE);
                    ProgressBar dbseek=findViewById(R.id.dbProgress);
                    ProgressBar stseek=findViewById(R.id.stProgress);
                    Handler handler=new Handler();
                    int finalPerdb = perdb;
                    int finalPerst = perst;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                dbseek.setProgress(finalPerdb,true);
                                stseek.setProgress(finalPerst,true);
                            }
                            else{
                                dbseek.setProgress(finalPerdb);
                                stseek.setProgress(finalPerst);
                            }

                    TextView textView=findViewById(R.id.dbSize);
                    textView.setText( dbsize+"MB");
                    textView=findViewById(R.id.dblimit);
                    textView.setText( dblimit+"MB");
                    textView=findViewById(R.id.stlimit);
                    textView.setText( stlimit+"MB");
                    textView=findViewById(R.id.stSize);
                    textView.setText(stsize+"MB");
                        }
                    },1000);

                }catch (Exception e){e.printStackTrace();}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Admin/app").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    long total = 0;
                    double sum_rate = 0;

                    try {
                        if (dataSnapshot.child("TOTAL_RATES").exists())
                            total = (long) dataSnapshot.child("TOTAL_RATES").getValue();
                        if (total != 0) {
                            try {
                                if (dataSnapshot.child("TOTAL_SUM").exists())
                                    sum_rate = (double) dataSnapshot.child("TOTAL_SUM").getValue();
                            } catch (Exception e) {
                                sum_rate = (long) dataSnapshot.child("TOTAL_SUM").getValue();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sum_rate = 0;
                        total = 0;
                    }
                    String s = "0";
                    if (total != 0) {
                        double sum = sum_rate / total;
                        TextView textView = findViewById(R.id.rating);
                        s = String.format(Locale.UK, "%.1f", sum);
                        textView.setText(String.format(Locale.UK,
                                "%s",
                                s));
                        textView.setVisibility(View.VISIBLE);
                        textView = findViewById(R.id.totalRating);
                        textView.setText("from " + total + " ratings");
                    } else {
                        findViewById(R.id.rating).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.totalRating)).setText("No ratings found");
                    }
                    long finalTotal = total;
                    String finalS = s;
                    findViewById(R.id.ratingfeedback).setOnClickListener(view -> {
                        Intent intent = new Intent(SettingsActivity.this, ExtraActivity.class);
                        intent.putExtra("RATING", finalS);
                        intent.putExtra("TOTALRATE", finalTotal);
                        startActivity(intent);
                    });


                }catch (Exception e){e.printStackTrace();}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private String getFormatedAmount(long amount){
        return "₹ "+ NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }

    public void logoutclick(View view) {
        showAlertDialog("Are you sure you want to logout.");
    }
    private void showAlertDialog(String msg) {
                 AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout");
                alert.setMessage(msg);
        alert.setPositiveButton("Logout", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            clearPreferences("CRED");
            clearPreferences("RECENT");
            clearPreferences("RECENT_SEARCH");
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        alert.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    void clearPreferences(String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }

    public void emptySearchbarClicked(View view) {
    }

    public void endActivity(View view) {
    }


    public void developerOnCick(View view) {
      Intent intent=new Intent(Intent.ACTION_SENDTO);
      intent.setData(Uri.parse("mailto:"+developer));
      startActivity(intent);
    }


}
