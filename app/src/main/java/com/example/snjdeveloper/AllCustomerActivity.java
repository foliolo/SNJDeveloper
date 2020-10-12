package com.example.snjdeveloper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.snjdeveloper.admin.CustomerInfoActivity;
import com.example.snjdeveloper.user.AllBlockedUsersActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class AllCustomerActivity extends AppCompatActivity {
    ArrayList<String> data;
    private EntryItemAdapter adapter;
    private View progressBar;
    String key;
    private View nothing;
    RadioGroup radioGroup1;
    RadioGroup radioGroup2;
    private boolean isFilterVisible=false;
    private String from;
    private EditText searchEditText;
    private View searchBar;
    private View orderFilterView;
    private Runnable input_finish_checker;
    private Handler handler=new Handler();
    private long delay=1000;
    private long last_text_edit=0;
    private String searchKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        from=getIntent().getStringExtra("FROM");
        if(from!=null)
            setTheme(R.style.AppThemeBlack);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_cutomers);
        radioGroup1=findViewById(R.id.radiogroup1);
        orderFilterView=findViewById(R.id.order_filters);
        radioGroup2=findViewById(R.id.radiogroup2);
        progressBar=findViewById(R.id.progressbar);
        nothing=findViewById(R.id.nothing_found);
        RecyclerView entryRecyclerView = findViewById(R.id.entry_recycler_view);
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        entryRecyclerView.setLayoutManager(mLayoutManager);
        data=new ArrayList<>();
         adapter=  new EntryItemAdapter(data);
        entryRecyclerView.setAdapter(adapter);
        searchEditText=findViewById(R.id.searchEdit);
        searchBar=findViewById(R.id.searchBar);
        key="MOBILE";
        if (from!=null)
        {
            progressBar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
        }
        else
            query("");
        initializeSearch();
        firebase();
    }
    private void initializeSearch(){
        input_finish_checker= () -> {
            if (System.currentTimeMillis()>(last_text_edit+delay-500)){
                if (adapter!=null)
                    if (from!=null)
                        if (!searchKey.isEmpty())
                            query("+91"+searchKey);
                        else {  data.clear();adapter.notifyDataSetChanged();progressBar.setVisibility(View.GONE);nothing.setVisibility(View.GONE);}
                    else query("+91"+searchKey);
            }
        };
    }
    void firebase(){
        searchEditText.setHint("Search By Mobile Number");
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);
            }
            @Override
            public void afterTextChanged(Editable editable) {
                nothing.setVisibility(View.GONE);
                searchKey=editable.toString().trim();
                data.clear();
                adapter.notifyDataSetChanged();
                if (from!=null)
                    if (searchKey.trim().isEmpty()){
                        data.clear();
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }else{
                        progressBar.setVisibility(View.VISIBLE);
                        key="MOBILE";
                        last_text_edit=System.currentTimeMillis();
                        handler.postDelayed(input_finish_checker,delay);
                    }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    key="MOBILE";radioGroup2.clearCheck();radioGroup1.check(R.id.mobile);
                    last_text_edit=System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker,delay);
                }
            }
        });
    }



    private void query(String query){
        try {nothing.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            data.clear();
            adapter.notifyDataSetChanged();
        }catch (Exception e){e.printStackTrace();
        }
        Query query1;
        if(query.isEmpty())
           query1= FirebaseDatabase.getInstance().getReference("Customers").orderByChild(key);
        else query1=FirebaseDatabase.getInstance().getReference("Customers").orderByChild("MOBILE").
                startAt(query).endAt(query+"\uf8ff").limitToFirst(10);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data.clear();
                for(DataSnapshot snap:dataSnapshot.getChildren())
                { try {String mobile,uid,name;Object disData;
                        if(snap.child(key).getValue()!=null) {
                            mobile = (String) snap.child("MOBILE").getValue();
                            name = (String) snap.child("NAME").getValue();
                            uid = snap.getKey();
                            disData = snap.child(key).getValue();

                            if (key.contains("RATE") || key.contains("WALLET"))
                                data.add(uid+","+ disData +","+name+","+mobile);
                            else if (key.contains("NAME"))
                                data.add(uid+","+mobile+","+name+","+mobile);
                            else
                                data.add(uid+","+disData+","+name+","+mobile);
                        }
                }catch (Exception ignored){}

                }
                progressBar.setVisibility(View.GONE);
                if (data.size()<1)
                    nothing.setVisibility(View.VISIBLE);
                if (key.contains("BOTTLE"))
                    Collections.reverse(data);


                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void mobileClicked(View view) {
        radioGroup2.clearCheck();
        key="MOBILE";
        query("");
    }

    public void nameClicked(View view) {
        radioGroup2.clearCheck();
        key="NAME";
        query("");
    }

    public void addressClicked(View view) {
        radioGroup2.clearCheck();
       key= "USER_ADDRESS";
        query("");
    }

    public void paymentClicked(View view) {
        radioGroup1.clearCheck();
        key="WALLET";
        query("");
    }

    public void bottleClicked(View view) {
        radioGroup1.clearCheck();
        key="WATER_BOTTLE_PENDING";
        query("");
    }

    public void rateOnClick(View view) {
        radioGroup1.clearCheck();
       key="RATE";
        query("");
    }

    public void searchuttonOnclick(View view) {
        setTheme(R.style.AppThemeBlack);
        if (isFilterVisible)
            filterOnClicked(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation(true,view);

        }else{
            searchBar.setVisibility(View.VISIBLE);
        }
    }
    public  void expand(final View v, int duration, int targetHeight) {

        int prevHeight = v.getHeight();

        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    public  void collapse(final View v, int duration, int targetHeight) {
        int prevHeight = v.getHeight();
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, 0);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                v.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void animation(boolean show,View view) {
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

    public void endActivity(View view) {
        onBackPressed();
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
            if (from==null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    animation(false, findViewById(R.id.search));
                } else {
                    searchBar.setVisibility(View.INVISIBLE);
                }
            }
        //else finish();
    }

    @Override
    public void onBackPressed() {
       if (from==null) {
           if (searchBar.getVisibility() == View.VISIBLE)
               endsearch(null);
           else finish();
       }else finish();
    }


    public void backOnClicked(View view) {
        onBackPressed();
    }

    public void filterOnClicked(View view) {
        if (isFilterVisible) {
            collapse(orderFilterView, 300, 300);
            isFilterVisible = false;
        } else {
            expand(orderFilterView, 300, 300);
            isFilterVisible = true;
        }
    }

    public void blockedOnClick(View view) {
        startActivity(new Intent(this, AllBlockedUsersActivity.class));
    }


    public class EntryItemAdapter extends RecyclerView.Adapter<EntryItemAdapter.EntryItemViewHolder> {
        private final ArrayList<String> entryModes;

        EntryItemAdapter(ArrayList<String> entryModes) {
            this.entryModes = entryModes;
        }

        @NonNull
        @Override
        public EntryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new EntryItemViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.listview_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull EntryItemViewHolder entryItemViewHolder, int position) {
            entryItemViewHolder.bindEntryMode(entryModes.get(position), position);
        }

        @Override
        public int getItemCount() {
            return entryModes.size();
        }

        private class EntryItemViewHolder extends RecyclerView.ViewHolder {
            private final TextView mobile;
            private final TextView name;
            private ImageView imageView;

            EntryItemViewHolder(@NonNull View view) {
                super(view);
                mobile = view.findViewById(R.id.userDesc);
                imageView = view.findViewById(R.id.userImage);
                name = view.findViewById(R.id.userTitle);
            }

            void bindEntryMode(final String data, int position) {
                String[] record = data.split(",");
                name.setText(record[2]);
                Constants.setBackgroundColor(itemView.findViewById(R.id.cardview), itemView.getContext(), position);
                setNameImage(record[2]);
                if (key.contains("RATE") || key.contains("WALLET")) {
                    String w = String.valueOf(record[1]);
                    if (w.contains("-")) {
                        w = w.replace("-", "Pending ₹ ");
                        mobile.setTextColor(getResources().getColor(R.color.red));
                    } else {
                        w = "₹ " + w;
                        mobile.setTextColor(getResources().getColor(R.color.green_light));
                    }
                    mobile.setText(w);
                } else if (key.contains("BOTTLE")) {
                    String w = String.valueOf(record[1]);
                    if(!w.equals("0")){
                        w ="Pending "+w;
                        mobile.setTextColor(getResources().getColor(R.color.red));
                    }else mobile.setTextColor(getResources().getColor(R.color.green_light));
                    mobile.setText(w);
                }else{ mobile.setTextColor(getResources().getColor(R.color.object_confirmed_bg_gradient_end));
                        mobile.setText(record[1]);
                }

                try {
                    name.setText(record[2]);
                }catch (Exception es){es.printStackTrace();}
                Glide.with(itemView).load("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F"+
                        record[0]+"_pr"+"?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageView);
                itemView.setOnClickListener(view -> loadClick(record[3],record[0]));
            }

            private void setNameImage(String name) {
                if (name != null) {
                    if (!name.isEmpty()) {
                        String[] d = name.split(" ");
                        if (d.length > 1)
                            name = d[0].substring(0, 1) + d[1].substring(0, 1);
                        else name = name.substring(0, 1);
                    }
                    TextView textView = itemView.findViewById(R.id.username);
                    textView.setText(name.toUpperCase());
                }
            }
            private void loadClick(String mobile, String uid) {
                Intent intent;
                intent=new Intent(AllCustomerActivity.this, CustomerInfoActivity.class);
                intent.putExtra("0","BarCode");
                intent.putExtra("UID",uid);
                intent.putExtra("MOBILE",mobile);
                startActivity(intent);

            }

        }
    }


}
