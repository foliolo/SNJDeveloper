package com.example.snjdeveloper.newdata;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public  class NewOrderActivity extends AppCompatActivity {
    private String mobile;
    private String uid;
    private long rate;
    private String quaText = "";
    private AppCompatEditText amount_received;
    private String mode = "";
    private long wallet;
    private Button saveBt;
    private boolean isDataLoaded = false;
    private long bottle;
    private TextView userNameText;
    private RadioButton noPaymentRadioBt;
    private String amtReceived = "";
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);
        TextView textView = findViewById(R.id.title);
        textView.setText("New Order");
        amount_received = findViewById(R.id.paid_amount);
        userNameText = findViewById(R.id.username);
        noPaymentRadioBt = findViewById(R.id.noPayment);
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
        radioGroup = findViewById(R.id.radiogroup);
        String[] modeofpayments = {"No payment", "Cash", "Paytm", "PhonePay", "GooglePay", "AmazonPay", "Other"};

        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            try {
                RadioButton radiob = radioGroup1.findViewById(i);
                if (radiob != null && radiob.isChecked())
                    mode = modeofpayments[radioGroup1.indexOfChild(radiob)];
                else mode = "";
                if (!amtReceived.isEmpty() && !mode.isEmpty()) {
                    if (amtReceived.equals("0") || amtReceived.replace("0", "").trim().isEmpty()) {
                        if (!mode.equals(modeofpayments[0])) {
                            mode = modeofpayments[0];
                            radioGroup.check(R.id.noPayment);
                            showAlertDialog(false, "Invalid mode selected\nWhen amount received is 0 'no payment' mode should be selected.", false);
                        }
                    } else {
                        if (mode.equals(modeofpayments[0])) {
                            mode = "";
                            radioGroup.clearCheck();
                            showAlertDialog(false, "Invalid mode selected\nWhen amount received is greater than 0 'no payment' mode cannot be selected.", false);
                        }
                    }
                }
                Log.e("Mode", "->" + mode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    void showSnackbarMsg(String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.total_bottle_layout), msg, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setBackgroundTint(getResources().getColor(R.color.red));
        snackbar.show();
    }

    private void loadListner() {
        EditText rate_edit = findViewById(R.id.rate_edit);
        TextView textView1 = findViewById(R.id.rate_amount);
        findViewById(R.id.change).setOnClickListener(view -> {
            try {
                TextView textView = (TextView) view;
                if (textView.getText().equals("Change")) {
                    textView.setText("Done");
                    textView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    textView1.setText("Cancel");
                    textView.setTextColor(getResources().getColor(R.color.white));
                    rate_edit.setVisibility(View.VISIBLE);
                } else {
                    if (rate_edit.getText().toString().trim().isEmpty()) {
                        showSnackbarMsg("Enter rate");
                    } else if (rate_edit.getText().toString().trim().equals("0")) {
                        showSnackbarMsg("Rate should be greater than 0");
                    } else if (rate_edit.getText().toString().trim().startsWith("0")) {
                        showSnackbarMsg("Rate should not start with 0");
                    } else {
                        textView.setBackgroundColor(getResources().getColor(R.color.white));
                        textView.setTextColor(getResources().getColor(R.color.black));
                        rate = Long.parseLong(rate_edit.getText().toString().replace("₹", "").trim());
                        textView.setText("Change");
                        rate_edit.setText("");
                        quaText = "";
                        EditText editText = findViewById(R.id.total_quantity);
                        editText.setText("");
                        rate_edit.setVisibility(View.GONE);
                        textView1.setText(String.format(Locale.UK, "Rate ₹ %d", rate));
                        FirebaseDatabase.getInstance().
                                getReference("Customers/" + uid)
                                .child("RATE").
                                setValue(rate);
                    }

                }
            }catch (Exception e){e.printStackTrace();}
        });
        textView1.setOnClickListener(view -> {
            if(textView1.getText().toString().trim().equals("Cancel")){
                TextView textView=findViewById(R.id.change);

                textView.setBackgroundColor(getResources().getColor(R.color.white));
                textView.setTextColor(getResources().getColor(R.color.black));

                //rate = Long.parseLong(rate_edit.getText().toString().replace("₹","").trim());


                textView.setText("Change");
                rate_edit.setText("");
                rate_edit.setVisibility(View.GONE);

                textView1.setText(String.format(Locale.UK, "Rate ₹ %d", rate));

            }
        });
        saveBt = findViewById(R.id.saveBt);
        saveBt.setText("Save Order");
        saveBt.setOnClickListener(view -> {
            if (isDataLoaded) {
                if (isNetworkAvailable(this)) {
                    AppCompatEditText returnedit = findViewById(R.id.quantity_return);
                    String returnbottle = Objects.requireNonNull(returnedit.getText()).toString().trim();
                    boolean isBottleReturn = false;
                    if (rate == 0)
                        showAlertDialog(false, "Rate not found for this user.\nPlease Enter rate.", false);
                    else if (mobile == null)
                        showAlertDialog(false, "Mobile not found for this user.", false);
                    else if (quaText.isEmpty())
                        showAlertDialog(false, "Quantity is not found for this order.\nPlease enter quantity.", false);
                    else if (quaText.equals("0"))
                        showAlertDialog(false, "Invalid quantity\nQuantity should be greater then 0.", false);
                    else if (quaText.startsWith("0"))
                        showAlertDialog(false, "Invalid quantity\nplease do not include zero at the start of the quantity.", false);
                    else if (amount_received.getText().toString().replace("₹", "").trim().isEmpty())
                        showAlertDialog(false, "Amount paid by user is not found for this order.\nPlease enter amount.", false);
                    else if (amtReceived.trim().length() > 1 && amtReceived.trim().startsWith("0"))
                        showAlertDialog(false, "Invalid amount\nPlease do not use extra 0 at the start of the amount", false);
                    else if (mode.isEmpty())
                        showAlertDialog(false, "Please select the mode of the payment done by the user.", false);
                    else if (!returnbottle.isEmpty() && !returnbottle.equals("0")) {
                        if (returnbottle.startsWith("0")) {
                            showAlertDialog(false, "Do not use extra 0 with return bottle quantity", false);
                            return;
                        }
                        long bot = Long.parseLong(returnbottle);
                        if (bottle >= bot) {
                            isBottleReturn = true;
                            showAlertDialog(true, "Save this new order for the mobile number " + mobile + ".\nBefore saving please make sure amount received is carefully entered as once saved cannot be modified later.", isBottleReturn);
                        } else {
                            Log.e("Bottle", bottle + " bot " + bot);
                            showAlertDialog(false, "The returned bottles cannot be more then pending bottles", false);
                        }
                    } else {
                        showAlertDialog(true,
                                "Save this new order for the mobile number " + mobile +
                                        ".\nBefore saving please make sure amount received is" +
                                        " carefully entered as once saved cannot be modified later.",
                                false);
                    }
                } else
                    showAlertDialog(false, "No internet connectivity available.\nPlease connect to a stable network and try again.", false);
            }else showAlertDialog(false, "Data is not loaded from the server.\nPlease wait to load the data or connect to a more stable internet connection.", false);
        });
    }
    private void showAlertDialog(boolean forSave, String msg, boolean isBottleReturn) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New order ");
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
        AppCompatEditText editText = findViewById(R.id.total_amount);
        long total_amount = Long.parseLong(editText.getText().toString()
                .replace("₹ ", "").trim());
        long paid = Long.parseLong(amtReceived);
        if (mode.isEmpty()) {
            showAlertDialog(false, "Invalid mode selected", false);
            return;
        }
        if (mode.equals("No payment") && paid > 0) {
            showAlertDialog(false, "Invalid mode selected\nThe entered amount is not 0, 'No payment' mode should not be selected", false);
            radioGroup.clearCheck();
            mode = "";
            return;
        }
        long finalTotal = paid - total_amount;
        String note;
        HashMap<String, Object> data = new HashMap<>();
        long time = System.currentTimeMillis();
//        data.put("TIME",time);
        String id = String.valueOf(time);
        data.put("DATE", id);
        if (finalTotal == 0)
            note = "Transaction done for order no " + id;
        else note = getWalletStatus(finalTotal, id);

        data.put("UID", uid);
        data.put("AMOUNT", total_amount);
        data.put("KEY", uid + id);
        data.put("PAID_AMOUNT", paid);
        data.put("QUANTITY", Long.parseLong(quaText));
        data.put("PAID_VIA", mode);
        AppCompatEditText returnEdit = findViewById(R.id.quantity_return);
        new TransactionDb().getDatabaseReference(this)
                .child("Extra").child("LastConnection").
                setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        FirebaseDatabase.getInstance().getReference("Orders").
                child(id).
                setValue(data).addOnSuccessListener(aVoid -> {
            data.remove("QUANTITY");
            data.remove("AMOUNT");
            data.put("NOTE", note);
            if (!mode.equals("No payment")) {
                new TransactionDb().getReference(NewOrderActivity.this)
                        .child(id).
                        setValue(data);
                FirebaseDatabase.getInstance().getReference("Transactions")
                        .child(id).setValue(data);
                new QRCodeGenerator("Transaction," + id, id + "_tr", NewOrderActivity.this);
            } else Log.e("NewOrder", "No payment");
            FirebaseDatabase.getInstance().getReference("Customers/" + uid)
                    .child("WALLET").setValue(wallet);
            new QRCodeGenerator("Order," + id, id + "_or", NewOrderActivity.this);

            if (isBottleReturn) {
                try {
                    new NewReturnBottlesActivity().saveData("Bottles returned with order number " + id,
                            uid,
                            bottle + Long.parseLong(quaText),
                            Long.parseLong(Objects.requireNonNull(returnEdit.getText()).toString().trim()),
                            true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                FirebaseDatabase.getInstance().getReference("Customers/"+uid).child("WATER_BOTTLE_PENDING")
                        .setValue( (bottle+ Long.parseLong(quaText)));
            }
            Intent intent=new Intent(this, HomeAdmin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            findViewById(R.id.progressBt).setVisibility(View.GONE);
            showAlertDialog(false, "Error in storing the order.\nPlease check your internet connection and try again after some time", false);
            saveBt.setText("Failed to save. Try Again");
            saveBt.setEnabled(true);
        });


    }

    private String getOldWalletStatus(long finalTotal, String orderno) {
        String note = "";
        String subnote = " for the order number " + orderno;
        if (wallet == 0) {
            if (finalTotal > 0) {//extra paid
                wallet += finalTotal;
                note = "Extra " + "₹" + Math.abs(finalTotal) + " added to wallet," + subnote;
            } else if (finalTotal < 0) {//less paid
                wallet += finalTotal;
                note = "Pending " + "₹" + Math.abs(finalTotal) + " added to wallet," + subnote;
            } else note = "Transaction done for order no " + orderno;

        } else if (wallet > 0)  //wallet is not zero have some extra balance
        {
            if (finalTotal > 0) {//extra paid
                note = "Extra " + "₹" + Math.abs(finalTotal) + " added into the wallet Balance "
                        + "₹" + wallet + " New Balance " + "₹" + (wallet + finalTotal) + "," + subnote;
                wallet += finalTotal;
            } else if (finalTotal < 0) {//less paid
                if (wallet + finalTotal > 0) {
                    wallet += finalTotal;
                    note = "Used " + "₹" + Math.abs(finalTotal) + " from the wallet New Balance " + "₹" +
                            wallet + "," + subnote;
                } else {
                    note = "Left ₹ " + Math.abs(finalTotal) + " is paid from wallet balance ₹"
                            + Math.abs(wallet) + "," + subnote;
                    wallet += finalTotal;
                    if (wallet < 0)
                        note = note + " ,Left  " + "₹ " + Math.abs(wallet) +
                                " is added to the wallet New Balance Pending " + "₹" + Math.abs(wallet) + "," + subnote;
                    else note = note + ", wallet New Balance " + "₹" + wallet;
                }
            } else note = "Transaction done for order no " + orderno;
        } else //less paid wallet<0
        {//less paid
            if (finalTotal > 0) {
                if (wallet + finalTotal > 0) {

                    note = "Extra  " + "₹" + Math.abs(finalTotal) + " is used for the wallet pending "
                            + "₹" + wallet;
                    note += " New Balance " + "₹" + wallet + "," + subnote;

                } else if (wallet + finalTotal < 0) {
                    wallet += finalTotal;
                    note = "Extra  " + "₹" + Math.abs(finalTotal) + " is used to complete pending wallet" +
                            " New Balance Pending " + "₹" + (wallet * -1) + "," + subnote;
                } else {
                    note = "Extra  " + "₹" + Math.abs(finalTotal) + " is used to fulfill the pending " + "₹"
                            + (wallet * -1) + " New Balance " + "₹" + "0" + "," + subnote;
                    wallet += finalTotal;
                }
            } else if (finalTotal < 0) {
                note = "Left  " + "₹" + Math.abs(finalTotal) + " is  added to the pending Wallet Balance " + "₹" + (wallet * -1) +
                        " New Balance Pending " + "₹" + ((wallet + finalTotal) * -1) + "," + subnote;
                wallet += finalTotal;
            } else note = "Transaction done for order no " + orderno;
        }


        return note;
    }


    private String getWalletStatus(long finalTotal, String orderno) {
        String note = "";
        String subnote = "\nFor the order number " + orderno;
        String oldWalletStatus;
        if (wallet < 0)
            oldWalletStatus = "\nOld Balance Pending ₹ " + Math.abs(wallet);
        else
            oldWalletStatus = "\nOld Balance ₹ " + Math.abs(wallet);

        String newWalletStatus = "\nNew Balance ₹ ";
        if (wallet == 0) {
            if (finalTotal > 0) {//extra paid
                wallet += finalTotal;
                note = "Extra " + "₹ " + Math.abs(finalTotal) + " added to wallet." +
                        oldWalletStatus + newWalletStatus + wallet + subnote;
            } else if (finalTotal < 0) {//less paid
                wallet += finalTotal;
                note = "Pending " + "₹ " + Math.abs(finalTotal) + " added to wallet."
                        + oldWalletStatus + newWalletStatus + wallet + subnote;
            } else note = "Transaction done for order no " + orderno;

        } else if (wallet > 0)  //wallet is not zero have some extra balance
        {
            if (finalTotal > 0) {//extra paid
                note = "Extra " + "₹ " + Math.abs(finalTotal) + " added into the wallet. \nOld Balance "
                        + "₹ " + wallet + "\nNew Balance " + "₹ " + (wallet + finalTotal) + "" + subnote;
                wallet += finalTotal;
            } else if (finalTotal < 0) {//less paid
                if (wallet + finalTotal > 0) {
                    wallet += finalTotal;
                    note = "Used " + "₹ " + Math.abs(finalTotal) + " from the wallet." + oldWalletStatus + newWalletStatus + wallet + subnote;
                } else {
                    note = "Left ₹ " + Math.abs(finalTotal) + " is paid from wallet balance ₹ "
                            + Math.abs(wallet);
                    wallet += finalTotal;
                    if (wallet < 0)
                        note = note + " ,Left  " + "₹ " + Math.abs(wallet) +
                                " is added to the wallet." + oldWalletStatus + "\nNew Balance Pending " + "₹ " + Math.abs(wallet) + "" + subnote;
                    else note = note + oldWalletStatus + "\nNew Balance " + "₹ " + wallet;
                    note += "" + subnote;
                }
            } else note = "Transaction done for order no " + orderno;
        } else //less paid wallet<0
        {//more paid
            if (finalTotal > 0) {
                if (wallet + finalTotal > 0) {

                    note = "Extra " + "₹ " + Math.abs(finalTotal) + " is added in the wallet.\nOld Balance" +
                            " pending "
                            + "₹ " + Math.abs(wallet);
                    wallet += finalTotal;
                    note += "\nNew Balance " + "₹ " + wallet + "" + subnote;

                } else if (wallet + finalTotal < 0) {
                    wallet += finalTotal;
                    note = "Extra  " + "₹" + Math.abs(finalTotal) + " is used to complete pending payment" +
                            oldWalletStatus +
                            "\nNew Balance Pending " + "₹ " + (wallet * -1) + "" + subnote;
                } else {
                    note = "Extra  " + "₹" + Math.abs(finalTotal) + " is used to fulfill the pending " + "₹"
                            + (wallet * -1) + " New Balance " + "₹ " + "0" + "" + subnote;
                    wallet += finalTotal;
                }
            } else if (finalTotal < 0) {
                note = "Left " + "₹ " + Math.abs(finalTotal) + " is added to the Wallet" + oldWalletStatus +
                        "\nNew Balance Pending " + "₹ " + ((wallet + finalTotal) * -1) + "" + subnote;
                wallet += finalTotal;
            } else note = "Transaction done for order no " + orderno;
        }

        return note;
    }


    private void loadData() {
        TextView textView = findViewById(R.id.mobilenumber);
        mobile = getIntent().getStringExtra("MOBILE");
        textView.setText(mobile);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        textView = findViewById(R.id.date);
        textView.setText(dateFormat.format(calendar.getTime()));
        uid = getIntent().getStringExtra("UID");
        AppCompatEditText quant = findViewById(R.id.total_quantity);
        AppCompatEditText totalAmount = findViewById(R.id.total_amount);
        FirebaseDatabase.getInstance().getReference("Customers/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isDataLoaded=true;
                findViewById(R.id.change).setVisibility(View.VISIBLE);
                findViewById(R.id.progressbar).setVisibility(View.GONE);
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
                        TextView textView = findViewById(R.id.mobilenumber);
                        mobile = (String) dataSnapshot.child("MOBILE").getValue();
                        textView.setText(mobile);
                        new LoadRecentUsersFragment()
                                .addNewRecent(mobile, uid, name.toUpperCase(), NewOrderActivity.this);

                    }
                }catch (Exception e){e.printStackTrace();}


                try {
                    rate=(long) dataSnapshot.child("RATE").getValue();
                }catch (Exception e){rate=0;}

                try {
                    bottle=(long)dataSnapshot.child("WATER_BOTTLE_PENDING").getValue();
                }catch (Exception e){e.printStackTrace();  bottle=0;}

                new NewReturnBottlesActivity().loadBottle(findViewById(R.id.bottleProgress),findViewById(R.id.bottle),bottle);

                TextView textView1=findViewById(R.id.rate_amount);
                try {
                    wallet=(long)dataSnapshot.child("WALLET").getValue();
                }catch (Exception e){wallet=0;}

                loadWallet();
                Log.e("NewOrder","Rate "+rate);

                if(rate==0){
                    Log.e("NewOrder","Rate 0");
                    TextView change=findViewById(R.id.change);
                    change.setText("Save");
                    change.setBackgroundColor(getResources().getColor(R.color.black));
                    change.setTextColor(getResources().getColor(R.color.white));

                    findViewById(R.id.rate_edit).setVisibility(View.VISIBLE);
                    textView1.setText("No rate found");
                }else{Log.e("NewOrder","Rate not 0 "+rate);
                    findViewById(R.id.change).setVisibility(View.VISIBLE);
                    textView1.setText(String.format(Locale.UK,"Rate ₹ %d", rate));
                    quaText=quant.getText().toString().trim();
                    if(!quaText.isEmpty()){
                        totalAmount.setText(String.format(Locale.UK,"₹ %d",  Long.parseLong(quaText) * rate));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        quant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                quaText = editable.toString();
                if (rate != 0 && !quaText.isEmpty()) {
                    totalAmount.setText(String.format
                            (Locale.UK, "₹ %d", Long.parseLong(quaText) * rate));
                } else totalAmount.setText("₹ 0");
            }
        });
        amount_received.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                amtReceived = editable.toString().trim();

                if (!amtReceived.isEmpty() && (amtReceived.equals("0") || amtReceived.trim().replace("0", "").trim().isEmpty())) {
                    radioGroup.check(R.id.noPayment);
                    mode = "No payment";
                } else {
                    if (radioGroup.getCheckedRadioButtonId() != -1 && radioGroup.getCheckedRadioButtonId() == R.id.noPayment)
                        radioGroup.clearCheck();
                }
                Log.e("Amt", "Mode->" + mode);
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
