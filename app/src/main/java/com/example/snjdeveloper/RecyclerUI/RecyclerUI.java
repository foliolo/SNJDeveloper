package com.example.snjdeveloper.RecyclerUI;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.InfoActivity;
import com.example.snjdeveloper.Model;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecyclerUI {

    private final SimpleDateFormat dateFormat;
    private   FilterAdapter adapter;
    private Context context;
     private List<Model> list;
    private String type;
    private Calendar calendar;

    public RecyclerUI(DataSnapshot dataSnapshot, Context context, RecyclerView recyclerView,
                      TextView costText, TextView totalTripText, String key, String type){
         this.context=context;
         list=new ArrayList<>();
        this.type=type;
        adapter= new FilterAdapter(list);
        dateFormat=new SimpleDateFormat("dd MMM yyyy", Locale.UK);
        calendar= Calendar.getInstance();
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
      try {
          recyclerView.scrollToPosition((int) (dataSnapshot.getChildrenCount()-1));

      }catch (Exception e){e.printStackTrace();}
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(adapter);
        totalTripText.setText(String.format(Locale.UK,"From %d %s", dataSnapshot.getChildrenCount(), type));
       try {
           setRecyclerUI(dataSnapshot,costText);
       }catch (Exception e ){}

      }

    public RecyclerUI(DataSnapshot dataSnapshot, Context context, RecyclerView recyclerView, String key, String type) {
        this.context=context;
        list=new ArrayList<>();
        this.type=type;
        adapter= new FilterAdapter(list);
        dateFormat=new SimpleDateFormat("dd MMM yyyy", Locale.UK);
        calendar= Calendar.getInstance();
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        try {
            recyclerView.scrollToPosition((int) (dataSnapshot.getChildrenCount()-1));

        }catch (Exception e){e.printStackTrace();}
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(adapter);
        setRecyclerUIForSearching(dataSnapshot);

    }
    private void setRecyclerUIForSearching(DataSnapshot dataSnapshot){
        for (DataSnapshot dataSnapshot2:dataSnapshot.getChildren())
        {
            Model  model=dataSnapshot2.getValue(Model.class);
            list.add(model);
            adapter.notifyDataSetChanged();
        }
    }

    private void setRecyclerUI(DataSnapshot dataSnapshot, TextView costText){
        long cost=0;
            for (DataSnapshot dataSnapshot2:dataSnapshot.getChildren())
            {
                Model  model=dataSnapshot2.getValue(Model.class);
                if (model != null) {
                    if (type.equals("Bottles"))
                        cost+=model.getQUANTITY();
                    else
                    cost+=model.getPAID_AMOUNT();
                }
                 list.add(model);
                 adapter.notifyDataSetChanged();
            }
            costText.setText(getFormatedAmount(cost));
        }
    private String getFormatedAmount(long amount){
        return "₹ "+ NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }
      class FilterAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
          List<Model> dataList;
          FilterAdapter(List<Model> data){
              this.dataList=data;
          }
          @NonNull
          @Override
          public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
             View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_adapter_layout, parent, false);

              return new ViewHolder.FoodViewHolder(view);
          }
          String getFormatedTime(long time){
              calendar.setTimeInMillis(time);
              return dateFormat.format(calendar.getTime());
          }
          @Override
          public void onBindViewHolder(@NonNull  ViewHolder.FoodViewHolder viewHolder, int position) {
              //set all data
              Log.e("Recycle", "Inside onbindviewholder");
              Model model = list.get(position);
//                   Log.e("Recycle","setting time "+model.getTIME());
              try {
                  viewHolder.setUserImage(model.getUID());
//                      viewHolder.setTime(getFormatedTime(model.getTIME()));
                  viewHolder.setTime(getFormatedTime(Long.parseLong(model.getDATE())));
                  viewHolder.setWalletStatus("Paid " + getFormatedAmount(model.getPAID_AMOUNT()));
//                      viewHolder.setOrderNo(String.valueOf(model.getTIME()));
                  viewHolder.setOrderNo(model.getDATE());
                  personalData(model.getUID(), viewHolder);
                  if (type.equals("Orders")) {
                      TextView textView = viewHolder.mView.findViewById(R.id.wallet);
                      if (model.getPAID_AMOUNT() < model.getAMOUNT()) {
                          textView.setTextColor(context.getResources().getColor(R.color.red));
                      } else
                          textView.setTextColor(context.getResources().getColor(R.color.green_light));

                      viewHolder.setAmount(getFormatedAmount(model.getAMOUNT()));

                  } else viewHolder.setAmount("Paid Via " + model.getPAID_VIA());
                  loadListener(model, viewHolder, position);
              } catch (Exception e) {
                  e.printStackTrace();
                  }

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

          private void loadListener(Model model, final ViewHolder.FoodViewHolder viewHolder, int position){
              viewHolder.itemView.setOnClickListener(view -> {
                  Intent intent;
                  intent = new Intent(context, InfoActivity.class);
//                  intent.putExtra("1", String.valueOf(model.getTIME()));
                  intent.putExtra("1", model.getDATE());
//                  intent.putExtra("2",String.valueOf (model.getTIME()));
                  intent.putExtra("2", model.getDATE());
                  intent.putExtra("3", String.valueOf(model.getQUANTITY()));
                  intent.putExtra("4", model.getNOTE());
                  intent.putExtra("5", String.valueOf(model.getAMOUNT()));
                  intent.putExtra("6", "" + model.getPAID_VIA());
                  intent.putExtra("7", String.valueOf(model.getPAID_AMOUNT()));
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  context.startActivity(intent);
              });
          }

          @Override
          public int getItemCount() {
              if (dataList==null)
                  return 0;
              else
                  return dataList.size();
          }
      }
    }
