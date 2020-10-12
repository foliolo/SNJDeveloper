package com.example.snjdeveloper.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.snjdeveloper.Constants;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.RecyclerUI.UserListAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class UserDataSelector extends BottomSheetDialogFragment {
    private final String selector;
    private RecyclerView listView;
    private ArrayList<String> registrationIDs;
    private ArrayList<String> displayDataList;
    private RecyclerView.Adapter listAdapter;
    private String errorMessage;
    private CheckBox selectAllCheckBox;
    private String msgSuffix;

    public UserDataSelector(String selector, ArrayList<String> registrationIDs, ArrayList<String> displayDataList
            , RecyclerView listview) {
        this.selector = selector;
        this.registrationIDs = registrationIDs;
        this.displayDataList = displayDataList;
        this.listView = listview;
    }

    private ArrayList<String> data;
    private ArrayList<String> selectorData;
    private EntryItemAdapter adapter;
    private View progressBar;
    private String key;
    private View nothing;
    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private boolean isFilterVisible=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_all_cutomers, container, false);
        v.findViewById(R.id.back).setOnClickListener(this::backOnClicked);
        v.findViewById(R.id.back_main).setOnClickListener(this::backOnClicked);
        v.findViewById(R.id.search).setOnClickListener(this::searchuttonOnclick);
        v.findViewById(R.id.filter).setOnClickListener(this::filterOnClicked);
        v.findViewById(R.id.backSearchBar).setOnClickListener(this::backOnClicked);
        v.findViewById(R.id.cancelsearcBar).setOnClickListener(this::emptySearchbarClicked);
        v.findViewById(R.id.mobile).setOnClickListener(this::mobileClicked);
        v.findViewById(R.id.nameSearch).setOnClickListener(this::nameClicked);
        v.findViewById(R.id.addressSearch).setOnClickListener(this::addressClicked);
        v.findViewById(R.id.rateSearch).setOnClickListener(this::rateOnClick);
        v.findViewById(R.id.bottleClicked).setOnClickListener(this::bottleClicked);
        v.findViewById(R.id.paymentSearch).setOnClickListener(this::paymentClicked);
        v.findViewById(R.id.blockedUsers).setVisibility(View.GONE);
        if (selector.contains("EMAIL")) {
            errorMessage = "Email not found";
            msgSuffix = "email";
        } else if (selector.contains("TOKEN")) {
            errorMessage = "User logged out. We cannot send message to a logged out user.";
            msgSuffix = "notification";
        } else {
            msgSuffix = "message";
            errorMessage = "An error occurred. Contact developer if it continues";
        }
        return v;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTag().equals("BillReport"))
            new MoreOptionFragment.Action().actonEndListener();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radioGroup1 = requireView().findViewById(R.id.radiogroup1);
        selectAllCheckBox = view.findViewById(R.id.checkbox_all_customers);
        Log.e("IDs", "Selected->" + registrationIDs);
        if (registrationIDs.contains("Select All") || displayDataList.toString().contains("All Customers"))
            selectAllCheckBox.setChecked(true);
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            registrationIDs.clear();
            displayDataList.clear();
            if (isChecked) {
                displayDataList.add("display_icon,Send " + msgSuffix + " to all ,All Customers,0");
                if (selector.contains("TOKEN"))
                    registrationIDs.add("Select All");
                else {
                    registrationIDs.addAll(selectorData);
                    if (registrationIDs.contains(null))
                        removeElementFromList(registrationIDs, null);
                }
            }
            adapter.notifyDataSetChanged();
            listAdapter.notifyDataSetChanged();
        });
        selectorData = new ArrayList<>();
        radioGroup2 = getView().findViewById(R.id.radiogroup2);
        progressBar = getView().findViewById(R.id.progressbar);
        nothing = getView().findViewById(R.id.nothing_found);
        RecyclerView entryRecyclerView = getView().findViewById(R.id.entry_recycler_view);
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        entryRecyclerView.setLayoutManager(mLayoutManager);
        data = new ArrayList<>();
        adapter = new EntryItemAdapter(data, 2, getActivity());
        entryRecyclerView.setAdapter(adapter);
        key = "MOBILE";
        Glide.with(getActivity()).load(R.drawable.arrow_down_white).into((ImageView) view.findViewById(R.id.back_main));
        query("");
        initializeListView();
        firebase();
    }

    private void removeElementFromList(ArrayList<String> registrationIDs, String value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registrationIDs.removeIf(n -> Objects.equals(n, value));
        } else {
            ArrayList<String> remainingList = new ArrayList<>();
            for (String ele : registrationIDs) {
                if (ele != null)
                    remainingList.add(ele);
            }
            registrationIDs.clear();
            registrationIDs.addAll(remainingList);
        }
    }


    private void initializeListView() {
        if (listView.getAdapter() == null) {
            listAdapter = new EntryItemAdapter(displayDataList, 1, getActivity());
            LinearLayoutManager manager = new LinearLayoutManager(getActivity());
            listView.setLayoutManager(manager);
            listView.setAdapter(listAdapter);
        } else if (listView.getAdapter() instanceof UserListAdapter)
            listAdapter = listView.getAdapter();
        else
            listAdapter = listView.getAdapter();
    }

    private void firebase(){
        EditText editText = requireView().findViewById(R.id.searchEdit);
        editText.setHint("Search By Mobile Number");

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {

                progressBar.setVisibility(View.VISIBLE);
                nothing.setVisibility(View.GONE);
                String s=editable.toString().trim();
                key = "MOBILE";
                radioGroup2.clearCheck();
                radioGroup1.check(R.id.mobile);
                query("+91" + s);


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
        if (query.isEmpty()) {
            query1 = FirebaseDatabase.getInstance().getReference("Customers").orderByChild(key);
        } else
            query1 = FirebaseDatabase.getInstance().getReference("Customers").orderByChild("MOBILE").
                    startAt(query).endAt(query + "\uf8ff").limitToFirst(10);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selectorData.clear();
                if (!getTag().equals("BillReport"))
                    selectAllCheckBox.setVisibility(View.VISIBLE);
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    try {
                        String mobile, uid, name;
                        Object disData;
                        if (snap.child(key).getValue() != null) {
                            mobile = (String) snap.child("MOBILE").getValue();
                            name = (String) snap.child("NAME").getValue();
                            uid = snap.getKey();
                            disData = snap.child(key).getValue();
                            if (selector.contains("USER_EMAIL")) {
                                String tempData = (String) snap.child("USER_EMAIL").getValue();
                                disData = tempData == null ? disData + "\nNo Email Found" : disData + "\n" + tempData;
                            } else if (selector.contains("TOKEN")) {
                                String token = (String) snap.child(selector).getValue();
                                if (token == null) {
                                    disData = disData + "\nLogged Out";
                                }
                            }
                            if (selector.contains("UID"))
                                selectorData.add(uid);
                            else
                                selectorData.add((String) snap.child(selector).getValue());

                            String storeData;
                            if (key.contains("RATE") || key.contains("WALLET"))
                                storeData = (uid + "," + disData + "," + name + "," + mobile);
                            else if (key.contains("NAME"))
                                storeData = (uid + "," + mobile + "," + name + "," + mobile);
                            else
                                storeData = (uid + "," + disData + "," + name + "," + mobile);
                            if (!data.contains(storeData))
                                data.add(storeData);
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

    private void mobileClicked(View view) {
        radioGroup2.clearCheck();
        key="MOBILE";
        query("");
    }

    private void nameClicked(View view) {
        radioGroup2.clearCheck();
        key="NAME";
        query("");
    }

    private void addressClicked(View view) {
        radioGroup2.clearCheck();
        key= "USER_ADDRESS";
        query("");
    }

    private void paymentClicked(View view) {
        radioGroup1.clearCheck();
        key="WALLET";
        query("");
    }

    private void bottleClicked(View view) {
        radioGroup1.clearCheck();
        key="WATER_BOTTLE_PENDING";
        query("");
    }

    private void rateOnClick(View view) {
        radioGroup1.clearCheck();
        key="RATE";
        query("");
    }

    private void searchuttonOnclick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation(true,view);
        }else{
            requireView().findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
        }
        if (isFilterVisible)
            filterOnClicked(null);
    }
    private  void expand(final View v, int duration, int targetHeight) {

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

    private  void collapse(final View v, int duration, int targetHeight) {
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
    private void animation(boolean show, View view) {
        final RelativeLayout cardView = requireView().findViewById(R.id.searchBar);
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


    private void emptySearchbarClicked(View view) {
        EditText editText = requireView().findViewById(R.id.searchEdit);


        if (!editText.getText().toString().trim().isEmpty())
            editText.setText("");
        else endsearch(getView().findViewById(R.id.searchBar));


    }

    private void endsearch(View view) {
        EditText editText = getView().findViewById(R.id.searchEdit);
        if (!editText.getText().toString().trim().isEmpty())
            editText.setText("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation(false, getView().findViewById(R.id.search));
        } else {
            getView().findViewById(R.id.searchBar).setVisibility(View.INVISIBLE);
        }
        //else finish();
    }




    public void backOnClicked(View view) {
        dismiss();
    }

    private void filterOnClicked(View view) {
        if (isFilterVisible) {
            collapse(requireView().findViewById(R.id.order_filters), 300, 300);
            isFilterVisible = false;
        } else {
            expand(requireView().findViewById(R.id.order_filters), 300, 300);
            isFilterVisible = true;
        }
    }


    public class EntryItemAdapter extends RecyclerView.Adapter<EntryItemAdapter.EntryItemViewHolder> {
        private final ArrayList<String> entryModes;
        int from;
        Context context;

        EntryItemAdapter(ArrayList<String> entryModes, int i, Context context) {
            this.entryModes = entryModes;
            this.from = i;
            this.context = context;
        }

        @NonNull
        @Override
        public EntryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout;
            if (from==2)
                layout=R.layout.user_list_checkbox;
            else layout=R.layout.listview_layout;
            return new EntryItemViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(layout, parent, false));

        }

        @Override
        public void onBindViewHolder(@NonNull EntryItemViewHolder entryItemViewHolder, int position) {
            entryItemViewHolder.bindEntryMode(entryModes.get(position),position);
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
                imageView= view.findViewById(R.id.userImage);
                name=view.findViewById(R.id.userTitle);
            }
            void bindEntryMode(final String data, int position) {
                try {
                    String[] record = data.split(",");
                    name.setText(record[2]);

                    Constants.setBackgroundColor(itemView.findViewById(R.id.cardview), getContext(), position);
                    setNameImage(record[2]);
                    if (key.contains("RATE") || key.contains("WALLET")) {
                        String w = String.valueOf(record[1]);
                        if (w.contains("-")) {
                            w = w.replace("-", "Pending ₹ ");
                            mobile.setTextColor(context.getResources().getColor(R.color.red));
                        } else {
                            w = "₹ " + w;
                            mobile.setTextColor(context.getResources().getColor(R.color.green_light));
                        }
                        mobile.setText(w);
                    } else if (key.contains("BOTTLE")) {
                        String w = String.valueOf(record[1]);
                        if (!w.equals("0")) {
                            w = "Pending " + w;
                            mobile.setTextColor(context.getResources().getColor(R.color.red));
                        } else
                            mobile.setTextColor(context.getResources().getColor(R.color.green_light));
                        mobile.setText(w);
                    } else {
                        mobile.setTextColor(context.getResources().getColor(R.color.object_confirmed_bg_gradient_end));
                        mobile.setText(record[1]);
                    }

                try {
                    name.setText(record[2]);
                }catch (Exception es){es.printStackTrace();}
                Glide.with(itemView).load("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F"+
                        record[0]+"_pr"+"?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageView);

               if (from==2) {
                   CheckBox checkBox = itemView.findViewById(R.id.checkbox);
                   String sData=selectorData.get(position);
                   Log.e("SData","->"+sData);
                   if (displayDataList.contains(data))
                       checkBox.setChecked(true);
                   else checkBox.setChecked(false);
                   checkBox.setOnClickListener(view -> loadClick(position, checkBox, data));
               }
                }catch (Exception e){e.printStackTrace();}

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

            private void loadClick(int pos, CheckBox checkBox, String data) {
                Log.e("UserDataSelect", "Clicked->" + registrationIDs);
                if (selectAllCheckBox.isChecked()) {
                    selectAllCheckBox.setChecked(false);
                    registrationIDs.clear();
                    displayDataList.clear();
                }
                if (checkBox.isChecked()) {
                    String d = selectorData.get(pos);
                    if (d != null) {
                        registrationIDs.add(d);
                        displayDataList.add(data);
                    } else {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                        checkBox.setChecked(false);
                    }
                } else {
                    displayDataList.remove(data);
                    registrationIDs.remove(selectorData.get(pos));
                }

                listAdapter.notifyDataSetChanged();
            }

        }
    }

}
