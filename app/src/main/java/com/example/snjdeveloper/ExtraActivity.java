package com.example.snjdeveloper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ExtraActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);
        TextView textView=findViewById(R.id.title);
        textView.setText("Feedback");
        loadData();
        loadListview();
    }

    private void loadListview() {
        FirebaseDatabase.getInstance().getReference("Admin/chats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Map<String, String>> list = new ArrayList<>();
                Log.e("Extra","Chat loaded");
                try {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Map<String,String> map=new HashMap<>();
                    map.put("name", (String) Objects.requireNonNull(d.child("NAME").getValue()));
                    map.put("msg",(String) Objects.requireNonNull(d.child("MESSAGE").getValue()));
                    list.add(map);
                    }

                    Log.e("Extra",list.toString());
                    if (list.size() > 0) {
                        ListView listView = findViewById(R.id.listview);
                        SimpleAdapter adapter = new SimpleAdapter(
                                ExtraActivity.this
                                , list
                                , android.R.layout.simple_list_item_2
                                , new String[]{"name", "msg"}
                                , new int[]{android.R.id.text1, android.R.id.text2});
                        listView.setAdapter(adapter);
                    }
                }catch (Exception e){e.printStackTrace();}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadData() {
        Intent intent =getIntent();
        TextView textView = findViewById(R.id.avg_rate);
        String rate=intent.getStringExtra("RATING");
        if (rate!=null) {
            textView.setText(String.format(Locale.UK,
                    "%s",
                    rate));
            textView.setVisibility(View.VISIBLE);
            textView = findViewById(R.id.total_rate);
            textView.setText(String.format(Locale.UK,"Total %d ratings",
                    intent.getLongExtra("TOTALRATE", 0)));
            RatingBar ratingBar = findViewById(R.id.ratingbar);
            ratingBar.setVisibility(View.VISIBLE);
            try {
                ratingBar.setRating(Float.parseFloat(rate));
                findViewById(R.id.progressbar).setVisibility(View.GONE);
            }catch (Exception ignored){}

        }
        else {
            FirebaseDatabase.getInstance().getReference("Admin/app").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        findViewById(R.id.progressbar).setVisibility(View.GONE);
                        long total = 0;
                        double sum_rate = 0;

                        try {
                            total = (long) dataSnapshot.child("TOTAL_RATES").getValue();
                            try {
                                sum_rate = (double) dataSnapshot.child("TOTAL_SUM").getValue();
                            } catch (Exception e) {
                                sum_rate = (long) dataSnapshot.child("TOTAL_SUM").getValue();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        double sum = sum_rate / total;
                        TextView textView = findViewById(R.id.avg_rate);
                        String s = String.format(Locale.UK, "%.1f", sum);
                        textView.setText(String.format(Locale.UK,
                                "%s",
                                s));
                        textView.setVisibility(View.VISIBLE);
                        textView = findViewById(R.id.total_rate);
                        textView.setText("Total " + total + " ratings");
                        RatingBar ratingBar = findViewById(R.id.ratingbar);
                        ratingBar.setVisibility(View.VISIBLE);
                        ratingBar.setRating((float) sum);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void backOnClicked(View view) {
        finish();
    }
}