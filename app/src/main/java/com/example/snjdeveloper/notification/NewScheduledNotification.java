package com.example.snjdeveloper.notification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ReceivedNodeAdapter;
import com.example.snjdeveloper.RecyclerUI.UserListAdapter;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.extra.Parameters;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NewScheduledNotification extends AppCompatActivity {
    public ArrayList<String> registrationIDs;
    private ArrayList<String> displayList;
    private RecyclerView recyclerView;
    private Dialog dialog;
    private boolean goback = true;
    private ScrollView scrollView;
    TreeMap<Integer, Long> monthRecord;
    private AutoCompleteTextView actionView;
    private Parameters parameters=new Parameters(true);
    private ReceivedNodeAdapter adapter;
    private Parameters styleParameters=new Parameters();
    private HashMap<String, Uri> fileToUpload;
    private boolean comingFromCustomer;
    private View infoFrag;
    private ArrayList<HashMap<String, Object>> styleList;
    private HashMap<String, Object> dataMapToStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_send_notification);
        fileToUpload=new HashMap<>();
        dataMapToStore=new HashMap<>();
        monthRecord = new TreeMap<>();
        displayList = new ArrayList<>();
        TextView textView = findViewById(R.id.title);
        textView.setText("New info box");
        Button button = findViewById(R.id.button);
        registrationIDs = new ArrayList<>();
        findViewById(R.id.schedule_time_view).setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.listview);
//        String extra = getIntent().getStringExtra("DIS_DATA");
//        if (extra != null)
//            initializeRecyclerview(extra, getIntent().getStringExtra("DATA"));

//        button.setOnClickListener(view -> selectContact());
        findViewById(R.id.from_edit).setOnClickListener(v -> createDaysSelectorDialog((TextView) v, 0));
        findViewById(R.id.to_edit).setOnClickListener(v -> createDaysSelectorDialog((TextView) v, 1));
        String extra = getIntent().getStringExtra("DIS_DATA");
        if (extra != null)
        {    comingFromCustomer =true;
            button.setVisibility(View.GONE);
            initializeRecyclerview(extra, getIntent().getStringExtra("DATA"));
        }else button.setOnClickListener(view -> selectContact());
        initializeActionParameters();
    }
    private void initializeActionParameters() {
        String[] actionParas=parameters.getAllActions();
        if (!comingFromCustomer)
        {  actionParas= removeElementFromArray(actionParas);
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                R.layout.textview_layout, actionParas);
        actionView = findViewById(R.id.actionAuto);
        actionView.setAdapter(adapter1);
        RecyclerView recyclerView = findViewById(R.id.recyclerview_parameters);
        setStyleParameters();
        ArrayList<HashMap<String, Object>> list=new ArrayList<>();
        list.addAll(styleList);
        setData(recyclerView, list);
        actionView.setOnItemClickListener((parent, view, position, id) -> {
           list.clear();
            if (comingFromCustomer) {
                if (position == 0) {
                    initializeInfoParameters(list);
                }else {
                    if (infoFrag!=null)
                        infoFrag.setVisibility(View.GONE);
                    list.addAll( parameters.getActionParametersList(position));
                }
            }else
            {
                list.addAll(parameters.getActionParametersList(position+1));
            }
            list.addAll(styleList);
            parameters.removeFields(list,new String[]{"intent"});
            adapter.notifyDataSetChanged();
        });
    }
    private void initializeInfoParameters(ArrayList<HashMap<String, Object>> list) {
        Fragment fragmentClass =new InfoParaFragment(adapter,registrationIDs.get(0));
        FragmentTransaction fragmentTransaction2;
        fragmentTransaction2= getSupportFragmentManager().beginTransaction();
        if (infoFrag==null)
            infoFrag= findViewById(R.id.info_frag);
        infoFrag.setVisibility(View.VISIBLE);
        try{ fragmentTransaction2.replace(R.id.info_frag, Objects.requireNonNull(fragmentClass));
            fragmentTransaction2.commit();
        }catch (Exception ignored){}

    }


    private String[] removeElementFromArray(String[] actionParas) {
        List<String> list = new ArrayList<>(Arrays.asList(actionParas));
        list.remove(0);
        return list.toArray(new String[0]);

    }



    private void setData(RecyclerView recyclerView, ArrayList<HashMap<String, Object>> data) {
        Log.e("Data","Setting data\n"+data);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        adapter=new ReceivedNodeAdapter(data,this,getSupportFragmentManager());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }
    private void setStyleParameters() {
        styleList=styleParameters.getNotificationParametersList(0);
        styleParameters.removeFields(styleList,new String[]{"TITLE","DESC"});
    }

    private void initializeRecyclerview(String data, String id) {
        displayList.add(data);
        registrationIDs.add(id);
        UserListAdapter listAdapter = new UserListAdapter(this, registrationIDs, displayList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listAdapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("AppUpdate","Inside activity result");
        if (data!=null) {
            if (requestCode==101 ) {
                int position = data.getIntExtra("EXTRA", -1);
                if (position != -1) {
                    Log.e("AppUpdate", "Received->" + position);
                    String uri = data.getStringExtra("DATA");
                    HashMap<String, Object> map = this.adapter.data.get(position);
                    map.put("value", uri);
                    Log.e("AppUpdate", "Setting received data\ndata->" + map);
                    adapter.notifyItemChanged(position);
                } else Log.e("Position is ", "" + position);
            }
        }
    }

    private boolean isAddingDataSuccessful(HashMap<String, Object> notificationData)  {
        HashMap<String,Object>finalData=setDataToUpload();
        if (finalData==null)
            return false;
        for (Map.Entry<String,Object>data:finalData.entrySet())
            notificationData.put(data.getKey(),data.getValue());
        Log.e("Extra Data","Processed \n "+finalData);
        return true;
    }
    private void startUpload(HashMap<String, Object> finalData) {
        HashMap<String,String>dataToUpdate=new HashMap<>();
        Log.e("AppUpdate","Started storage upload");
        int size=fileToUpload.size();
        final int[] uploaded = {0};
        AtomicBoolean error = new AtomicBoolean(false);
        FirebaseStorage storage = new TransactionDb().getStorageReference(this);
        for (Map.Entry<String,Uri>file:fileToUpload.entrySet()){
            if (!error.get()) {
                String time = String.valueOf(System.currentTimeMillis());
                String location;
                location = "extra/" +"__"+file.getKey()+"__"+ time + ".png";
                Log.e("AppUpdate","storage upload file "+location+" started...");
                StorageReference storageRef = storage.getReference(location);
                storageRef.putFile(file.getValue()).
                        addOnFailureListener(exception -> {
                            Toast.makeText(this, "Failed to upload QR image", Toast.LENGTH_SHORT).show();
                            Log.d("Upload Image", "Image uploaded fail");
                            error.set(true);
                            msgSend("Error occurred",false,false,R.color.red);
                        }).addOnSuccessListener(taskSnapshot -> {
                    Log.e("AppUpdate","storage upload file "+location+" uploaded...");
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        uploaded[0]++;
                        String link=uri.toString();
                        String key=link.substring(link.indexOf("__")+2,link.lastIndexOf("__"));
                        dataToUpdate.put(key,uri.toString());
                        Log.e("AppUpdate","storage upload file "+location+" download url received ..."+uri+"\nkey "+key);
                        checkIfUploadCompleted(uploaded[0], size, error.get(), finalData,dataToUpdate);
                    }).addOnFailureListener(e -> error.set(true));
                });

            }
        }

    }
    private void checkIfUploadCompleted(int uploaded, int size, boolean error, HashMap<String, Object> finalData, HashMap<String, String> dataToUpdate) {
        if (uploaded == size&& !error)
        {   Log.e("AppUpdate","storage upload file "+" all uploaded. going to db upload");
            for(Map.Entry<String, Object> map:finalData.entrySet()){
                for (Map.Entry<String,String>record:dataToUpdate.entrySet())
                    ((HashMap<String,Object>)map.getValue()).put(record.getKey(),record.getValue());
        }
            storeData(finalData);
        }
    }
    private HashMap<String,Object> setDataToUpload() {
        ArrayList<HashMap<String, Object>> list =adapter.data;
        HashMap<String,Object>finalData = null;
        if (list!=null){
            Log.e("Data to save", "->" + list);
            int i = 0;
            finalData=new HashMap<>();
            for (HashMap<String, Object> record : list) {
                {String key;
                    try {
                        TextInputLayout layout=(TextInputLayout) Objects.requireNonNull(record.get("key_edittext"));
//                        key= (String) (layout).getHint();
                        key= (String) record.get("key");
                        if (record.get("value_edittext")!=null) {
                            String value = Objects.requireNonNull(((TextInputEditText) Objects.requireNonNull(record.get("value_edittext"))).getText()).toString();
                            Log.e("Data", i + " key->" + key + "\nvalue->" + value);
                            if (layout.getHelperText() != null && layout.getHelperText().equals("required") && (value.isEmpty())) {//case for required field
                                layout.setError(key.toLowerCase() + " required");
                                return null;
                            }
                            else if (!value.isEmpty())
                                configureDataToSave(key, value, finalData, true);
                        }else {
                            if (key!=null)
                            {   Object value= record.get("value");
                                configureDataToSave(key,value,finalData,false);
                            }
                        }

                    } catch (Exception e){e.printStackTrace();}
                    i++;
                }
            }
            Log.e("","-----------------------------------------------------------------------------------------------------------------------------------");
            Log.e("Final Data","->"+finalData);
            Log.e("","-----------------------------------------------------------------------------------------------------------------------------------");
        }
        return finalData;
    }
    private void configureDataToSave(String key, Object value, HashMap<String, Object> finalData, boolean requireCast) {
        if (requireCast) {
            if (value instanceof String){
                if (value.toString().toLowerCase().equals("true")||value.toString().toLowerCase().equals("false"))
                    value=Boolean.parseBoolean((String) value);
                else if (TextUtils.isDigitsOnly(key) && TextUtils.isDigitsOnly((CharSequence) value))//means if it a info data
                        value=String.valueOf(value);
                else if (TextUtils.isDigitsOnly((String) value))
                    value=Long.parseLong((String) value);
                else{
                    if((key.toLowerCase().equals("img")||key.toLowerCase().equals("icon"))&& !((String) value).startsWith("http")){
                        fileToUpload.put(key, Uri.parse((String) value));
                    }
                }

            }
        }

        finalData.put(key,value);
    }




    public boolean isNetworkAvailable(Context con) {
        try {
            ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (goback)
            finish();
    }

    private void createDaysSelectorDialog(TextView editText, int index) {
        View dialogView = View.inflate(this, R.layout.calendar_layout, null);
        final Dialog dialog = new Dialog(this, R.style.Dialog1);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;
        Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(dialogView);
        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
                return true;
            }
            return false;
        });
        CalendarView calendarView = dialogView.findViewById(R.id.calendar);
        Calendar cal1 = Calendar.getInstance();
        calendarView.setMinDate(cal1.getTimeInMillis());
        calendarView.setOnDateChangeListener((calendarView1, year, month, day) -> {
            dialog.dismiss();
            month += 1;
            String timeFormat = day + "/" + month + "/" + year;
            long time = convertDateTOMillis(timeFormat + " 00:00:00");
            if (monthRecord.isEmpty() || !monthRecord.containsValue(time)) {
                try {
                    if (index == 0 && monthRecord.containsKey(1) && time >= monthRecord.get(1)) {
                        msgSend("From should be smaller than to", false, false, R.color.red);
                        return;
                    } else if (index == 1 && monthRecord.containsKey(0) && time <= monthRecord.get(0)) {
                        msgSend("To should be greater than from", false, false, R.color.red);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                editText.setText(timeFormat);
                monthRecord.put(index, time);
            } else msgSend(timeFormat + " already selected", false, false, R.color.red);
        });
        dialog.show();
    }

    long convertDateTOMillis(String date) {
        try {
            return (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).parse(date).getTime());
        } catch (ParseException e) {
            return -1;
        }
    }

    private void selectContact() {
        String selector;
        selector = "UID";
        BottomSheetDialogFragment fragment =
                new UserDataSelector(selector, registrationIDs, displayList, recyclerView);
        fragment.showNow(getSupportFragmentManager(), "");
    }

    public void sendData(View view) {
        if (registrationIDs.isEmpty())
            msgSend("Please select users", false, false, (R.color.red));
        else if (monthRecord.size() != 2)
            msgSend("Please select date", false, false, (R.color.red));
        else
            initialize();
    }

    private void initialize() {
        EditText titleEdit = findViewById(R.id.title_notify);
        EditText contentEdit = findViewById(R.id.desc);
        String content = Objects.requireNonNull(contentEdit.getText()).toString().trim();
        String title = Objects.requireNonNull(titleEdit.getText()).toString().trim();
        if (title.isEmpty())
            return;
        if (content.isEmpty())
            return;
        if (isNetworkAvailable(this))
            sendPopup(title, content);
        else msgSend("Failed to connect", false, false, R.color.red);
    }

    private void sendPopup(String title, String content) {
        try {dataMapToStore.clear();
            if (isAddingDataSuccessful(dataMapToStore))
            checkUser(title, content);
            else msgSend("Please complete required field",false,false,R.color.red);

        } catch (Exception e) {
            e.printStackTrace();
            msgSend("Error occurred", false, false, (R.color.red));

        }
    }

    private void checkUser(String title, String content) {
        msgSend("Checking users please wait...", true, false,
                (R.color.colorAccent));
        new TransactionDb().getDatabaseReference(this).child("Notifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, String> userData = new HashMap<>();
                int i = 0;
                try {

                    if (displayList.toString().contains("All Customers")) {
                        if (snapshot.hasChildren()) {
                            ArrayList<String> list = new ArrayList<>();
                            for (DataSnapshot s : snapshot.getChildren())
                                list.add(s.getKey());
                            infoExitForAllCustomers(title, content, list);

                        } else sendToDb(title, content);
                    } else {
                        for (String key : registrationIDs) {
                            if (snapshot.child(key).exists()) {
                                userData.put(key, displayList.get(i).split(",")[1]);
                            }
                            i++;
                        }
                        if (userData.isEmpty())
                            sendToDb(title, content);
                        else usersExist(userData, title, content);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msgSend("Error occurred ...", false, false,
                            (R.color.red));
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void infoExitForAllCustomers(String title, String content, ArrayList<String> users) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Users exist");
        alert.setMessage("Users already exist in previous created info box");
        alert.setPositiveButton("Overwrite old info box", (dialog, which) -> {
            msgSend("Deleting old info box...", true, false, R.color.colorAccent);
            sendToDb(title, content);
        });
        alert.setNegativeButton("Remove from current list", (dialog, which) -> {
            for (String key : users)
                registrationIDs.remove(key);
            if (recyclerView.getAdapter() != null)
                recyclerView.getAdapter().notifyDataSetChanged();
            sendToDb(title, content);
        });
        alert.setNeutralButton("Cancel", null);
        alert.show();
        AlertDialog alertDialog = alert.create();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
    }

    private void usersExist(HashMap<String, String> userData, String title, String content) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Users already exist in the information box created before\nUsers:\n " +
                userData.values().toString().replace("[", "")
                        .replace("]", "").replace(",", "\n"));
        alert.setTitle("Users already exists in other Information box");
        msgSend("Stopping upload....", false, false, R.color.red);
        alert.setPositiveButton("Remove from current list", (dialog, which) -> {

            for (String key : userData.keySet()) {
                int j = registrationIDs.indexOf(key);
                registrationIDs.remove(key);
                displayList.remove(j);

            }
            if (recyclerView.getAdapter() != null)
                recyclerView.getAdapter().notifyDataSetChanged();
            sendToDb(title, content);
        });
        alert.setNegativeButton("Remove from old information box", (dialog, which) -> sendToDb(title, content));
        alert.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAccent));

    }


    private void sendToDb(String title, String content) {
        if (registrationIDs.isEmpty())
            msgSend("User list is empty", false, false, (R.color.red));
        else {
            HashMap<String, Object> data = new HashMap<>();
            for (String key : registrationIDs) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("TITLE", title);
                map.put("DESC", content);
                map.put("FROM", monthRecord.get(0));
                map.put("TO", monthRecord.get(1));
                map.putAll(dataMapToStore);
                data.put(key, map);
            }

            msgSend("Sending please wait...", true, false,
                    (R.color.colorAccent));
            if (fileToUpload.isEmpty())
            storeData(data);
            else startUpload(data);
        }

    }

    private void storeData(HashMap<String, Object> data) {
        new TransactionDb().getDatabaseReference(this).child("Notifications")
                .updateChildren(data)
//                    .setValue(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        msgSend("Send successfully", false, true, (R.color.colorAccent));
                    else
                        msgSend("Error occurred", false, false, (R.color.red));

                });
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }

    public void showHelpClicked(View view) {
        showInfoDialog(R.string.info_box_help_message, R.drawable.info_box_img);

    }

    void showInfoDialog(int stringid, int iconId) {
        setTheme(R.style.AppThemeBlack);
        if (dialog == null) {
            final View dialogView = View.inflate(this, R.layout.info_dialog, null);
            dialog = new Dialog(this, R.style.Dialog1);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(dialogView);
            dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
                if (i == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                }
                return false;
            });
            TextView textView = dialog.findViewById(R.id.help_msg);
            textView.setText(stringid);
            scrollView = dialog.findViewById(R.id.scrollView);
            Glide.with(dialogView).load(iconId).into((ImageView) dialogView.findViewById(R.id.icon));
            dialogView.findViewById(R.id.okBt).setOnClickListener(view -> dialog.dismiss());
            dialog.setOnDismissListener(dialog -> setTheme(R.style.AppTheme));
        } else {
            TextView textView = dialog.findViewById(R.id.help_msg);
            textView.setText(stringid);
            if (scrollView != null)
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            dialog.show();
            Glide.with(dialog.getContext()).load(iconId).into((ImageView) dialog.findViewById(R.id.icon));
        }
        dialog.show();
    }

    private void msgSend(String msg, boolean isIndefinite, boolean isFinish, int color) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), msg, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(color));
        if (isIndefinite) {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            findViewById(R.id.parent).animate().alpha(0.4f).setDuration(400);
            goback = false;
        } else {
            if (findViewById(R.id.parent).getAlpha() != 1)
                findViewById(R.id.parent).animate().alpha(1).setDuration(400);
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            goback = true;
        }

        if (isFinish)
            new Handler().postDelayed(() -> {
                snackbar.dismiss();
                returnData();
                //finish();
            }, 1500);
        snackbar.show();
    }

    private void returnData() {
        Intent intent = new Intent();
        intent.putExtra("reload", true);
        setResult(RESULT_OK, intent);
        finish();
    }
}
