package com.example.snjdeveloper.user;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.Constants;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.ArrayList;

public class DeleteUserActivity extends AppCompatActivity {
    private String uid;
    private ArrayList<String> list;
    private View progressbar;
    private TextView progressTitle;
    private String name = "";
    private String mobile = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_del_user);
        receive();
        TextView textView = findViewById(R.id.title);
        progressTitle = findViewById(R.id.progressTitle);
        textView.setText("Edit user");
        list = new ArrayList<>();
        progressbar = findViewById(R.id.progressbar_dialog);
        initializeProgress();
    }

    private void initializeProgress() {
        findViewById(R.id.background_Bt).setOnClickListener(v -> progressbar.setVisibility(View.GONE));
    }

    private void receive() {
        Intent intent = getIntent();
        uid = intent.getStringExtra("UID");
        if (intent.getBooleanExtra("IS_BLOCKED", false))
            ((MaterialButton) findViewById(R.id.blockUserBt)).setText("Unblock user");
        if (uid == null) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show();
            endActivity();
            return;
        }
        name = intent.getStringExtra("NAME");
        mobile = intent.getStringExtra("MOBILE");
        long wallet = intent.getLongExtra("WALLET", 0);
        setData(name, mobile, wallet);
    }

    private void setData(String name, String mobile, long wallet) {
        TextView nameTextView = findViewById(R.id.name);
        TextView mobileTextView = findViewById(R.id.mobilenumber);
        TextView walletTextView = findViewById(R.id.wallet);
        nameTextView.setText(name);
        mobileTextView.setText(mobile);
        Constants.loadWallet(this, wallet, walletTextView);
        setUserImage(name);
    }

    private void setUserImage(String name) {
        if (name != null) {
            if (!name.isEmpty()) {
                String[] d = name.split(" ");
                if (d.length > 1)
                    name = d[0].substring(0, 1) + d[1].substring(0, 1);
                else name = name.substring(0, 1);
            }
            TextView textView = findViewById(R.id.username);
            textView.setText(name.toUpperCase());
        }
        Constants.setBackgroundColor(findViewById(R.id.nameLayout), this);
        Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F" +
                uid + "_pr" + "?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                .into((ImageView) findViewById(R.id.userimage));
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }

    public void blockUserClicked(View view) {
        String msg = "Block";
        if (((MaterialButton) view).getText().toString().contains("Unblock user"))
            msg = "Unblock";
        String finalMsg = msg;
        new AlertDialog.Builder(DeleteUserActivity.this)
                .setMessage("Are you sure you want to " + msg.toLowerCase() + " the user\n")
                .setTitle("Confirmation")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(msg, (dialog, which) -> {
                    showProgress(finalMsg + "ing " + name);
                    if (finalMsg.equals("Block"))
                        blockUser();
                    else unblockUser();
                })
                .show();

    }

    private void showProgress(String title) {
        progressTitle.setText(title);
        progressbar.setVisibility(View.VISIBLE);
    }

    private void unblockUser() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", uid);
            new Constants().makeHttpCall(DeleteUserActivity.this, "user_config-enableUser", jsonObject, "enableUser", progressbar, uid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (!isFinishing())
            if (progressbar.getVisibility() != View.VISIBLE)
                super.onBackPressed();
    }

    void blockUser() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", uid);
            new Constants().makeHttpCall(DeleteUserActivity.this, "user_config-disableUser", jsonObject, "disableUser", progressbar, uid);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void endActivity() {
        if (!isFinishing()) {
            Intent intent = new Intent(this, HomeAdmin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void delUserClicked(View view) {
        String[] options = {"Personal information", "Orders", "Transactions", "Returned bottles"};
        String[] optionsDbName = {"Customers", "Orders", "Transactions", "Bottles"};
        createSelectedCheckBoxDialog(options, optionsDbName);
    }

    private void createSelectedCheckBoxDialog(String[] options, String[] optionsDbName) {
        list.clear();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select the records to delete");
        builder.setMultiChoiceItems(options, null, (dialog, which, isChecked) -> {
            if (isChecked)
                list.add(optionsDbName[which]);
            else list.remove(optionsDbName[which]);
        }).
                setPositiveButton("Delete", (dialog, which) -> {
                    if (list.isEmpty()) {
                        Toast.makeText(DeleteUserActivity.this, "Select at least one option", Toast.LENGTH_LONG).show();
                        return;
                    }
                    new AlertDialog.Builder(this)
                            .setMessage(setConfirmMessage(options, optionsDbName, list))
                            .setTitle("Confirmation")
                            .setPositiveButton("Delete", (dialog1, which1) -> {
                                Log.e("DeleteRecord", "List is " + list + " calling async");
                                new DeleteRecords(list, uid, progressbar).execute();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null).show();

    }

    private String setConfirmMessage(String[] options, String[] optionsDbName, ArrayList<String> list) {
        StringBuilder listName = new StringBuilder();
        String message;
        for (String name : list) {
            for (int i = 0; i < optionsDbName.length; i++) {
                if (name.equals(optionsDbName[i]))
                    if (listName.length() == 0)
                        listName = new StringBuilder(options[i]);
                    else
                        listName.append(", ").append(options[i]);
            }

        }
        String finalList = listName.toString();
        if (list.size() == 2)
            finalList = finalList.replace(",", " and").toLowerCase();
        message = "Are you sure you want to delete\n" + finalList
                + " records of " + name.toLowerCase();
        return message;
    }

    class DeleteRecords extends AsyncTask<String, Integer, Integer> {
        private final ArrayList<String> list;
        private final String uid;

        private View progressBar;

        public DeleteRecords(ArrayList<String> list, String uid, View progressBar) {
            this.list = list;
            this.uid = uid;
            this.progressBar = progressBar;
            Log.e("Async", "Constructor");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("Async", "onPreExecute");
            showProgress("Deleting " + list.toString().replace("[", "").replace("]", "").toLowerCase());
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Log.e("ListToDelete", "->" + list);
            if (list.contains("Customers")) {
                DatabaseReference databaseReference;
                if (getIntent().getBooleanExtra("IS_BLOCKED", false))
                    databaseReference = new TransactionDb().getDatabaseReference(DeleteUserActivity.this).child("Blocked");
                else
                    databaseReference = // undo-here
                            FirebaseDatabase.getInstance().getReference("Customers");
//                         new TransactionDb().getDatabaseReference(DeleteUserActivity.this,"test1-b0b2c").child("Customers");
                databaseReference.child(uid).setValue(null).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {   //TODO api call here
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("uid", uid);
                            Constants.makeHttpCall(DeleteUserActivity.this, "user_config-deleteUser", jsonObject, progressBar);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                        endActivity();
                    }
                });
            }
            if (list.contains("Orders"))
                delFromQuery(
                        //undo-here
                        FirebaseDatabase.getInstance().getReference("Orders")
                        // new TransactionDb().getDatabaseReference(DeleteUserActivity.this,"test1-b0b2c").child("Orders")
                        , "orders");
            if (list.contains("Transactions"))
                delFromQuery(new TransactionDb().getDatabaseReference(DeleteUserActivity.this).child("Transactions"), "Transactions");
            if (list.contains("Bottles")) {
                FirebaseFirestore.getInstance(new TransactionDb().loadFirebase(DeleteUserActivity.this)).collection("Bottles")
                        .whereEqualTo("UID", uid).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Log.e("del doc", "->" + document.getData());
                            document.getReference().delete();
                        }
                        String msg = "Deleted " + queryDocumentSnapshots.size() + " returned bottles" + "\n" + name + "\n" + mobile;
                        Constants.storeStatusNotification(msg, 1, DeleteUserActivity.this);
                        Toast.makeText(DeleteUserActivity.this, queryDocumentSnapshots.size() + " returned bottles record deleted", Toast.LENGTH_LONG).show();
                    } else {
                        if (progressBar.getVisibility() != View.VISIBLE) {
                            String msg = "Failed to delete returned bottles" + "\n" + name + "\n" + mobile + "\nError: No bottles" + " found";
                            Constants.storeStatusNotification(msg, 0, DeleteUserActivity.this);
                        } else
                            Toast.makeText(DeleteUserActivity.this, "No returned bottles found", Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    endActivity();
                }).addOnFailureListener(e -> {
                    Toast.makeText(DeleteUserActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }).addOnCanceledListener(() -> Toast.makeText(DeleteUserActivity.this, "Error occurred", Toast.LENGTH_LONG).show());
            }
            return null;
        }

        private void delFromQuery(DatabaseReference databaseReference, String from) {
            databaseReference.orderByChild("UID").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Log.e("Item Deleted", "->" + dataSnapshot.getKey());
                            dataSnapshot.getRef().setValue(null);
                        }
                        String msg = "Deleted " + snapshot.getChildrenCount() + " " + from + "\n" + name + "\n" + mobile;
                        Constants.storeStatusNotification(msg, 1, DeleteUserActivity.this);
                    } else {
                        if (progressBar.getVisibility() != View.VISIBLE) {
                            String msg = "Failed to delete " + from + "\n" + name + "\n" + mobile + "\nError: No " + from + " found";
                            Constants.storeStatusNotification(msg, 0, DeleteUserActivity.this);
                        }
                        Log.e("DelRecord", "No record");
                    }
                    progressBar.setVisibility(View.GONE);
                    endActivity();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Log.e("Async", "onPostExecute");
        }
    }
}
