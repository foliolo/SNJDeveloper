package com.example.snjdeveloper.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ReceivedNodeAdapter;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.extra.Parameters;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InfoParaFragment extends Fragment {
    private Parameters parameters=new Parameters();
    private ReceivedNodeAdapter adapter;
    private String uid;
    private AutoCompleteTextView IDtempView;

    public InfoParaFragment( ReceivedNodeAdapter adapter, String uid) {
        this.adapter = adapter;
        this.uid = uid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_para,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeInfoParameters();
    }
    private void initializeInfoParameters() {
        AutoCompleteTextView autoCompleteTextView=getView().findViewById(R.id.type_data_value);
        ArrayAdapter<String> adapter1=new ArrayAdapter<>(getActivity(),
                R.layout.textview_layout,new Parameters().getInfoTypeList());
        autoCompleteTextView.setAdapter(adapter1);
        infoIdInitializer(autoCompleteTextView);
    }
    public void infoIdInitializer(AutoCompleteTextView completeTextView){
        ProgressBar progressBar=getView().findViewById(R.id.id_progressbar);
        if (completeTextView!=null) {
            completeTextView.setOnItemClickListener((parent, view, position, id) -> {
                getView().findViewById(R.id.id_view).setVisibility(View.VISIBLE);
                    parameters.removeFields(adapter.data,new String[]{"1","2","3","4","5","6","7"});
//                    adapter.data.add(parameters.getInitialInfoParameters());
                if (IDtempView!=null)
                {
                    IDtempView.setText("");
                    IDtempView.setClickable(false);
                    IDtempView.setEnabled(false);
                }
                    adapter.notifyDataSetChanged();
                    loadUserOrders(uid, completeTextView.getText().toString(),progressBar,completeTextView);
            });
            }
    }
    private void loadUserOrders(String uid, String type, ProgressBar progressBar, AutoCompleteTextView completeTextView) {
        if (progressBar!=null)
            progressBar.setVisibility(View.VISIBLE);
        Log.e("InfoScreen",type+" started to fetch from db progressbar "+progressBar);
        ArrayList<HashMap<String,Object>>ordersData=new ArrayList<>();
        List<String> allList=new ArrayList<>();
        if (type.contains("Bottle")){
            FirebaseFirestore.getInstance().collection("Bottles")
                    .whereEqualTo("UID",uid)
                    //.orderBy("UID").
                    //startAt(uid)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                Log.e("InfoScreen",type+" received from db");
                if (!queryDocumentSnapshots.isEmpty()) {
                    for(DocumentSnapshot document:queryDocumentSnapshots.getDocuments()){
                        allList.add(document.getId());
                        HashMap<String, Object> temp = (HashMap<String, Object>) document.getData();
                        setDBData(temp, ordersData,document.getId());
                    }
                }else Log.e("InfoScreen",type+" received from db is empty");
                loadInfoScreen(allList,ordersData,type,completeTextView);
            });
        }else {
            DatabaseReference databaseReference;
            if (type.contains("Orders")) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Orders");
            } else databaseReference = new TransactionDb().getReference(getContext());
            databaseReference.orderByChild("UID").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.e("InfoScreen",type+" received from db");
                    if (progressBar!=null)
                        progressBar.setVisibility(View.GONE);
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            HashMap<String,Object>temp= (HashMap<String, Object>) dataSnapshot.getValue();
                            setDBData(temp, ordersData,dataSnapshot.getKey());
                            allList.add(dataSnapshot.getKey());
                        }
                        Log.e("InfoScreen",type+" received from db is loaded "+allList+"\n"+ordersData);
                    }else{
                        Log.e("InfoScreen",type+" received from db is empty");
                    }
                    loadInfoScreen(allList,ordersData,type,completeTextView);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void loadInfoScreen(List<String> allList, ArrayList<HashMap<String, Object>> ordersData, String type, AutoCompleteTextView completeTextView) {
         try {
             if (completeTextView.getText().toString().equals(type)) {
                 String[] paras = allList.toArray(new String[0]);
                 ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(),
                         R.layout.textview_layout, paras);
                 Log.e("InfoData", "Loading paras " + Arrays.toString(paras));
                 if (IDtempView==null)
                     IDtempView = getView().findViewById(R.id.id_data_value);
                 else{
                     IDtempView.setClickable(true);
                     IDtempView.setEnabled(true);
                 }
                 Log.e("InfoData", "->" + adapter.data);
                 IDtempView.setAdapter(adapter1);
                 IDtempView.setOnItemClickListener((parent, view, position, id) -> {
                     ArrayList<HashMap<String, Object>> list;
                     list = parameters.mapToList(ordersData.get(position), parameters.mapInfoWithTempParas());
                        parameters.removeFields(adapter.data, parameters.getAllInfoParameters());
                     adapter.data.addAll(0,list);
                     adapter.notifyDataSetChanged();
                 });
             } else
                 Log.e("InfoData", "Not settings.. current type is different old " + type + " new " + completeTextView.getText().toString());
         }catch (Exception ignored){}
//        Log.e("InfoData", "Not settings.. current actionview postion is not INFO_SCREEN " + actionView.getText().toString());
    }

    private void setDBData(HashMap<String, Object> data, ArrayList<HashMap<String, Object>> ordersData,String time) {
        HashMap<String,Object>map=new HashMap<>();
        map.put("1",time);
        map.put("2",time);
        map.put("3",(data.get("QUANTITY")!=null?data.get("QUANTITY"):0));
        map.put("4",data.get("NOTE"));
        map.put("5",data.get("AMOUNT"));
        map.put("6",(data.get("PAID_VIA")!=null?data.get("PAID_VIA"):"null"));
        map.put("7",data.get("PAID_AMOUNT"));
        map.put("ACTION","ACTION_OPEN_INFO");
        map.put("intent","ACTION_OPEN_INFO");
        ordersData.add(map);
    }

}
