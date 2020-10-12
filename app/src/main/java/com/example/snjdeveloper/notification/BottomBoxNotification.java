package com.example.snjdeveloper.notification;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.example.snjdeveloper.ViewHolder;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class BottomBoxNotification extends AppCompatActivity {

    private Dialog dialog;
    private ScrollView scrollView;
    ArrayList<HashMap<String, Object>> notificationData;
    private View progressbar;
    private Calendar calendar;
    private DateFormat dateFormat;
    private RecyclerView recyclerView;
    public boolean fragmentVisible=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_box_notification);
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.UK);
        calendar = Calendar.getInstance();
        progressbar = findViewById(R.id.progressbar);
        notificationData = new ArrayList<>();
        initializeRecycler();

    }


    private void initializeRecycler() {
        progressbar.setVisibility(View.VISIBLE);
        if (notificationData != null && recyclerView != null && recyclerView.getAdapter() != null) {
            notificationData.clear();
            recyclerView.getAdapter().notifyDataSetChanged();
        }
        loadData();
    }

    private void loadData() {
        new TransactionDb().getDatabaseReference(this).child("Notifications").orderByChild("FROM_ADMIN").equalTo(null).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressbar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    for (DataSnapshot d : snapshot.getChildren()) {
                        try {
                            HashMap<String, Object> map = (HashMap<String, Object>) d.getValue();
                            if (map != null) {
                                map.put("KEY", d.getKey());
                                notificationData.add(map);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    configureRecyclerview();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configureRecyclerview() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new Adapter(this, notificationData));
    }

    public void showHelpClicked(View view) {
        showInfoDialog(R.string.info_box_help_message, R.drawable.info_box_img);
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }

    void showInfoDialog(int stringid, int iconId) {
        setTheme(R.style.AppThemeBlack);
        if (dialog == null) {
            final View dialogView = View.inflate(this, R.layout.info_dialog, null);
            dialog = new Dialog(this, R.style.Dialog1);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(dialogView);
            dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
                if (i == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                }
                return false;
            });
            TextView textView = dialog.findViewById(R.id.help_msg);
            textView.setText(stringid);
            scrollView = dialog.findViewById(R.id.scrollView);
            Glide.with(dialogView).load(iconId).into((ImageView) dialogView.findViewById(R.id.icon));
            dialogView.findViewById(R.id.okBt).setOnClickListener(view -> dialog.dismiss());
            dialog.setOnDismissListener(dialog -> setTheme(R.style.AppTheme));
        } else {
            TextView textView = dialog.findViewById(R.id.help_msg);
            textView.setText(stringid);
            if (scrollView != null)
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            dialog.show();
            Glide.with(dialog.getContext()).load(iconId).into((ImageView) dialog.findViewById(R.id.icon));
        }
        dialog.show();
    }

    public void addNewNotificationClicked(View view) {
        Intent intent = new Intent(this, NewScheduledNotification.class);

        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(fragmentVisible){
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("");
            fragment.onActivityResult(requestCode, resultCode, data);
            Log.e("Data","->received");
        }else
        if (requestCode == 101 && resultCode == RESULT_OK ) {
            if ( data != null) {
                boolean isReload = data.getBooleanExtra("reload", false);
                if (isReload)
                    initializeRecycler();
            }
        }
    }

    String getFormatedTime(long time) {
        calendar.setTimeInMillis(time);
        return dateFormat.format(calendar.getTime());
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
        Context context;
        ArrayList<HashMap<String, Object>> data;
        DatabaseReference dbReference;

        Adapter(Context context, ArrayList<HashMap<String, Object>> data) {
            this.context = context;
            this.data = data;
            dbReference = new TransactionDb().getDatabaseReference(context).child("Notifications");
        }

        @NonNull
        @Override
        public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder.FoodViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.bottom_notification_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder holder, int position) {
            try {
                HashMap<String, Object> map = this.data.get(position);
                TextView titleText = holder.itemView.findViewById(R.id.title);
                titleText.setText((String) map.get("TITLE"));
                TextView descText = holder.itemView.findViewById(R.id.desc);
                descText.setText((String) map.get("DESC"));
                personalData((String) map.get("KEY"), holder, position);
                String img = (String) map.get("IMG");
                if (img != null) {
                    ImageView imageView = holder.itemView.findViewById(R.id.icon);
                    Glide.with(holder.itemView).load(img).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                }
                holder.itemView.findViewById(R.id.cancel).setOnClickListener(v -> {

                    try {
                        int index = data.indexOf(map);
                        data.remove(index);
                        notifyItemRemoved(index);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    String key = (String) map.get("KEY");
                    if (key != null)
                        dbReference.child(key).setValue(null);
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        BottomSheetDialogFragment fragment=new EditBottomInfoFragment(map);
                        fragment.show(getSupportFragmentManager(),"");
                        return false;
                    }
                });
                ((TextView) holder.itemView.findViewById(R.id.from_time)).setText(getFormatedTime((Long) map.get("FROM")));
                ((TextView) holder.itemView.findViewById(R.id.to_time)).setText(getFormatedTime((Long) map.get("TO")));
                String color = (String) map.get("COLOR");
                if (color != null)
                    setColor(color, holder.itemView.findViewById(R.id.info_card));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void setColor(String color, CardView cardView) {
            int c = Color.parseColor(color);
            cardView.setCardBackgroundColor(c);
        }
        private void personalData(String uid, ViewHolder.FoodViewHolder viewHolder, int position) {
            FirebaseDatabase.getInstance().getReference("Customers").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    viewHolder.setMobileNumber((String) dataSnapshot.child("NAME").getValue());
                    viewHolder.setImg(uid, (String) dataSnapshot.child("NAME").getValue(), position);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

}