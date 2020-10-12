package com.example.snjdeveloper.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.snjdeveloper.InfoActivity;
import com.example.snjdeveloper.Model;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static com.example.snjdeveloper.Constants.isListLoaded;
import static com.example.snjdeveloper.Constants.searchKey;
import static com.example.snjdeveloper.admin.ControllerActivity.Totalcount;
    public class SearchAdapter extends FirebaseRecyclerAdapter<Model, ViewHolder.FoodViewHolder> {
        private SimpleDateFormat dateFormat;
        private Calendar calendar;
        private Context context;
        private View progressBar;
        private String type;
        private View nothing_found;
        //private boolean isListLoaded ;
        private String myid;
        SearchAdapter(Class<Model> modelClass,
                      int modelLayout,
                      Class<ViewHolder.FoodViewHolder> viewHolderClass,
                      Query ref, Context context, View progressBar, String type, View nothing_found, FragmentActivity activity, String myid) {

            super(new FirebaseRecyclerOptions.Builder
                    <Model>()
                    .setQuery(ref,
                            Model.class).build());
//            super(modelClass, modelLayout, viewHolderClass, ref);
            dateFormat = new SimpleDateFormat("dd MMM yy", Locale.UK);
            calendar = Calendar.getInstance();

            this.context = context;
            this.myid = myid;//getContext().getSharedPreferences("USERCREDENTIALS",Context.MODE_PRIVATE).getString("COLLEGEID",null);
            this.nothing_found = nothing_found;
            this.type = type;
            this.progressBar = progressBar;

        }


        @NonNull
        @Override
        public ViewHolder.FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;

            if (viewType == 1) {
                isListLoaded = true;
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_adapter_layout, parent, false);
                return new ViewHolder.FoodViewHolder(view);
            } else {
                isListLoaded=true;
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_empty, parent, false);
                return new ViewHolder.FoodViewHolder(view);
            }
        }

        @Override
        public int getItemViewType(int position) {

            Model model = getItem(position);
            String name;
            try {
                String second;
                if(type.equals("Customers")){
                    name = model.getMOBILE_NUMBER();
                    second = model.getNAME()+","+model.getUSER_ADDRESS();
                }else{
                    name =getRef(position).getKey();
                    second = model.getPAID_VIA();
                }

                int LAYOUT_ONE = 1;
                int LAYOUT_TWO = 2;
                switch (CheckFilters(name, second)) {
                    case 1:
                        Totalcount++;
                        return LAYOUT_ONE;
                    case 2:
                        return LAYOUT_TWO;
                }
            } catch (Exception ignored) {
            }
            return position;

        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder.FoodViewHolder viewHolder, int position) {
            super.onBindViewHolder(viewHolder, position);
            viewHolder.setIsRecyclable(false);
        }

        @Override
        protected void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder viewHolder, int position, @NonNull Model model) {
            if (getItemCount() < 1) {

                nothing_found.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                try {TextView textView = viewHolder.mView.findViewById(R.id.wallet);
                    if(type.equals("Customers")){
                        viewHolder.setUserImage(getRef(position).getKey());
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
                        String time=getRef(position).getKey();
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
        }

        private int CheckFilters(String username, String userid) {
            if (username == null) {
                username = "";
            }
            if (userid.toLowerCase().equals(myid.toLowerCase())) {
                return 2;
            } else if (searchKey.equals("not")) {
                return 1;
            } else if (username.toLowerCase().contains(searchKey.toLowerCase())
                    || userid.toLowerCase().contains(searchKey.toLowerCase())) {

                return 1;
            } else {
                return 2;
            }
        }


//        @Override
//        protected void populateViewHolder(final ViewHolder.FoodViewHolder viewHolder, final Model model, final int position) {
//            if (getItemCount() < 1) {
//
//                nothing_found.setVisibility(View.VISIBLE);
//                progressBar.setVisibility(View.GONE);
//            } else {
//                try {TextView textView = viewHolder.mView.findViewById(R.id.wallet);
//                    if(type.equals("Customers")){
//                        viewHolder.setUserImage(getRef(position).getKey());
//                        viewHolder.setTime(model.getNAME());
//                        viewHolder.setOrderNo(model.getMOBILE());
//                        viewHolder.setMobileNumber(model.getNAME());
//                        viewHolder.setWalletStatus(model.getUSER_ADDRESS());
//                        if (model.getWALLET()<0) {
//                            textView.setTextColor(context.getResources().getColor(R.color.red));
//                            viewHolder.setAmount("Pending"+getFormatedAmount(model.getWALLET()));
//                       }else viewHolder.setAmount(getFormatedAmount(model.getWALLET()));
//                    }else{
//                        viewHolder.setWalletStatus("Paid " +getFormatedAmount(model.getPAID_AMOUNT()));
//                        String time=getRef(position).getKey();
//                        viewHolder.setOrderNo(time);
//                        viewHolder.setUserImage(model.getUID());
//                        viewHolder.setTime(getFormatedTime(Long.parseLong(Objects.requireNonNull(time))));
//                        personalData(model.getUID(),viewHolder);
//                        if (type.equals("Orders")) {
//                            if (model.getPAID_AMOUNT() < model.getAMOUNT()) {
//                                textView.setTextColor(context.getResources().getColor(R.color.red));
//                                viewHolder.setAmount("Pending "+getFormatedAmount(model.getPAID_AMOUNT()));
//                            }else viewHolder.setAmount(getFormatedAmount(model.getPAID_AMOUNT()));
//                            viewHolder.setAmount(getFormatedAmount(model.getAMOUNT()));
//                        } else
//                            viewHolder.setAmount("Paid Via " + model.getPAID_VIA());
//                    }
//                    loadListener(model, viewHolder, position);
//
//                } catch (Exception ignored) {
//                }
//            }
//        }
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
                    intent.putExtra("2", model.getDATE());
                    intent.putExtra("3", String.valueOf(model.getQUANTITY()));
                    intent.putExtra("4", model.getNOTE());
                    intent.putExtra("5", String.valueOf(model.getAMOUNT()));
                    intent.putExtra("6", "" + model.getPAID_VIA());
                    intent.putExtra("7", String.valueOf(model.getPAID_AMOUNT()));

                }

                intent.putExtra("1", getRef(position).getKey());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }



        @Override
        public int getItemCount() {
            progressBar.setVisibility(View.VISIBLE);
            nothing_found.setVisibility(View.GONE);

             if(Totalcount==0 & isListLoaded){
                 progressBar.setVisibility(View.GONE);
                 nothing_found.setVisibility(View.VISIBLE);
             }
                if (isListLoaded)
                    progressBar.setVisibility(View.GONE);
                return super.getItemCount();
            }
        public Context getContext() {
            return context;
        }
    }