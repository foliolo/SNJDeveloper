package com.example.snjdeveloper.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.Constants;
import com.example.snjdeveloper.InfoActivity;
import com.example.snjdeveloper.LoadRecentUsersFragment;
import com.example.snjdeveloper.Model;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.ViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {
    private View progressBar;
    private Search adapter;
    private Runnable input_finish_checker;
    private Handler handler = new Handler();
    private long delay = 1000;
    private long last_text_edit = 0;
    private String searchKey = "";
    private ArrayList<Model> list;
    private View nothingFoundView;
    private Query orderDb;
    private Query transactionDb;
    private Query customerDb;
    ArrayList<Model> recentList;
    TextView recentTextView;
    private EditText searchbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
        nothingFoundView = findViewById(R.id.nothing_found);
        orderDb = FirebaseDatabase.getInstance().
                getReference().child("Orders");
        loadRecentData();
        recentTextView = findViewById(R.id.recentText);
        transactionDb = new TransactionDb().getReference(this);
        customerDb = FirebaseDatabase.getInstance().
                getReference("Customers");
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        list = new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_Chat);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new Search(this, list);
        recyclerView.setAdapter(adapter);
        initializeMedSearch();
        searchBarListner();
        recentList = new ArrayList<>();
    }

    private void loadRecentData() {
        ArrayList<String> recentTempList = new Recent().loadDataFromSharedPref(this);
        recentTempList.remove("POINTER");
        DatabaseReference db = new TransactionDb().getReference(this);
        for (String id : recentTempList) {
            String[] record = id.split(",");
            try {
                DatabaseReference query;
                if (record[1].contains("Trans"))
                    query = db;
                else query = FirebaseDatabase.getInstance().getReference(record[1]);
                Log.e("Recent", "record->" + Arrays.toString(record) + " id " + id);
                query.child(record[0])
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot d) {
                                try {
                                    Model model = d.getValue(Model.class);
                                    if (model != null) {
                                        Objects.requireNonNull(model).setId(d.getKey());
                                        model.setFrom(model.getQUANTITY() == 0 ? "Transactions" : "Orders");
                                        recentList.add(model);
                                    } else {
                                        Log.e("Recent", "Removing id->>" + id);
                                        recentTempList.remove(id);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.e("Recent", "RecentTemp->" + recentTempList.size() + "\nRecentList->" + recentList.size());
                                if (list.isEmpty() && recentTempList.size() == recentList.size()) {
                                    Collections.reverse(recentList);
                                    showRecent();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ArrayList<String> tempList = new LoadRecentUsersFragment().loadDataFromSharedPref(this);
        recentTempList.addAll(tempList);
        for (String id : tempList)
            FirebaseDatabase.getInstance().getReference("Customers")
                    .child(id.split(",")[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot d) {
                    Model model = d.getValue(Model.class);
                    if (model != null) {
                        model.setFrom("Customers");
                        model.setId(d.getKey());
                        recentList.add(model);
                    } else {
                        Log.e("Recent", "Removing id->>" + id);
                        recentTempList.remove(id);
                    }
                    Log.e("Recent", "RecentTemp->" + recentTempList.size() + "\nRecentList->" + recentList.size());
                    if (list.isEmpty() && recentTempList.size() == recentList.size())
                        showRecent();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void showRecent() {
        Log.e("Recent", "In  show    recent  ");
        if (searchKey.isEmpty()) {
            list.clear();
            list.addAll(recentList);
            adapter.notifyDataSetChanged();
            if (!list.isEmpty())
                recentTextView.setVisibility(View.VISIBLE);
            else Log.e("ShowRecent", "list is empty line 152");
        } else Log.e("ShowRecent", "Searchkey is not empty line 153");
    }

    public void endActivity(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void searchBarListner() {
        searchbar = findViewById(R.id.searchEdit);
        searchbar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchbar.setHint("Search by user / transaction / order");
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
                searchKey = editable.toString();
                progressBar.setVisibility(View.VISIBLE);
                recentTextView.setVisibility(View.GONE);
                try {
                    list.clear();
                    adapter.notifyDataSetChanged();
                    if (!searchKey.isEmpty()) {
                        nothingFoundView.setVisibility(View.GONE);
                        last_text_edit = System.currentTimeMillis();
                        handler.postDelayed(input_finish_checker, delay);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        nothingFoundView.setVisibility(View.GONE);
                        showRecent();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializeMedSearch() {
        input_finish_checker = () -> {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                if (adapter != null)
                    firebase();
            }
        };
    }

    public void backOnClicked(View view) {
        finish();
    }

    void firebase() {
        orderDb.orderByChild("DATE").startAt(searchKey).endAt(searchKey + "\uf8ff")
                .limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        initializeModel(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
        transactionDb.orderByChild("DATE").startAt(searchKey).endAt(searchKey + "\uf8ff")
                .limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        initializeModel(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
        String tempKey;
        String fieldName;
        if (TextUtils.isDigitsOnly(searchKey)) {
            fieldName = "MOBILE";
            tempKey = "+91" + searchKey;
        } else {
            fieldName = "NAME";
            tempKey = Constants.capitalize(searchKey);
        }
        customerDb.orderByChild(fieldName).startAt(tempKey).endAt(tempKey + "\uf8ff").limitToFirst(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        initializeModel(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    void initializeModel(DataSnapshot dataSnapshot) {
        nothingFoundView.setVisibility(View.GONE);
        if (!searchKey.isEmpty()) {
            for (DataSnapshot d : dataSnapshot.getChildren()) {
                Model model = d.getValue(Model.class);
                if (model != null) {
                    model.setId(d.getKey());
                    if (model.getKEY() == null) {
                        model.setFrom("Customers");
                        list.add(model);
                    } else {
                        //if (d.getKey().startsWith(searchKey)) {
                        model.setFrom(model.getQUANTITY() == 0 ? "Transactions" : "Orders");
                        list.add(model);
//                    }
                    }

                }
            }
            if (list.size() > 0)
                adapter.notifyDataSetChanged();
            else nothingFoundView.setVisibility(View.VISIBLE);
        }
    }

    public void emptySearchbarClicked(View view) {
        searchbar.setText("");
//        showRecent();
    }
}

class Search extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
    private final SimpleDateFormat dateFormat;
    private final Calendar calendar;
    private Context context;
    private ArrayList<Model> dataList;

    Search(Context context, ArrayList<Model> dataList) {
        this.context = context;
        this.dataList = dataList;
        dateFormat = new SimpleDateFormat("dd MMM yy", Locale.UK);
        calendar = Calendar.getInstance();

    }

    @Override
    public int getItemViewType(int position) {
        String from = dataList.get(position).getFrom();
        if (from.contains("Customers"))
            return R.layout.listview_layout;
        else return R.layout.adapter_layout;
//            return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder.FoodViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder viewHolder, int position) {
        {
            Model model = dataList.get(position);
            try {
                if (model.getFrom().contains("Customer")) {
                    viewHolder.setName(model.getNAME());
                    viewHolder.setMobile(model.getMOBILE());
                    viewHolder.setImg(model.getId(), model.getNAME(), position);
                    viewHolder.loadClick(model.getMOBILE(), model.getId());
                    TextView textView = viewHolder.mView.findViewById(R.id.extra);
//                        textView.setText("In Customers");
                    textView.setVisibility(View.VISIBLE);
                } else {
                    try {
                        viewHolder.setUserImage(model.getUID());
                        viewHolder.setAmount(getFormatedTime(Long.parseLong(model.getDATE())));
                        viewHolder.setWalletStatus("In " + model.getFrom().toLowerCase());
                        viewHolder.setOrderNo(model.getDATE());
                        personalData(model.getUID(), viewHolder);
                        TextView textView = viewHolder.mView.findViewById(R.id.wallet);
                        textView.setTextColor(context.getResources().getColor(R.color.green_light));
                        loadListener(model, viewHolder, position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void personalData(String uid, ViewHolder.FoodViewHolder viewHolder) {
        FirebaseDatabase.getInstance().getReference("Customers").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                viewHolder.setMobileNumber((String) dataSnapshot.child("MOBILE").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListener(Model model, final ViewHolder.FoodViewHolder viewHolder, int position) {
        viewHolder.itemView.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(context, InfoActivity.class);
            intent.putExtra("1", (model.getDATE()));
            intent.putExtra("2", (model.getDATE()));
            intent.putExtra("3", String.valueOf(model.getQUANTITY()));
            intent.putExtra("4", model.getNOTE());
            intent.putExtra("5", String.valueOf(model.getAMOUNT()));
            intent.putExtra("6", "" + model.getPAID_VIA());
            intent.putExtra("7", String.valueOf(model.getPAID_AMOUNT()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    private String getFormatedTime(long time) {
        calendar.setTimeInMillis(time);
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}

