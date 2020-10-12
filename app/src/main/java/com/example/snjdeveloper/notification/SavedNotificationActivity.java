package com.example.snjdeveloper.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.RecyclerUI.UserListAdapter;
import com.example.snjdeveloper.TransactionDb;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class SavedNotificationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<HashMap<String, Object>> data;
    private ProgressBar progressBar;
    private UserListAdapter adapter;
    private View alertScreen;
    private Query fireQuery;
    private boolean isDBReachedLast;
    private boolean isDbBusyLoading;
    private DocumentSnapshot queryDocumentSnap;
    private View loaderProgressbar;
    private String nodeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notification);
        nodeName=getIntent().getStringExtra("NodeName");
        if (nodeName==null)
            nodeName="Saved Notification";
        TextView textView=findViewById(R.id.title);
        if (getIntent().getStringExtra("Title")!=null)
            textView.setText(getIntent().getStringExtra("Title"));
        alertScreen = findViewById(R.id.info_screen);
        SwipeRefreshLayout layout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerview);
        data = new ArrayList<>();
        progressBar = findViewById(R.id.progressbar);
        loaderProgressbar = findViewById(R.id.progressbar_loader);
        layout.setOnRefreshListener(() -> {
            if (adapter != null) {
                data.clear();
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                layout.setRefreshing(false);
                fireQuery = FirebaseFirestore.getInstance(new TransactionDb().loadFirebase(this))
                        .collection(nodeName).orderBy("time", Query.Direction.DESCENDING);

                firebaseData(true);
            }
        });
        initializeRecyclerView();
        fireQuery = FirebaseFirestore.getInstance(new TransactionDb().loadFirebase(this))
                .collection(nodeName).orderBy("time", Query.Direction.DESCENDING);
        firebaseData(true);
    }

    @Override
    public void onBackPressed() {
        if (alertScreen.getVisibility() != View.VISIBLE)
            super.onBackPressed();
        else
            alertScreen.animate().setDuration(500).alpha(0).withEndAction(() -> {
                alertScreen.setVisibility(View.GONE);
                alertScreen.setAlpha(1);
            });
    }

    void firebaseData(boolean start) {
        Log.e("Query", "requesting...");
        isDbBusyLoading = true;
        if (fireQuery != null && !start)
            fireQuery = fireQuery
                    .startAfter(queryDocumentSnap).limit(6);
        else fireQuery = Objects.requireNonNull(fireQuery).limit(8);
        fireQuery.get().addOnCompleteListener(task -> {
            try {
                loaderProgressbar.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.GONE);
                if (!task.isSuccessful()) {
                    Toast.makeText(SavedNotificationActivity.this, "Error occurred: " + task.getException().getMessage() + "\nSwipe to retry", Toast.LENGTH_LONG).show();
                } else {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    if (queryDocumentSnapshots.isEmpty()) {
                        isDBReachedLast = true;
                        Log.e("Result", "No Query Results");
                        if (data.size() == 0) {
                        }
                    } else {
                        for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                            try {
                                HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getData();
                                if (map != null) {
                                    Date date = snapshot.getTimestamp("time").toDate();
                                    map.put("time", date);
                                    map.put("key",snapshot.getId());
                                    data.add(map);
                                    queryDocumentSnap = snapshot;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(SavedNotificationActivity.this, "Error occurred: " + e.getMessage() + "\nSwipe to retry", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    isDbBusyLoading = false;
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.getMessage();
                isDbBusyLoading = false;
            }
        })
                .addOnFailureListener(e -> Log.e("Result", "Failed: No Query Results " + e.getMessage())).addOnCanceledListener(() -> Log.e("Result", "Canceled: No Query Results "));


    }


    void initializeRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new UserListAdapter(SavedNotificationActivity.this, data);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE &&
                        !isDBReachedLast && !isDbBusyLoading) {
                    Log.e("Scroll", "Detected->" + newState);
                    loaderProgressbar.setVisibility(View.VISIBLE);
                    firebaseData(false);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
    }

    public void showHelpClicked(View view) {
        alertScreen.setVisibility(View.VISIBLE);
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }
}
