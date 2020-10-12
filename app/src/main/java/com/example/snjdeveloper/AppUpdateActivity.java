package com.example.snjdeveloper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.extra.ImageSelectionOptionBottomSheet;
import com.example.snjdeveloper.extra.Parameters;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppUpdateActivity extends AppCompatActivity {
    ArrayList<HashMap<String,Object>>data;
    ArrayList<HashMap<String,Object>>screenParameters;
    View progress;
    private ReceivedNodeAdapter adapter;
    private AutoCompleteTextView styleView;
    private RecyclerView screenRecycler;
    private RecyclerView paraRecycler;
    Parameters parameters=new Parameters();
    private Uri apkFileUri;
    private TextView apkFileTextView;
    ImageView undoImageview;
    View buttonView;
    private int oldStyle;
    private HashMap<String,Uri>fileToUpload;
    private View progressbardialog;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);
        data=new ArrayList<>();
        screenParameters=new ArrayList<>();
        progress=findViewById(R.id.progress);
        screenRecycler=findViewById(R.id.recyclerview_screen);
        paraRecycler=findViewById(R.id.recyclerview_parameters);
        dbRef= FirebaseDatabase.getInstance().getReference("Admin").child("app").child("Update");
        loadDataFromDb();
        setSaveBt();
        setAutoCompleteView();
        undoImageview=findViewById(R.id.undoImageview);
        buttonView=findViewById(R.id.bottomButton);
        fileToUpload=new HashMap<>();
        initializeProgressdialog();
    }

    private void initializeProgressdialog() {
        progressbardialog=findViewById(R.id.progressbar_dialog);
        findViewById(R.id.background_Bt).setVisibility(View.GONE);
    }

    private void setAutoCompleteView() {
        ArrayAdapter<String> adapter1=new ArrayAdapter<>(this,
                R.layout.textview_layout,parameters.getAllNotifications());
        styleView=findViewById(R.id.styleAuto);
        styleView.setAdapter(adapter1);
        styleView.setOnItemClickListener((parent, view, position, id) -> {
            ArrayList<HashMap<String, Object>> list = parameters.getNotificationParametersList(position);
            parameters.removeFields(list,new String[]{"ACTION"});
            undoImageview.setVisibility(View.VISIBLE);
            if (buttonView.getVisibility()==View.GONE)
                buttonView.setVisibility(View.VISIBLE);
            setData(paraRecycler,list);
        });
    }

    private void setSaveBt() {
        findViewById(R.id.saveBt).setOnClickListener(v -> {
            if (this.adapter.data!=null) {
                setDataToUpload();
            }else Toast.makeText(AppUpdateActivity.this,"No data found",Toast.LENGTH_LONG).show();
        });
    }

    private void setDataToUpload() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        fileToUpload.clear();
        list.addAll(this.adapter.data);
        list.addAll(screenParameters);
        Log.e("Data to save", "->" + list);
        int i = 0;
        HashMap<String,Object>finalData=new HashMap<>();
        for (HashMap<String, Object> record : list) {
            {String key;
                try {TextInputLayout layout=(TextInputLayout) Objects.requireNonNull(record.get("key_edittext"));
                    key= (String) (layout).getHint();
                    if (record.get("value_edittext")!=null) {
                        String value = Objects.requireNonNull(((TextInputEditText) Objects.requireNonNull(record.get("value_edittext"))).getText()).toString();
                        Log.e("Data", i + " key->" + key + "\nvalue->" + value);
                        if (layout.getHelperText() != null && layout.getHelperText().equals("required") && (value.isEmpty())) {//case for required field
                            layout.setError(key.toLowerCase() + " required");
                            break;
                        }else if ((layout.isErrorEnabled()&& validColor(layout))){ //case for valid color if exist
                            break;
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
        uploadData(finalData);
        Log.e("","-----------------------------------------------------------------------------------------------------------------------------------");
    }

    private void uploadData(HashMap<String, Object> finalData) {
        new AlertDialog.Builder(this)
                .setTitle("App update")
                .setMessage("Data ready to upload")
                .setPositiveButton("Upload now", (dialog, which) -> {
                    progressbardialog.setVisibility(View.VISIBLE);
                    if (apkFileUri!=null)
                    {   if (!apkFileUri.toString().startsWith("http"))
                            fileToUpload.put("APP_NAME",apkFileUri);
                    }
                    if (!fileToUpload.isEmpty())
                    startUpload(finalData);
                    else storeDataInDb(finalData);
                })
                .setNegativeButton("Cancel",null)
                .create()
                .show();
    }

    private void storeDataInDb(HashMap<String, Object> finalData) {
        Log.e("AppUpdate","Started db upload");
         //new TransactionDb().getDatabaseReference(this,"test1-b0b2c").child("Admin").child("app").child("Update")
         dbRef.setValue(finalData).addOnCompleteListener(task -> {
                     progressbardialog.setVisibility(View.GONE);
                     if (task.isSuccessful()){
                         onBackPressed();
                     }else {
                         Toast.makeText(AppUpdateActivity.this,"Error occurred",Toast.LENGTH_LONG).show();
                     }
                 });
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
                FirebaseStorage tempStorage;
                if (file.getKey().equals("APP_NAME"))
                {   tempStorage=FirebaseStorage.getInstance();
                    location = "apps/" + finalData.get("APP_NAME");
                }
                else {
                    location = "extra/" +"__"+file.getKey()+"__"+ time + ".png";
                    tempStorage=storage;
                }
                Log.e("AppUpdate","storage upload file "+location+" started...");
                StorageReference storageRef = tempStorage.getReference(location);
                storageRef.putFile(file.getValue()).
                        addOnFailureListener(exception -> {
                    Toast.makeText(this, "Failed to upload QR image", Toast.LENGTH_SHORT).show();
                    Log.d("Upload Image", "Image uploaded fail");
                    error.set(true);
                    progressbardialog.setVisibility(View.GONE);
                    Toast.makeText(AppUpdateActivity.this,"Error occurred",Toast.LENGTH_LONG).show();
                }).addOnSuccessListener(taskSnapshot -> {
                            Log.e("AppUpdate","storage upload file "+location+" uploaded...");
                    if (!taskSnapshot.getMetadata().getName().contains(".apk")){
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            uploaded[0]++;
                            String link=uri.toString();
                            String key=link.substring(link.indexOf("__")+2,link.lastIndexOf("__"));
                            finalData.put(key,uri.toString());
                            Log.e("AppUpdate","storage upload file "+location+" download url received ..."+uri+"\nkey "+key);
                            checkIfUploadCompleted(uploaded[0], size, error.get(), finalData);
                        }).addOnFailureListener(e -> error.set(true));
                    }else {
                        Log.e("AppUpdate","storage upload file "+location+" is a apk not getting download url...");
                        uploaded[0]++;
                        checkIfUploadCompleted(uploaded[0], size, error.get(), finalData);
                    }
                });

            }
        }

    }

    private void checkIfUploadCompleted(int uploaded, int size, boolean error, HashMap<String, Object> finalData) {
        if (uploaded == size&& !error)
        {   Log.e("AppUpdate","storage upload file "+" all uploaded. going to db upload");
            storeDataInDb(finalData);
        }
    }

    @Override
    public void onBackPressed() {
        if (progressbardialog.getVisibility()!=View.VISIBLE)
            super.onBackPressed();

    }

    private boolean validColor(TextInputLayout layout) {
        if (layout.getError()!=null)
       return layout.getError().toString().contains("color");
        else return false;

    }

    private void configureDataToSave(String key, Object value, HashMap<String, Object> finalData, boolean requireCast) {
        if (requireCast) {
            if (value instanceof String){
                if (value.toString().toLowerCase().equals("true")||value.toString().toLowerCase().equals("false"))
                    value=Boolean.parseBoolean((String) value);
                else if (TextUtils.isDigitsOnly((String) value))
                    value=Long.parseLong((String) value);
                else{
                    if((key.toLowerCase().equals("img")||key.toLowerCase().equals("icon"))&& !((String) value).startsWith("http")){
                        fileToUpload.put(key,Uri.parse((String) value));
                    }
                }
            }
        }

        finalData.put(key,value);
    }

    private void loadDataFromDb() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String,Object>map= (HashMap<String, Object>) snapshot.getValue();
                Log.e("Received","->"+snapshot.getValue());
                HashMap<String,Object>styleParameters=new HashMap<>();
                if (map!=null)
                {   HashMap<String,Object>screenParameters=parameters.getUpdateScreenParameters();

                    if (snapshot.hasChild("STYLE"))
                    {   oldStyle=(int)((long)snapshot.child("STYLE").getValue());
                        styleParameters.putAll(parameters.getNotificationParameters(oldStyle));
                    }
                    for(Map.Entry<String, Object> record:map.entrySet()){
                        HashMap<String, Object> newMap = new HashMap<>();
                        String key=record.getKey();
                        newMap.put("key", key);
                        newMap.put("value", record.getValue());
                        if (isBooleanParameter(styleParameters.get(key))||isBooleanParameter(screenParameters.get(key)))
                            newMap.put("required",true);
                        if (screenParameters.containsKey(record.getKey())){
                                  AppUpdateActivity.this.screenParameters.add(newMap);
                        }else {
                            if (key.equals("STYLE"))
                            {   int pos=Integer.parseInt (String.valueOf( record.getValue()));
                                Log.e("Style Pos","->"+pos);
//                                styleView.setListSelection(pos);
                                setStyle(oldStyle);
                                //                                styleView.setText(parameters.getParameterName(""+record.getValue()),false);
                            }
                            else {
                                if (key.equals("IMG")||key.equals("ICON"))
                                    newMap.put("isImg", true);
                            }
                            data.add(newMap);
                            styleParameters.remove(key);
                        }
                    }
                }
                progress.setVisibility(View.GONE);
                if(!screenParameters.isEmpty())
                    setData(screenRecycler,screenParameters);
                if (!data.isEmpty())
                {   styleParameters.remove("ACTION");
                    setRemainingIntoList(styleParameters,data);
                    setData(paraRecycler,data);
                }
                Log.e("Data","->"+data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setStyle(int oldStyle) {
        styleView.setText(parameters.getParameterName(""+oldStyle),false);
    }

    private boolean isBooleanParameter(Object key) {
        return key instanceof Boolean && (Boolean) key;
    }

    private void setRemainingIntoList(HashMap<String, Object> styleParameters, ArrayList<HashMap<String, Object>> data) {
      data.addAll(parameters.mapToList(styleParameters));
    }

    void setData(RecyclerView recyclerView, ArrayList<HashMap<String, Object>> data){
        Log.e("Data","Setting data\n"+data);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        adapter=new ReceivedNodeAdapter(data,this,getSupportFragmentManager());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    public void addField(View view) {
        HashMap<String,Object>map=new HashMap<>();
        map.put("key","");
        map.put("value","");
        data.add(map);
        adapter.notifyItemInserted(data.size()-1);
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
            }else if (requestCode==102){
                apkFileUri= Uri.parse(data.getStringExtra("DATA"));
                if (apkFileTextView==null)
                apkFileTextView=findViewById(R.id.apkName);
                apkFileTextView.setText("New apk selected "+apkFileUri);
                apkFileTextView.setVisibility(View.VISIBLE);
                apkFileTextView.setSelected(true);
            }
        }
    }

    public void uploadAPKClicked(View view) {
        BottomSheetDialogFragment sheet=new ImageSelectionOptionBottomSheet(-1,"APK",102 ,"application/vnd.android.package-archive");
        sheet.show(getSupportFragmentManager(),"AppUpdate");

    }

    public void undoDataClicked(View view) {
        setData(screenRecycler,screenParameters);
        setData(paraRecycler,data);
        view.setVisibility(View.GONE);
        Log.e("Undo","Clicked");
        if (apkFileTextView!=null)
            apkFileTextView.setVisibility(View.GONE);

        apkFileUri=null;
        setStyle(oldStyle);
    }
}