package com.example.snjdeveloper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.snjdeveloper.newdata.NewOrderActivity;
import com.example.snjdeveloper.newdata.NewReturnBottlesActivity;
import com.example.snjdeveloper.newdata.NewTransactionActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Objects;

public class LoadRecentUsersFragment extends Fragment {
    private String type;
    private int maxRecentLimit=10;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler_layout, container, false);
        RecyclerView entryRecyclerView = v.findViewById(R.id.entry_recycler_view);
        type= getArguments() != null ? getArguments().getString("CLASS_NAME") : "";
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        ArrayList<String> data = loadDataFromSharedPref(getContext());
        if (data.size()>0) {
            requireActivity().findViewById(R.id.one).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.frame).setVisibility(View.VISIBLE);
            BottomSheetBehavior<CardView> bottomSheetBehavior;
            CardView addressCard = getActivity().findViewById(R.id.cardview);
            bottomSheetBehavior = BottomSheetBehavior.from(addressCard);
            bottomSheetBehavior.setPeekHeight(dpToPx(270));
        }
        entryRecyclerView.setLayoutManager(mLayoutManager);
        Log.e("AddedRecent",""+data);
        entryRecyclerView.setAdapter(new EntryItemAdapter(data));
        try {
            entryRecyclerView.scrollToPosition(data.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    public ArrayList<String> loadDataFromSharedPref(Context context) {
        SharedPreferences sh = context.
                getSharedPreferences("RECENT", Context.MODE_PRIVATE);


        return loadRecentData(sh);
    }

    private ArrayList<String> loadRecentData(SharedPreferences sh) {
        ArrayList<String> arr = new ArrayList<>();
        for (int i = 1; i < maxRecentLimit; i++) {
            String data = sh.getString(String.valueOf(i), null);
            if (data == null)
                break;
            arr.add(data);
        }
        return arr;
    }

    public void addNewRecent(String mobile, String uid,String name, Activity activity){
        SharedPreferences sh= activity.getSharedPreferences("RECENT",Context.MODE_PRIVATE);
        int pointer=sh.getInt("POINTER",1);
        //check if exist in shared pref
        int initial=0;//if data exist in pref it will not be zero

        if(sh.getString("1",null)==null){
            replaceWithNew(pointer, sh, mobile+","+name, uid);
        }else {
            for (int i = 1; i <= maxRecentLimit; i++) {
                String data = sh.getString(String.valueOf(i), null);
                if (data == null)
                    break;
                if (data.contains(uid))
                    initial = i;
            }
            if (initial != 0 && initial!=pointer-1)
                changePosition(initial, pointer, sh, mobile+","+name, uid);
            else if(initial==0)
                   replaceWithNew(pointer, sh, mobile+","+name, uid);

        }
    }
/*Add new recent contact into shared pref*/
    private void replaceWithNew(int pointer, SharedPreferences sh, String mobile, String uid) {
        SharedPreferences.Editor edit=sh.edit();
        edit.putString(String.valueOf(pointer),uid+","+mobile);
        if(pointer==maxRecentLimit)
            pointer=1;
        else pointer++;

        edit.putInt("POINTER",pointer);
        edit.apply();
    }
/*
* If the recent already exist in shred pref recent so we put the new one at the pointer position ie at last
* and we move the rest one step decrement ie data of 3 to 2 , data of 4 to 3 etc.
* */
    private void changePosition(int initial, int pointer,SharedPreferences sh,String mobile,String uid) {
        SharedPreferences.Editor edit=sh.edit();
        for(int i=initial;i<pointer-1;i++)
            edit.putString(String.valueOf(i), sh.getString(String.valueOf(i + 1), ""));

        edit.putString(String.valueOf(pointer-1),uid+","+mobile);
        edit.apply();

    }

    private class EntryItemAdapter extends RecyclerView.Adapter<EntryItemAdapter.EntryItemViewHolder> {
        private final ArrayList<String> entryModes;
        EntryItemAdapter(ArrayList<String> entryModes) {
            this.entryModes = entryModes;
        }

        @NonNull
        @Override
        public EntryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new EntryItemViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recent_user_layout, parent, false));
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
            private  ImageView imageView;
            EntryItemViewHolder(@NonNull View view) {
                super(view);
                mobile = view.findViewById(R.id.mobile);
                imageView= view.findViewById(R.id.user_img);
                name=view.findViewById(R.id.username);
            }

            void bindEntryMode(final String data, int position) {

                Constants.setBackgroundColor(itemView.findViewById(R.id.cardview), itemView.getContext(), position);
                String[] record =data.split(",");
                mobile.setText(record[1]);
                Log.e("AddedRecent","Recent loaded"+data);
                try {
                    name.setText(record[2]);
                }catch (Exception es){es.printStackTrace();}
                Glide.with(itemView).load("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F"+
                record[0]+"_pr"+"?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                        .apply(RequestOptions.circleCropTransform())
//                        .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        return false;
//                    }
//                })
                .into(imageView);
                imageView.setOnClickListener(view -> loadType(record[1],record[0]));
            }

        }
    }
    private void loadType(String mobile,String uid) {
        try {
            Intent intent = null;
            switch (type) {
                case "Order":
                    intent = new Intent(getActivity(), NewOrderActivity.class);
                    break;
                case "Transaction":
                    intent = new Intent(getActivity(), NewTransactionActivity.class);
                    break;
                case "Bottle":
                    intent = new Intent(getActivity(), NewReturnBottlesActivity.class);
            }
            Objects.requireNonNull(intent).putExtra("MOBILE", mobile);
            intent.putExtra("UID", uid);
            startActivity(intent);
        }catch (Exception e){e.printStackTrace();}
    }
}
