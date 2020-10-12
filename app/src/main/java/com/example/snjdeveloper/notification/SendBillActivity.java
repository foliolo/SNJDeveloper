package com.example.snjdeveloper.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.RecyclerUI.UserListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SendBillActivity extends AppCompatActivity {
    private boolean autoAlert = false;
    private DatabaseReference dbRef;
    private ArrayList<String> registrationIDs;
    private ArrayList<String> displayList;
    RecyclerView recyclerView;
    private ProgressBar progressBar;
    HashMap<String, Boolean> adminNotificationCred;
    private View settingsFab;
    View progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_bill_notification);
        registrationIDs = new ArrayList<>();
        displayList = new ArrayList<>();
        progressDialog = findViewById(R.id.progressbar_dialog);
        adminNotificationCred = new HashMap<>();
        progressBar = findViewById(R.id.progressbar);
        settingsFab = findViewById(R.id.settingsFab);
        dbRef = FirebaseDatabase.getInstance().getReference("Admin/Notification-Settings").child("SEND_MONTHLY_PAYMENT");
        checkAutoBillStatus();
        recyclerView = findViewById(R.id.recycler_view);
    }

    public void initializeFragment() {
        settingsFab.setVisibility(View.VISIBLE);
        settingsFab.setOnClickListener(v -> {
            MoreOptionFragment fragment = new MoreOptionFragment(registrationIDs, displayList, adminNotificationCred);
            fragment.show(getSupportFragmentManager(), "BillReport");
        });

    }

    public void backOnClicked(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (progressDialog.getVisibility() != View.VISIBLE)
            super.onBackPressed();
    }

    private void checkAutoBillStatus() {
        FirebaseDatabase.getInstance().getReference("Admin/Notification-Settings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    adminNotificationCred = (HashMap<String, Boolean>) snapshot.getValue();
                    if (snapshot.child("SEND_MONTHLY_PAYMENT").exists())
                        autoAlert = (boolean) snapshot.child("SEND_MONTHLY_PAYMENT").getValue();
                    if (autoAlert)
                        showEnableScreen();
                    else showDisableScreen();
                } else showDisableScreen();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showDisableScreen() {
        findViewById(R.id.disableScreen).setVisibility(View.VISIBLE);
        SwitchCompat autoSwitch = findViewById(R.id.switchWidget2);
        autoSwitch.setChecked(false);
        autoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            findViewById(R.id.disableScreen).setVisibility(View.GONE);
            dbRef.setValue(true);
            autoSwitch.setOnCheckedChangeListener(null);
            showEnableScreen();
        });
    }

    private void showEnableScreen() {
        initializeFragment();
        if (registrationIDs.isEmpty())
            loadNotAlertUsers();
        else loadRecyclerview();

        findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
        SwitchCompat autoSwitch = findViewById(R.id.topSwitch);
        autoSwitch.setChecked(true);
        autoSwitch.setVisibility(View.VISIBLE);
        autoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            findViewById(R.id.scroll_view).setVisibility(View.GONE);
            dbRef.setValue(false);
            autoSwitch.setVisibility(View.GONE);
            autoSwitch.setOnCheckedChangeListener(null);
            settingsFab.setVisibility(View.GONE);
            showDisableScreen();
        });

    }

    private void loadRecyclerview() {
        UserListAdapter listAdapter = new UserListAdapter(this, registrationIDs, displayList, findViewById(R.id.desc1), findViewById(R.id.desc2));
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listAdapter);
    }

    private void loadNotAlertUsers() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("Customers").orderByChild("RECEIVE_MONTHLY_PAYMENT").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                progressBar.setVisibility(View.GONE);
                if (snap.hasChildren()) {
                    for (DataSnapshot snapshot : snap.getChildren())
                        if (snapshot.child("RECEIVE_MONTHLY_PAYMENT").exists() &&
                                !(boolean) snapshot.child("RECEIVE_MONTHLY_PAYMENT").getValue()) {
                            {
                                registrationIDs.add(snapshot.getKey());
                                String storeData = snapshot.getKey() + "," + snapshot.child("MOBILE").getValue() + "," + snapshot.child("NAME").getValue() + "," + snapshot.child("MOBILE").getValue();
                                displayList.add(storeData);
                            }
                        }
                }
                if (!registrationIDs.isEmpty()) {
//                    showMessageTextView();
                    loadRecyclerview();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
