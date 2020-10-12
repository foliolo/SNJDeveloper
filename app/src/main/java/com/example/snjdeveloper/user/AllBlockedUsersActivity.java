package com.example.snjdeveloper.user;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.Constants;
import com.example.snjdeveloper.Model;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.ViewHolder;
import com.example.snjdeveloper.admin.CustomerInfoActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllBlockedUsersActivity extends AppCompatActivity {
    ArrayList<Model> data;
    private EntryItemAdapter adapter;
    private View progressBar;
    String key = "MOBILE";
    private View nothing;
    private EditText searchEditText;
    private View searchBar;
    private Runnable input_finish_checker;
    private Handler handler = new Handler();
    private long delay = 1000;
    private long last_text_edit = 0;
    private String searchKey;
    private DatabaseReference database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_cutomers);
        findViewById(R.id.filter).setVisibility(View.GONE);
        findViewById(R.id.blockedUsers).setVisibility(View.GONE);
        database = new TransactionDb().getDatabaseReference(this).child("Blocked");
        ((TextView) findViewById(R.id.title)).setText(R.string.blocked_users);
        progressBar = findViewById(R.id.progressbar);
        nothing = findViewById(R.id.nothing_found);
        RecyclerView entryRecyclerView = findViewById(R.id.entry_recycler_view);
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        entryRecyclerView.setLayoutManager(mLayoutManager);
        data = new ArrayList<>();
        adapter = new EntryItemAdapter(data);
        entryRecyclerView.setAdapter(adapter);
        searchEditText = findViewById(R.id.searchEdit);
        searchBar = findViewById(R.id.searchBar);
        query("");
        initializeSearch();
        firebase();
    }

    private void initializeSearch() {
        input_finish_checker = () -> {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                if (adapter != null) {
                    String tempKey;
                    if (TextUtils.isDigitsOnly(searchKey)) {
                        key = "MOBILE";
                        tempKey = "+91" + searchKey;
                    } else {
                        key = "NAME";
                        tempKey = Constants.capitalize(searchKey);
                    }
                    query(tempKey);
                }
            }
        };
    }

    void firebase() {
        searchEditText.setHint("Search By Mobile Number");
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                nothing.setVisibility(View.GONE);
                searchKey = editable.toString().trim();
                data.clear();
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                key = "MOBILE";
                last_text_edit = System.currentTimeMillis();
                handler.postDelayed(input_finish_checker, delay);
            }
        });
    }


    private void query(String query) {
        try {
            nothing.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            data.clear();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query1;
        if (query.isEmpty())
            query1 = database.orderByChild(key);
        else query1 = database.orderByChild(key).
                startAt(query).endAt(query + "\uf8ff").limitToFirst(10);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data.clear();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    try {
                        Model model = snap.getValue(Model.class);
                        if (model != null) {
                            model.setUID(snap.getKey());
                            data.add(model);
                        }
                    } catch (Exception ignored) {
                    }

                }
                progressBar.setVisibility(View.GONE);
                if (data.size() < 1)
                    nothing.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void backOnClicked(View view) {
        finish();
    }

    public void searchuttonOnclick(View view) {
        setTheme(R.style.AppThemeBlack);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation(true, view);
        } else {
            searchBar.setVisibility(View.VISIBLE);
        }
    }

    void animation(boolean show, View view) {
        final RelativeLayout cardView = (RelativeLayout) searchBar;
        int height = cardView.getHeight();
        int width = cardView.getWidth();
        int endRadius = (int) Math.hypot(width, height);
        int cx = (int) (view.getX());// + (cardView.getWidth()));
        int cy = (int) (view.getY());// + cardView.getHeight();
        if (show) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(cardView, cx, cy, cy, endRadius);
            revealAnimator.setDuration(400);
            revealAnimator.start();
            cardView.setVisibility(View.VISIBLE);
            // showZoomIn();
        } else {
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(cardView, cx, cy, cx, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    cardView.setVisibility(View.INVISIBLE);
                }
            });
            anim.setDuration(400);
            anim.start();
        }

    }

    public void emptySearchbarClicked(View view) {
        if (!searchEditText.getText().toString().trim().isEmpty())
            searchEditText.setText("");
        else endsearch(searchBar);


    }

    public void endsearch(View view) {
        setTheme(R.style.AppTheme);
        if (!searchEditText.getText().toString().trim().isEmpty())
            searchEditText.setText("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation(false, findViewById(R.id.search));
        } else {
            searchBar.setVisibility(View.INVISIBLE);
        }
        //else finish();
    }

    class EntryItemAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
        private final ArrayList<Model> entryModes;

        EntryItemAdapter(ArrayList<Model> entryModes) {
            this.entryModes = entryModes;
        }

        @NonNull
        @Override
        public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder.FoodViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.listview_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder viewHolder, int position) {
            Model model = data.get(position);
            viewHolder.setName(model.getNAME());
            viewHolder.setMobile(model.getMOBILE());
            viewHolder.setImg(model.getId(), model.getNAME(), position);
            viewHolder.itemView.setOnClickListener(view -> loadClick(model));

        }

        private void loadClick(Model model) {
            Intent intent;
            intent = new Intent(AllBlockedUsersActivity.this, CustomerInfoActivity.class);
//                intent.putExtra("0","BarCode");
            intent.putExtra("1", model.getUID());
            intent.putExtra("2", model.getNAME());
            intent.putExtra("3", model.getMOBILE());
            intent.putExtra("4", model.getWALLET());
            intent.putExtra("5", model.getUSER_ADDRESS());
            intent.putExtra("6", model.getRATE());
            intent.putExtra("7", model.getUSER_EMAIL());
            intent.putExtra("IS_BLOCKED", true);
            startActivity(intent);

        }

        @Override
        public int getItemCount() {
            return entryModes.size();
        }

    }

}
