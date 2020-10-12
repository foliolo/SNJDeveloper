package com.example.snjdeveloper.RecyclerUI;

import android.content.Context;
import android.content.Intent;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecyclerUIBottle {

    private final SimpleDateFormat dateFormat;
    private Context context;
     private List<Model> list;
    private Calendar calendar;
    public RecyclerUIBottle(List<Model> list, Context context, RecyclerView recyclerView,
                            TextView costText, TextView totalBottleText,
                             String key, String type,long bottle,long returnbottle){
        this.context=context;

        FilterAdapter adapter = new FilterAdapter(list);
        dateFormat=new SimpleDateFormat("dd MMM yyyy", Locale.UK);
        calendar= Calendar.getInstance();
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        this.list=list;

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        totalBottleText.setText(String.format(Locale.UK,"Taken %d %s \nReturned %d", bottle, type,returnbottle));
        costText.setText(getFormatedAmount(bottle));
    }

    private String getFormatedAmount(long amount){
        return ""+ NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }
      class FilterAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
          List<Model> dataList;
          FilterAdapter(List<Model> data){
              this.dataList=data;
          }
          @NonNull
          @Override
          public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
             View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_layout, parent, false);

              return new ViewHolder.FoodViewHolder(view);
          }
          String getFormatedTime(long time){
              calendar.setTimeInMillis(time);
              return dateFormat.format(calendar.getTime());
          }
          @Override
          public void onBindViewHolder(@NonNull  ViewHolder.FoodViewHolder viewHolder, int position) {
              //set all data

              Model model=list.get(position);

                  try {viewHolder.setBottleImage(model.getUID());
                      viewHolder.setWalletStatus(getFormatedTime(model.getTIME()));
                      viewHolder.setOrderNo(String.valueOf(model.getTIME()));
                      personalData(model.getUID(),viewHolder);
                      TextView textView= viewHolder.mView.findViewById(R.id.amount);
                      //viewHolder.mView.findViewById(R.id.time).setVisibility(View.GONE);
                      String suffix="bottles";
                      if (model.getQUANTITY()==1)
                          suffix="bottle";
                          if(model.getPAID_VIA()==null) {
                              textView.setTextColor(context.getResources().getColor(R.color.green_light));
                              viewHolder.setAmount("Returned "+model.getQUANTITY()+" "+suffix);
                          }else{
                              textView.setTextColor(context.getResources().getColor(R.color.red));
                              viewHolder.setAmount("Taken "+model.getQUANTITY()+" "+suffix);
                          }


                          //viewHolder.setAmount(getFormatedAmount(model.getQUANTITY()));


                      loadListener(model,viewHolder,position);
                  }catch (Exception e){
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
                  intent.putExtra("1", String.valueOf(model.getTIME()));
                  intent.putExtra("2",String.valueOf (model.getTIME()));
                  intent.putExtra("3", String.valueOf(model.getQUANTITY()));
                  intent.putExtra("4",model.getNOTE());
                  intent.putExtra("5", String.valueOf(model.getAMOUNT()));
                  intent.putExtra("6",""+ model.getPAID_VIA());
                  intent.putExtra("7", String.valueOf( model.getPAID_AMOUNT()));
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
