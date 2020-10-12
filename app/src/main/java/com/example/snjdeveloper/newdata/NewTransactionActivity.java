package com.example.snjdeveloper.newdata;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.snjdeveloper.LoadRecentUsersFragment;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.example.snjdeveloper.qrcodegenerator.QRCodeGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public  class NewTransactionActivity extends AppCompatActivity {
    private String mobile;
    private String uid;
    private AppCompatEditText amount_received;
    private String mode="";
    private long wallet;
    private Button saveBt;
    private boolean isDataLoaded=false;
    private long bottle;
    private TextView userNameText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction);
        TextView textView=findViewById(R.id.title);
        textView.setText("New Transaction");
        amount_received=findViewById(R.id.paid_amount);
        userNameText = findViewById(R.id.username);
        loadMode();
        loadData();
        loadName();
        loadListner();
    }
    private void loadName() {
        try {
            Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/" +
                    "files%2F" + uid + "_pr?alt=media&token=" + "479a3ebb-6ef0-4ef8-b111-3e30fa2efb")
                    .apply(RequestOptions.circleCropTransform()).listener(new RequestListener<Drawable>() {

                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    userNameText.setVisibility(View.GONE);
                    return false;
                }
            }).into((ImageView) findViewById(R.id.userimage));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void loadMode() {
        RadioGroup radioGroup = findViewById(R.id.radiogroup);
        String[] modeofpayments = {"Cash", "Paytm", "PhonePay", "GooglePay", "AmazonPay", "Other"};
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            try {
                View radiob = radioGroup1.findViewById(i);
                mode = modeofpayments[radioGroup1.indexOfChild(radiob)];
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadListner() {
        saveBt = findViewById(R.id.saveBt);
        saveBt.setText("Save Transaction");
        saveBt.setOnClickListener(view -> {
            if (isDataLoaded) {
                if (isNetworkAvailable(this)) {
                    AppCompatEditText returnedit = findViewById(R.id.quantity_return);
                    String returnbottle = Objects.requireNonNull(returnedit.getText()).toString().trim();
                    if (mobile == null)
                        showAlertDialog(false, "Mobile not found for this user.", false);
                    else if (Objects.requireNonNull(amount_received.getText()).toString().isEmpty() ||
                            amount_received.getText().toString().replace("0", "").trim().isEmpty())
                        showAlertDialog(false, "Amount paid by user is not found for this transaction.\nPlease enter amount.", false);
                    else if (amount_received.getText().toString().trim().equals("0"))
                        showAlertDialog(false, "Amount paid by the user should be greater then 0", false);
                    else if (amount_received.getText().toString().trim().startsWith("0"))
                        showAlertDialog(false, "Invalid amount\nPlease do not use extra 0 at the start of the amount", false);
                    else if (mode.isEmpty())
                        showAlertDialog(false, "Please click on the mode of the payment done by the user.", false);
                    else if (!returnbottle.isEmpty() && !returnbottle.equals("0") ||
                            !returnbottle.replace("0", "").trim().isEmpty()) {
                        if (returnbottle.startsWith("0")) {
                            showAlertDialog(false, "Do not use extra 0 with return bottle quantity", false);
                            return;
                        }
                        long bot = Long.parseLong(returnbottle);
                        if (bottle >= bot) {
                            showAlertDialog(true, "Save this new transaction for the mobile number " + mobile + ".\nBefore saving please make sure amount received is carefully entered as once changed cannot be modified later."
                                    , true);
                        } else {
                            Log.e("Bottle", bottle + " bot " + bot);
                            showAlertDialog(false, "The returned bottles cannot be more then pending bottles"
                                    , false);
                        }
                    } else
                        showAlertDialog(true, "Save this new transaction for the mobile number " + mobile + ".\nBefore saving please make sure amount received is carefully entered as once saved cannot be modified later.", false);
                } else
                    showAlertDialog(false, "No internet connectivity available.\nPlease connect to a stable network and try again.", false);
            }else showAlertDialog(false, "Data is not loaded from the server.\nPlease wait to load the data or connect to a more stable internet connection.", false);
            });

    }
    private void showAlertDialog(boolean forSave, String msg, boolean isBottleReturn) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New transaction ");
        alert.setMessage(msg);
        if (!forSave){
            alert.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            });
        }else{
            alert.setPositiveButton("Save", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                saveData(isBottleReturn);
            });
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });
        }
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
    public static boolean isNetworkAvailable(Context con){
        try{
            ConnectivityManager cm=(ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=cm.getActiveNetworkInfo();
            if (networkInfo!=null&& networkInfo.isConnected()){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    private void saveData(boolean isBottleReturn) {


        findViewById(R.id.progressBt).setVisibility(View.VISIBLE);
        saveBt.setEnabled(false);
        saveBt.setText("");
        long paid = Long.parseLong(amount_received.getText().toString().replace("₹", ""));
        long finalTotal = paid;
        String note;
        HashMap<String, Object> data = new HashMap<>();
        long time = System.currentTimeMillis();
//        data.put("TIME",time);
        String id = String.valueOf(time);
        data.put("DATE", id);
        note = getWalletStatus(finalTotal);
        data.put("UID", uid);
        data.put("KEY", uid + id);
        data.put("PAID_AMOUNT", paid);
        data.put("PAID_VIA", mode);
        data.put("NOTE", note);
        AppCompatEditText returnEdit = findViewById(R.id.quantity_return);
        new TransactionDb().getReference(NewTransactionActivity.this)
                .child(id).
                setValue(data).addOnSuccessListener(aVoid -> {
            FirebaseDatabase.getInstance().getReference("Transactions").child(id).
                    setValue(data);
            FirebaseDatabase.getInstance().getReference("Customers/" + uid)
                    .child("WALLET").setValue(wallet);
            new QRCodeGenerator("Transaction," + id, id + "_tr", NewTransactionActivity.this);
            if (isBottleReturn) {
                try {
                    new NewReturnBottlesActivity().saveData("Bottles returned with transaction number " + id,
                            uid,
                            bottle,
                            Long.parseLong(Objects.requireNonNull(returnEdit.getText()).toString().trim()),
                            true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            Intent intent=new Intent(this, HomeAdmin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            findViewById(R.id.progressBt).setVisibility(View.GONE);
            showAlertDialog(false,"Error in storing the transaction.\nPlease check your internet connection and try again after some time", false);
            saveBt.setText("Failed to save. Try Again");
            saveBt.setEnabled(true);
        });
    }
    private String getWalletStatus(long finalTotal) {
        String note = "";
        String oldWalletStatus;
        if (wallet < 0)
            oldWalletStatus = "\nOld Balance Pending ₹ " + Math.abs(wallet);
        else
            oldWalletStatus = "\nOld Balance ₹ " + Math.abs(wallet);

        String newWalletStatus = "\nNew Balance ₹ ";

        if (wallet == 0) {
            if (finalTotal > 0) {//extra paid
                wallet += finalTotal;
                note = "Extra" + "₹ " + Math.abs(finalTotal) + " added into the wallet";

            } else if (finalTotal < 0) {//less paid
                wallet += finalTotal;
                note = "Pending " + "₹ " + Math.abs(finalTotal) + " added into the wallet";
            }//else note= "Transaction done for order no "+orderno;
            note += oldWalletStatus + newWalletStatus + wallet;
        }
        else if(wallet>0)  //wallet is not zero have some extra balance
        {
            if (finalTotal > 0) {//extra paid
                note = "Extra " + "₹" + Math.abs(finalTotal) + " added into the wallet.\nOld Balance "
                        + "₹ " + wallet + "\nNew Balance " + "₹ " + (wallet + finalTotal);
                wallet += finalTotal;
            } else if (finalTotal < 0) {//less paid
                if (wallet + finalTotal > 0) {
                    wallet += finalTotal;
                    note = "Used " + "₹ " + Math.abs(finalTotal) + " from the wallet." + oldWalletStatus + "\nNew Balance " + "₹ " + wallet;
                } else {
                    note = "Left ₹ " + Math.abs(finalTotal) + " is paid from wallet balance ₹ " + Math.abs(wallet);
                    wallet += finalTotal;
                    if (wallet < 0)
                        note = note + " ,Left  " + "₹ " + Math.abs(wallet) + " is into the wallet \nNew Balance Pending " + "₹" + Math.abs(wallet);
                    else note = note + oldWalletStatus + "\nNew Balance " + "₹ " + wallet;
                }
            }//else note= "Transaction done for order no "+orderno;
        }
        else //less paid wallet<0
        {//less paid
            if(finalTotal>0){
                if (wallet + finalTotal > 0) {

                    note = "" + "₹ " + Math.abs(finalTotal) + " is added into the wallet \nOld Balance pending "
                            + "₹ " + Math.abs(wallet);
                    wallet += finalTotal;
                    note += "\nNew Balance " + "₹ " + wallet;

                } else if (wallet + finalTotal < 0) {
                    wallet += finalTotal;
                    note = "" + "₹ " + Math.abs(finalTotal) + " is used to complete pending payment" + oldWalletStatus +
                            "\nNew Balance Pending " + "₹" + (wallet * -1);
                } else {
                    note = "" + "₹ " + Math.abs(finalTotal) + " is used to complete pending payment.\nOld Balance " +
                            "pending ₹" + (wallet * -1) + "\nNew Balance " + "₹ 0";
                    wallet += finalTotal;
                }
            } else if (finalTotal < 0) {
                note = "Left  " + "₹ " + Math.abs(finalTotal) + " is added into the wallet. " +
                        "\nOld Wallet Balance Pending " + "₹ " + (wallet * -1) +
                        " \nNew Balance Pending " + "₹ " + ((wallet + finalTotal) * -1);
                wallet += finalTotal;
            }//else note= "Transaction done for order no "+orderno;
        }


        return  note;
    }



    private void loadData() {
        TextView textView=findViewById(R.id.mobilenumber);
        mobile=getIntent().getStringExtra("MOBILE");
        textView.setText(mobile);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        textView=findViewById(R.id.date);
        textView.setText(dateFormat.format(calendar.getTime()));
        uid=getIntent().getStringExtra("UID") ;
        FirebaseDatabase.getInstance().getReference("Customers/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isDataLoaded=true;
                try {
                    String name = (String) dataSnapshot.child("NAME").getValue();
                    if (name != null) {
                        if (!name.isEmpty()) {
                            String[] d = name.split(" ");
                            if (d.length > 1)
                                name = d[0].substring(0, 1) + d[1].substring(0, 1);
                            else {
                                if (name.length() >= 2)
                                    name = name.substring(0, 2);
                                else
                                    name = name.substring(0, 1);
                            }
                        }
                        userNameText.setText(name.toUpperCase());
                        new LoadRecentUsersFragment().addNewRecent(mobile, uid, name.toUpperCase(), NewTransactionActivity.this);

                    }
                }catch (Exception e){e.printStackTrace();}

                try {
                    wallet=(long)dataSnapshot.child("WALLET").getValue();
                }catch (Exception e){wallet=0;}
                try {
                    bottle=(long)dataSnapshot.child("WATER_BOTTLE_PENDING").getValue();
                }catch (Exception e){bottle=0;}
                new NewReturnBottlesActivity().
                        loadBottle(findViewById(R.id.bottleProgress),findViewById(R.id.bottle),bottle);
                loadWallet();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadWallet() {
        findViewById(R.id.walletProgress).setVisibility(View.INVISIBLE);
        TextView textView=findViewById(R.id.wallet);
        String w=String.valueOf(wallet);

        if(wallet<0)
        {   w=w.replace("-","Pending ₹ ");
            textView.setTextColor(getResources().getColor(R.color.red));
        }
        else   {w="₹ "+w;
            textView.setTextColor(getResources().getColor(R.color.green_light));
        }

        textView.setText(String.format(Locale.UK," %s", w));
    }

    public void backOnClicked(View view) {
        finish();
    }
}
