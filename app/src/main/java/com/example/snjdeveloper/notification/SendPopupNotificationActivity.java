package com.example.snjdeveloper.notification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ReceivedNodeAdapter;
import com.example.snjdeveloper.RecyclerUI.UserListAdapter;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.extra.Parameters;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendPopupNotificationActivity extends AppCompatActivity {
    public ArrayList<String> registrationIDs;
    private ArrayList<String> displayList;
    private RecyclerView recyclerView;
    private ViewPager2 viewPager2;
    private View layoutSelectorView;
    private HashMap<String, Object> selectedHtml;
    private View displayWebview;
    private View displayImageView;
    private Dialog dialog;
    private boolean goback = true;
    private ScrollView scrollView;
    private AutoCompleteTextView actionView;
    private Parameters parameters=new Parameters(true);
    private ReceivedNodeAdapter adapter;
    private Parameters styleParameters=new Parameters();
    private int currentStyle=-1;
    private String[] oldKeySet;
    private HashMap<String, Uri> fileToUpload;
    private boolean comingFromCustomer;
    private View infoFrag;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_send_notification);
        fileToUpload=new HashMap<String, Uri>();
        displayList = new ArrayList<>();
        TextView textView = findViewById(R.id.title);
        textView.setText("Send popup message");
        Button button = findViewById(R.id.button);
        registrationIDs = new ArrayList<>();
        recyclerView = findViewById(R.id.listview);
        selectedHtml = new HashMap<>();
        layoutSelectorView = findViewById(R.id.selector_layout);
        MaterialButton button1 = findViewById(R.id.addLayoutBt);
        button1.setVisibility(View.VISIBLE);
        displayWebview = findViewById(R.id.display_html);
        displayImageView = findViewById(R.id.imageView_display);
        findViewById(R.id.del_webview).setOnClickListener(view -> {
            selectedHtml.clear();
            displayWebview.setVisibility(View.GONE);
            removeFields();
            currentStyle=-1;
            adapter.notifyDataSetChanged();
        });
        findViewById(R.id.close_selector).setOnClickListener(view -> layoutSelectorView.setVisibility(View.GONE));
        button1.setOnClickListener(view -> {
            loadLayoutSelector();
        });
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
        actionView.setOnItemClickListener((parent, view, position, id) -> {
            ArrayList<HashMap<String, Object>> list=new ArrayList<>();
            setData(recyclerView, list);
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
                list .addAll(parameters.getActionParametersList(position+1));
            }
            parameters.removeFields(list,new String[]{"intent"});
            adapter.notifyDataSetChanged();
            setStyleParameters(false);
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
    private void setStyleParameters(boolean reset){
        if (currentStyle!=-1) {
            ArrayList<HashMap<String, Object>> list = styleParameters.getNotificationParametersList(currentStyle);
            if (reset) {
                if (oldKeySet != null && oldKeySet.length != 0)
                    removeFields();
                fileToUpload.clear();
                oldKeySet = styleParameters.getNotificationParameters(currentStyle).keySet().toArray(new String[0]);
            }
            parameters.removeFields(list, new String[]{"TITLE", "DESC", "INTENT"});
            if (adapter!= null&& adapter.data!=null)
            adapter.data.addAll(list);
            else setData(findViewById(R.id.recyclerview_parameters),list);
            adapter.notifyDataSetChanged();
        }
    }

    private void removeFields() {
      try {
          parameters.removeFields(adapter.data, oldKeySet);
      }catch (Exception ignored){}
    }

    private void initializeRecyclerview(String data, String id) {
        displayList.add(data);
        registrationIDs.add(id);
        UserListAdapter listAdapter = new UserListAdapter(this, registrationIDs, displayList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listAdapter);
    }

    public static boolean isNetworkAvailable(Context con) {
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
        if (layoutSelectorView == null)
            finish();
        else if (layoutSelectorView.getVisibility() == View.VISIBLE)
        {  try {
            if ((int) selectedHtml.get("STYLE") != currentStyle)
            {
                currentStyle = (int) selectedHtml.get("STYLE");
                setStyleParameters(true);
            }

        }catch (Exception e){e.printStackTrace();}
            layoutSelectorView.setVisibility(View.GONE);
        }
        else if (goback)
            finish();
        //else finish();
    }

    private void loadLayoutSelector() {
        if (viewPager2 == null) {
            viewPager2 = findViewById(R.id.viewPager);
            new ViewPagerInitializer(viewPager2, this, findViewById(R.id.selectorBt),
                    findViewById(R.id.progressbar_selector), selectedHtml
                    , findViewById(R.id.webview_display), displayWebview, displayImageView);
        }
        layoutSelectorView.setVisibility(View.VISIBLE);
    }

    private void selectContact() {
        String selector;
        selector = "UID";
        BottomSheetDialogFragment fragment =
                new UserDataSelector(selector, registrationIDs, displayList, recyclerView);
        fragment.showNow(getSupportFragmentManager(), "");
    }

    public void sendData(View view) {
        if (!registrationIDs.isEmpty())
            initialize();
        else msgSend("Please select users", false, false, (R.color.red));
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
        sendPopup(title, content);
    }

    private void sendPopup(String title, String content) {
        Log.e("SendPopup", "->" + selectedHtml);
        try {
            if (isAddingDataSuccessful(selectedHtml)) {
                Log.e("Data","Adding successful "+selectedHtml);
                if (isNetworkAvailable(this)) {
                    if (!selectedHtml.isEmpty()) {
                        int style;
                        try {
                            style= ((int) selectedHtml.get("STYLE"));
                        }catch (ClassCastException e){style= Integer.parseInt(String.valueOf(selectedHtml.get("STYLE")));}
                        if (style <= NotificationAction.NOTIFICATION_DIALOG) {
                            selectedHtml.put("TITLE", title);
                            selectedHtml.put("DESC", content);
//                            selectedHtml.remove("HTML");
                        } else {
                            String html = (String) selectedHtml.get("HTML");
                            html = html.replace("insert_your_title", title.replace("\n", "<br>"));
                            html = html.replace("insert_your_content", content.replace("\n", "<br>"));
                            selectedHtml.put("HTML", html);
                            selectedHtml.put("ACTION", "ACTION_DELETE_NOTIFICATION");
//                selectedHtml.put("IS_DIALOG", true);
                        }
                        msgSend("Checking users please wait...", true, false, R.color.colorAccent);
                        FirebaseDatabase.getInstance().getReference().child("Notifications")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>)
                                                dataSnapshot.getValue();
                                        if (map != null) {
                                            msgSend("Stopping upload", false, false, R.color.colorAccent);
                                            if (displayList.toString().contains("All Customers")) {
                                                popupExitForAllCustomers();
                                            } else
                                                checkUsers(map);
                                        } else sendPopup();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    } else {
                        msgSend("Please select layout", false, false, (R.color.red));
                    }
                } else
                    msgSend("No network connectivity", false, false, (R.color.red));
            }
        } catch (Exception e) {
            e.printStackTrace();
            msgSend("Error occurred", false, false, (R.color.red));

        }

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
    private void popupExitForAllCustomers() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Users exist");
        alert.setMessage("Users already exist in previous created popup messages");
        alert.setPositiveButton("Delete Old popup messages", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                msgSend("Deleting old popup messages...", true, false, R.color.colorAccent);
                FirebaseDatabase.getInstance().getReference().child("Notifications").
                        setValue(null)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                                sendPopup();
                            else
                                msgSend("Error occurred", false, false, R.color.red);
                        });
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
        AlertDialog alertDialog = alert.create();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
    }

    void sendPopup() {
        if (registrationIDs.isEmpty())
            msgSend("User list is empty", false, false, (R.color.colorAccent));
        else {
            selectedHtml.put("USERS", registrationIDs.toString().replace("[", "")
                    .replace("]", ""));
            msgSend("Sending please wait...", true, false,
                    (R.color.colorAccent));
            if (fileToUpload.isEmpty())
                storePopup();
            else startUpload(selectedHtml);
        }

    }
    private void startUpload(HashMap<String, Object> finalData) {
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
                            finalData.put(key,uri.toString());
                            Log.e("AppUpdate","storage upload file "+location+" download url received ..."+uri+"\nkey "+key);
                            checkIfUploadCompleted(uploaded[0], size, error.get(), finalData);
                        }).addOnFailureListener(e -> error.set(true));
                });

            }
        }

    }
    private void checkIfUploadCompleted(int uploaded, int size, boolean error, HashMap<String, Object> finalData) {
        if (uploaded == size&& !error)
        {   Log.e("AppUpdate","storage upload file "+" all uploaded. going to db upload");
            storePopup();
        }
    }

    private void storePopup() {
        FirebaseDatabase.getInstance().getReference().child("Notifications")
                .child(String.valueOf(System.currentTimeMillis())).setValue(selectedHtml).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                msgSend("Send successfully", false, true, (R.color.colorAccent));
            else
                msgSend("Error occurred", false, false, (R.color.red));

        });
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
    void checkUsers(HashMap<String, HashMap<String, Object>> map) {
        HashMap<String, String> allUsers = new HashMap<>();
        HashMap<String, String> userData = new HashMap<>();
        try {
            for (String key : map.keySet()) {
                String tempUsers = (String) map.get(key).get("USERS");
                int i = 0;
                for (String userId : registrationIDs) {
                    if (tempUsers.contains(userId)) {
                        String oldValue = allUsers.get(key);
                        allUsers.put(key, oldValue == null ? userId : "," + userId);
                        userData.put(userId, displayList.get(i).split(",")[1]);
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            msgSend("Error occurred", false, false, R.color.red);
        }
        if (userData.isEmpty())
            sendPopup();
        else {
            usersAlreadyExistInPopup(userData, allUsers, map);
        }
    }

    private void usersAlreadyExistInPopup(HashMap<String, String> userData, HashMap<String, String> allUsers, HashMap<String, HashMap<String, Object>> map) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Users already exist in the popup message created before\nUsers:\n " + userData.values().toString().replace("[", "")
                .replace("]", "").replace(",", "\n"));
        alert.setTitle("Users already exists in other message");
        msgSend("Stopping upload....", false, false, (R.color.colorAccent));
        alert.setPositiveButton("Remove from current list", (dialogInterface, i) -> {
//            registrationIDs.removeAll(userData.keySet());
            RecyclerView recyclerView = findViewById(R.id.listview);
            int size = registrationIDs.size();
            for (int j = 0; j < size; j++) {
                if (userData.containsKey(registrationIDs.get(j))) {
                    registrationIDs.remove(j);
                    displayList.remove(j);
                    if (recyclerView.getAdapter() != null)
                        recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
            sendPopup();
            dialogInterface.dismiss();
        });
        alert.setNegativeButton("Remove from old layout", (dialog, which) -> {
            msgSend("Removing users please wait...", true, false, (R.color.colorAccent));
            for (String key : allUsers.keySet()) {
                String tempUsers = (String) map.get(key).get("USERS");
                for (String userId : Objects.requireNonNull(allUsers.get(key)).split(",")) {
                    if (tempUsers.contains("," + userId))
                        tempUsers = tempUsers.replace("," + userId, "");
                    else if (tempUsers.contains(", " + userId))
                        tempUsers = tempUsers.replace(", " + userId, "");
                    else if (tempUsers.contains(userId + ", "))
                        tempUsers = tempUsers.replace(userId + ", ", "");
                    else
                        tempUsers = tempUsers.replace(userId, "");
                }
                //if    tempusers   is  empty   del layout
                if (tempUsers.isEmpty())
                    map.put(key, null);
                else {
                    HashMap<String, Object> tempMap = map.get(key);
                    tempMap.put("USERS", tempUsers.trim());
                    map.put(key, tempMap);
                }
            }
            msgSend("Sending please wait...", true, false,
                    (R.color.colorAccent));
            FirebaseDatabase.getInstance().getReference().child("Notifications")
                    .setValue(map).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    sendPopup();
                else
                    msgSend("Error occurred", false, false, (R.color.red));

            });
            dialog.dismiss();

        });
        alert.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundColor(getResources().getColor(R.color.transparent));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAccent));
//        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
//        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
//        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }


    public void backOnClicked(View view) {
        finish();
    }

    public void showHelpClicked(View view) {
        showInfoDialog(R.string.popup_help_message, R.drawable.inapp_notification);

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
                finish();
            }, 2000);
        snackbar.show();
    }

    @Deprecated
    private void oldPopupNotification() {
        FirebaseFirestore.getInstance().collection("Extra")
                .document("Notification").set(selectedHtml)
                .addOnSuccessListener(documentSnapshot -> {
                    FirebaseDatabase.getInstance().getReference("Developers").child("UserNotification").child("USERS")
                            .setValue(registrationIDs.toString().replace("[", "")
                                    .replace("]", "")).addOnSuccessListener(
                            aVoid ->
                                    msgSend("Send successfully", false, true, (R.color.colorAccent)));
                });
    }
}
