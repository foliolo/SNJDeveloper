package com.example.snjdeveloper.messages;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.ViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class GeneralProblemActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<HashMap<String, Object>> data;
    private View progressBar;
    private Calendar calendar;
    private DateFormat dateFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notification);
        TextView textView=findViewById(R.id.title);
        textView.setText("App problems");
        SwipeRefreshLayout layout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerview);
        data = new ArrayList<>();
        progressBar = findViewById(R.id.progressbar);
        findViewById(R.id.info_icon).setVisibility(View.GONE);
        loadData();
        layout.setOnRefreshListener(() -> {
            layout.setRefreshing(false);
            data.clear();
            loadData();
        });
    }
    public void backOnClicked(View view) {
        onBackPressed();
    }

    private void loadData() {
        new TransactionDb().getDatabaseReference(this).child("Reports").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    HashMap<String,Object>map= ((HashMap<String, Object>) dataSnapshot.getValue());
                    map.put("key",dataSnapshot.getKey());
                    data.add(map);
                }
                Collections.reverse(data);
                initializeRecycler();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeRecycler() {
        dateFormat=new SimpleDateFormat("dd MMM yyyy", Locale.UK);
        calendar= Calendar.getInstance();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        Adapter adapter = new Adapter(this, data);
        recyclerView.setAdapter(adapter);
    }
    String getFormattedTime(long time){
        calendar.setTimeInMillis(time);
        return dateFormat.format(calendar.getTime());
    }
    class Adapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder>{


        private final Context context;
        private final ArrayList<HashMap<String, Object>> data;

        public Adapter(Context context, ArrayList<HashMap<String, Object>> data) {
        this.context=context;
        this.data=data;
        }

        @NonNull
        @Override
        public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new com.example.snjdeveloper.ViewHolder.FoodViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.notification_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder holder, int position) {
            HashMap<String, Object> map = data.get(position);
            holder.setName((String) map.get("MSG"));
            if (map.get("key")!=null) {
                String time = getFormattedTime(Long.parseLong((String) map.get("key")));
                holder.setTime(time);
            }
            Log.e("Adapter","Pos->"+position+"\n"+"->"+map);
            if (map.get("img")==null)
                holder.getAllDataAndImg((Boolean) map.get("HAVE_IMG"), context,map,(String) map.get("UID"),(String) map.get("key"),position);
            else holder.setData(context,(String) map.get("img"),(String) map.get("name"),(String)map.get("uid"),(String)map.get("mobile"));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
