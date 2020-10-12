package com.example.snjdeveloper.RecyclerUI;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class UserListAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {

    private ArrayList<HashMap<String, Object>> data;
    Context context;
    private ArrayList<String> registrationIDs;
    private ArrayList<String> displayList;
    TextView textView1;
    TextView textView2;
    int layout;

    public UserListAdapter(Context context, ArrayList<String> registrationIDs, ArrayList<String> displayList) {
        this.context = context;
        this.registrationIDs = registrationIDs;
        this.displayList = displayList;
        this.layout = R.layout.listview_layout;
    }

    public UserListAdapter(Context context, ArrayList<String> registrationIDs, ArrayList<String> displayList, TextView textView1, TextView textView2) {
        this.context = context;
        this.registrationIDs = registrationIDs;
        this.displayList = displayList;
        this.textView1 = textView1;
        this.textView2 = textView2;
        this.layout = R.layout.list_with_delete;
    }

    public UserListAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
        this.context = context;
        this.data = data;
        layout = R.layout.notification_item;
    }

    @NonNull
    @Override
    public com.example.snjdeveloper.ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new com.example.snjdeveloper.ViewHolder.FoodViewHolder(LayoutInflater.from(context)
                .inflate(layout, parent, false));
    }

//    @Override
//    public int getItemViewType(int position) {
//        HashMap<String, Object> map = data.get(position);
//        if (layout==R.layout.notification_item){
//            if (map.containsKey("have_img"))
//                return R.layout.notification_item_with_img;
//            else return layout;
//        }else return layout;
//
//    }

    @Override
    public void onBindViewHolder(@NonNull com.example.snjdeveloper.ViewHolder.FoodViewHolder holder, int position) {
        if (layout == R.layout.notification_item) {
            HashMap<String, Object> map = data.get(position);
            holder.setName((String) map.get("message"));
            holder.setBackground(map.get("status"), context);
            Date time = (Date) map.get("time");
            holder.setTime(time.toString().replace("GMT+05:30 ", ""));
            if (map.containsKey("have_img"))
            {
                Log.e("Adapter","Pos->"+position+"\n"+"->"+map);
                if (map.get("img")==null)
                holder.getUserData((boolean)map.containsKey("have_img"),context,map,(String) map.get("uid"),(String) map.get("key"),position);
                else holder.setData(context,(String) map.get("img"),(String) map.get("name"),(String)map.get("uid"),(String)map.get("mobile"));
            }

        } else {
            String[] data = displayList.get(position).split(",");
            holder.setImg(data[0], data[2], position);
            holder.setName(data[2]);
            holder.setMobile(data[3]);
            if (layout == R.layout.list_with_delete) {
                holder.itemView.findViewById(R.id.delBt).setOnClickListener(v -> {
                    holder.itemView.findViewById(R.id.delProgress).setVisibility(View.VISIBLE);
                    FirebaseDatabase.getInstance().getReference("Customers").child(data[0]).child("RECEIVE_MONTHLY_PAYMENT").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            holder.itemView.findViewById(R.id.delProgress).setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                displayList.remove(position);
                                registrationIDs.remove(data[0]);
                                notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "Failed to delete " + data[2] + ". Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                });
            }
        }
    }

    private void showMessageTextView() {
        textView1.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);
    }

    private void hideMessageTextView() {
        textView1.setVisibility(View.GONE);
        textView2.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (textView1 != null) {
            if (displayList.isEmpty())
                hideMessageTextView();
            else showMessageTextView();
        }
        if (data == null)
            return displayList.size();
        else return data.size();
    }


}
