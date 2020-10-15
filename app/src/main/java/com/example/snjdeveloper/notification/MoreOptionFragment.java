package com.example.snjdeveloper.notification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.Constants;
import com.example.snjdeveloper.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MoreOptionFragment extends BottomSheetDialogFragment {
    private View newCustomerFab;
    private View sendBillFab;
    private static ArrayList<String> registrationIDs;
    private ArrayList<String> displayList;
    private UserDataSelector fragment;
    private RecyclerView recyclerView;
    private AppCompatCheckBox billNotify;
    private AppCompatCheckBox billEmail;
    private AppCompatCheckBox billInApp;
    private static DatabaseReference dbRef;
    private DatabaseReference adminNotificationRef;
    private static ArrayList<String> oldList;
    HashMap<String, Boolean> adminNotificationCred;

    MoreOptionFragment(ArrayList<String> registrationIDs, ArrayList<String> displayList, HashMap<String, Boolean> adminNotificationCred) {
        MoreOptionFragment.registrationIDs = registrationIDs;
        this.displayList = displayList;
        oldList = new ArrayList<>();
        oldList.addAll(registrationIDs);
        this.adminNotificationCred = adminNotificationCred;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.more_option_screen_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        oldList = new ArrayList<>();
        dbRef = FirebaseDatabase.getInstance().getReference("Customers");
        adminNotificationRef = FirebaseDatabase.getInstance().getReference("Admin/Notification-Settings");
        initialize();
        setListeners();
    }

    private void initialize() {
//        mainFab=getView().findViewById(R.id.fabOptions);
        newCustomerFab = getView().findViewById(R.id.fabnewCustomer);
        sendBillFab = getView().findViewById(R.id.fabSendAlert);
        billEmail = getView().findViewById(R.id.sendEmail);
        billNotify = getView().findViewById(R.id.sendNotification);
        billInApp = getView().findViewById(R.id.sendBillInApp);
        if (adminNotificationCred != null) {
            if (adminNotificationCred.containsKey("SEND_BILL_EMAIL"))
                billEmail.setChecked(adminNotificationCred.get("SEND_BILL_EMAIL"));
            if (adminNotificationCred.containsKey("SEND_BILL_NOTIFICATION"))
                billNotify.setChecked(adminNotificationCred.get("SEND_BILL_NOTIFICATION"));
            if (adminNotificationCred.containsKey("SEND_BILL_IN_APP"))
                billInApp.setChecked((boolean)adminNotificationCred.get("SEND_BILL_IN_APP"));
            else billInApp.setChecked(true);
        }
    }

    void initializeCustomerList() {
        if (recyclerView == null)
            recyclerView = getActivity().findViewById(R.id.recycler_view);
        if (fragment == null)
            fragment =
                    new UserDataSelector("UID", registrationIDs, displayList, recyclerView);
        fragment.show(getActivity().getSupportFragmentManager(), "BillReport");
    }

    private void setListeners() {
        newCustomerFab.setOnClickListener(v -> {
            dismiss();
            initializeCustomerList();
        });
        View progressbar = getActivity().findViewById(R.id.progressbar_dialog);
        ((TextView) getActivity().findViewById(R.id.progressTitle)).setText("Sending bill");
        getActivity().findViewById(R.id.background_Bt).setOnClickListener(v -> {
            progressbar.animate().setDuration(500).alpha(0).withEndAction(() -> progressbar.setVisibility(View.GONE));

        });
        sendBillFab.setOnClickListener(v -> {
            dismiss();
            Context context = getActivity();
            new AlertDialog.Builder(context)
                    .setTitle("Send bill")
                    .setMessage("Are you sure you want to send bill to all customers")
                    .setPositiveButton("OK", (dialog, which) -> {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("uid", "");
                            Constants.makeHttpCall(context, "bill_report-sendBill", jsonObject, progressbar);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).setNegativeButton("Cancel", null)
                    .create()
                    .show();
        });
        billInApp.setOnCheckedChangeListener((buttonView, isChecked) -> storeChanges("SEND_BILL_IN_APP", isChecked));
        billEmail.setOnCheckedChangeListener((buttonView, isChecked) -> storeChanges("SEND_BILL_EMAIL", isChecked));
        billNotify.setOnCheckedChangeListener((buttonView, isChecked) -> storeChanges("SEND_BILL_NOTIFICATION", isChecked));
    }

    void storeChanges(String fieldName, boolean state) {
        adminNotificationCred.put(fieldName, state);
        adminNotificationRef.child(fieldName).setValue(state);
    }

    static class Action extends ActionListener {
        @Override
        public void actonEndListener() {
            super.actonEndListener();
            Log.e("Action", "Dismissed listener invoked");
            for (String uid : registrationIDs) {
                oldList.remove(uid);
                dbRef.child(uid).child("RECEIVE_MONTHLY_PAYMENT").setValue(false);
            }
            for (String uid : oldList)
                dbRef.child(uid).child("RECEIVE_MONTHLY_PAYMENT").setValue(null);
            oldList.clear();
            oldList.addAll(registrationIDs);
        }
    }
}
