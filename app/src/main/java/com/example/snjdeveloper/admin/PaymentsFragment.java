package com.example.snjdeveloper.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.InfoActivity;
import com.example.snjdeveloper.Model;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.ViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class PaymentsFragment extends Fragment {
    private View progressBar;
    private String type;
    private TransactionAdapter transactionAdapter;
    private ArrayList<Model>dataList=new ArrayList<>();
    private View nothing_found;
    private Runnable input_finish_checker;
    private Handler handler=new Handler();
    private long delay=1000;
    private long last_text_edit=0;
    private Query dbReference;
    private String  searchKey;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_all_user, container, false);
        searchKey = "";
        progressBar = v.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        type = this.getArguments() != null ? this.getArguments().getString("CLASS_NAME") : "";
        if (type.toLowerCase().contains("trans"))
            dbReference = new TransactionDb().getReference(getContext());
        else
            dbReference = FirebaseDatabase.getInstance().
                    getReference(type);
        nothing_found = v.findViewById(R.id.nothing_found);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = v.findViewById(R.id.recycler_Chat);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        transactionAdapter = new TransactionAdapter(getContext(), dataList);
        recyclerView.setAdapter(transactionAdapter);
        initializeSearch();
        searchBarListner();

        return v;
    }
    private void initializeSearch(){
        input_finish_checker= () -> {
            if (System.currentTimeMillis()>(last_text_edit+delay-500)&&!searchKey.isEmpty())
                loadFirebase(searchKey);
            else    {progressBar.setVisibility(View.GONE);nothing_found.setVisibility(View.GONE);}
        };
    }

    private void searchBarListner(){
        EditText searchbar=requireActivity(). findViewById(R.id.searchEdit);
        searchbar.setHint("Search by "+type.substring(0,type.length()-1).toLowerCase()+" number");
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchKey=editable.toString();
                dataList.clear();
                transactionAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                nothing_found.setVisibility(View.GONE);
                try {
                    if(!searchKey.isEmpty()) {
                        last_text_edit = System.currentTimeMillis();
                        handler.postDelayed(input_finish_checker, delay);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        nothing_found.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadFirebase(String key) {
        dbReference.orderByChild("DATE").startAt(searchKey).endAt(searchKey + "\uf8ff")
                .limitToLast(6).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataList.clear();
                transactionAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if (transactionAdapter != null)
                    transactionAdapter.notifyDataSetChanged();
                try {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (d.getKey() != null
//                                && d.getKey().startsWith(searchKey)
                        ) {
                            Model model = d.getValue(Model.class);
                            model.setId(d.getKey());
                            dataList.add(model);
                        }
                    }
                }catch (Exception e){e.printStackTrace();}
                if (dataList.size()==0){
                    nothing_found.setVisibility(View.VISIBLE);
                }else{
                    if (transactionAdapter!=null)
                        transactionAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    class TransactionAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder>{
        private final SimpleDateFormat dateFormat;
        private final Calendar calendar;
        Context context;
        ArrayList<Model> dataList;

        TransactionAdapter(Context context, ArrayList<Model> dataList) {
            this.context = context;
            this.dataList = dataList;
            dateFormat = new SimpleDateFormat("dd MMM yy", Locale.UK);
            calendar = Calendar.getInstance();
        }
        @NonNull
        @Override
        public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
          return   new ViewHolder.FoodViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.new_adapter_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder viewHolder, int position) {
            Model model=dataList.get(position);
            try {
                TextView textView = viewHolder.itemView.findViewById(R.id.wallet);
                if(type.equals("Customers")){
                    viewHolder.setUserImage( model.getId());
                    viewHolder.setTime(model.getNAME());
                    viewHolder.setOrderNo(model.getMOBILE());
                    viewHolder.setMobileNumber(model.getNAME());
                    viewHolder.setWalletStatus(model.getUSER_ADDRESS());
                    if (model.getWALLET()<0) {
                        textView.setTextColor(context.getResources().getColor(R.color.red));
                        viewHolder.setAmount("Pending"+getFormatedAmount(model.getWALLET()));
                    }else viewHolder.setAmount(getFormatedAmount(model.getWALLET()));
                }else{
                    viewHolder.setWalletStatus("Paid " +getFormatedAmount(model.getPAID_AMOUNT()));
                    String time=model.getId();
                    viewHolder.setOrderNo(time);
                    viewHolder.setUserImage(model.getUID());
                    viewHolder.setTime(getFormatedTime(Long.parseLong(Objects.requireNonNull(time))));
                    personalData(model.getUID(),viewHolder);
                    if (type.equals("Orders")) {
                        if (model.getPAID_AMOUNT() < model.getAMOUNT()) {
                            textView.setTextColor(context.getResources().getColor(R.color.red));
                            viewHolder.setAmount("Pending "+getFormatedAmount(model.getPAID_AMOUNT()));
                        }else viewHolder.setAmount(getFormatedAmount(model.getPAID_AMOUNT()));
                        viewHolder.setAmount(getFormatedAmount(model.getAMOUNT()));
                    } else
                        viewHolder.setAmount("Paid Via " + model.getPAID_VIA());
                }
                loadListener(model, viewHolder, position);

            } catch (Exception ignored) {
            }
        }
        private String getFormatedAmount(long amount){
            return "₹ "+ NumberFormat.getNumberInstance(Locale.UK).format(amount);
        }

        private String getFormatedTime(long time) {
            calendar.setTimeInMillis(time);
            return dateFormat.format(calendar.getTime());
        }
        private void personalData(String uid, ViewHolder.FoodViewHolder viewHolder){
            FirebaseDatabase.getInstance().getReference("Customers").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    viewHolder.setMobileNumber((String)dataSnapshot.child("MOBILE").getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        private void loadListener(Model model, final ViewHolder.FoodViewHolder viewHolder, int position) {
            viewHolder.itemView.setOnClickListener(view -> {
                Intent intent;

                if(type.equals("Customers")){
                    intent = new Intent(context, CustomerInfoActivity.class);

                    intent.putExtra("2", model.getNAME());
                    intent.putExtra("3", model.getMOBILE());
                    intent.putExtra("4",model.getWALLET_STATUS());
                    intent.putExtra("5",model.getUSER_ADDRESS());
                    intent.putExtra("6",model.getRATE());


                }else {
                    intent = new Intent(context, InfoActivity.class);

//                    intent.putExtra("2",String.valueOf (model.getTIME()));
                    intent.putExtra("2", (model.getDATE()));
                    intent.putExtra("3", String.valueOf(model.getQUANTITY()));
                    intent.putExtra("4", model.getNOTE());
                    intent.putExtra("5", String.valueOf(model.getAMOUNT()));
                    intent.putExtra("6", "" + model.getPAID_VIA());
                    intent.putExtra("7", String.valueOf(model.getPAID_AMOUNT()));

                }

                intent.putExtra("1", model.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }



        @Override
        public int getItemCount() {
                return dataList.size();
            }

        }

}
