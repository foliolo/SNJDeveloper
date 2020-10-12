package com.example.snjdeveloper;


import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.snjdeveloper.admin.CustomerInfoActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;


public class ViewHolder {
    public static class FoodViewHolder extends RecyclerView.ViewHolder{
        public View mView;

        public FoodViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setUserName(String Name) {
            try {
                TextView textView = mView.findViewById(R.id.username);
                textView.setText(Name);
            } catch (Exception ignored) {
            }

        }




        public void setMobileNumber(String MobileNumber){
            try {
                TextView textView=mView.findViewById(R.id.mobilenumber);
                textView.setText(MobileNumber);
            }catch (Exception ignored){}

        }



        public void setOrderNo(String order_no) {
            try {
                TextView textView=mView.findViewById(R.id.orderno);
                textView.setText(order_no);
            }catch (Exception ignored){}
        }

        public void setAmount(String amount) {
            try {
                TextView textView=mView.findViewById(R.id.amount);
                textView.setText(amount);
            }catch (Exception ignored){}
        }

      public   void setTime(String time) {
            try {
                TextView textView=mView.findViewById(R.id.time);
                textView.setText(time);
            }catch (Exception ignored){}
        }

        public    void setWalletStatus(String wallet_status) {
            try {
                TextView textView=mView.findViewById(R.id.wallet);
                textView.setText(wallet_status);
            }catch (Exception ignored){}
        }

        public void setUserImage(String uid) {
            Glide.with(itemView).load("https://firebasestorage.googleapis.com/v0/b/billrec." +
                    "appspot.com/o/files%2F"+
                    uid+"_pr?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                    .apply(RequestOptions.circleCropTransform()).error(R.drawable.transactions).into((ImageView) mView.findViewById(R.id.img1));
        }

        public void setBottleImage(String uid) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Glide.with(itemView).load("https://firebasestorage.googleapis.com/v0/b/billrec." +
                        "appspot.com/o/files%2F" +
                        uid + "_pr?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                        .apply(RequestOptions.circleCropTransform()).error(R.drawable.bottle_drawable).
                        into((ImageView) mView.findViewById(R.id.img1));
            } else {
                Glide.with(itemView).load("https://firebasestorage.googleapis.com/v0/b/billrec." +
                        "appspot.com/o/files%2F" +
                        uid + "_pr?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                        .apply(RequestOptions.circleCropTransform()).error(R.drawable.bottlefinal).
                        into((ImageView) mView.findViewById(R.id.img1));
            }
        }

        public void setName(String name) {
            ((TextView) itemView.findViewById(R.id.userTitle)).setText(name);
        }

        public void setMobile(String mobile) {
            ((TextView) itemView.findViewById(R.id.userDesc)).setText(mobile);
        }

        public void setImg(String id, String name, int position) {
            ImageView imageView = itemView.findViewById(R.id.userImage);
            Glide.with(itemView).load("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F" +
                    id + "_pr" + "?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d")
                    .apply(RequestOptions.circleCropTransform())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            imageView.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(imageView);
            setNameImage(name);
            Constants.setBackgroundColor(itemView.findViewById(R.id.cardview), itemView.getContext(), position);
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

        public void loadClick(String mobile, String uid) {
            itemView.setOnClickListener(v -> {
                Intent intent;
                intent = new Intent(itemView.getContext(), CustomerInfoActivity.class);
                intent.putExtra("0", "BarCode");
                intent.putExtra("UID", uid);
                intent.putExtra("MOBILE", mobile);
                itemView.getContext().startActivity(intent);

            });

        }


        public void setBackground(Object status, Context context) {
            View view = itemView.findViewById(R.id.parent);
            int color;
            if (status == null) {
                color = R.color.lightgray;
            } else {
                long s = (long) status;
                if (s == 0)
                    color = R.color.red;
                else if (s == 1) color = R.color.green_light;
                else color = R.color.lightgray;
            }
            view.setBackgroundColor(ContextCompat.getColor(context, color));
        }

        public void getUserData(boolean have_img, Context context, HashMap<String, Object> map, String uid, String key, int position) {
            if (have_img) {
                String link = "https://firebasestorage.googleapis.com/v0/b/billrec-transactions.appspot.com/o" +
                        "/Monthly_Bill_Reports%2F" + uid + "%2F" + key + ".jpeg?alt=media&token=d100f11b-eb01-4af1-bcff-20ebb700fe49";
                setData(context, link, null, null, null);
                map.put("img", link);
            }else map.put("img","");
            FirebaseDatabase.getInstance().getReference("Customers").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.e("Data Received","->"+snapshot.getValue());
                    if (snapshot.hasChildren()) {
                        map.put("name", snapshot.child("NAME").getValue());
                        map.put("mobile", snapshot.child("MOBILE").getValue());
                        setData(context, null, (String) snapshot.child("NAME").getValue(), uid, (String) snapshot.child("MOBILE").getValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        public void setData(Context context, String  img,String name,String uid,String mobile) {
            if (name!=null){
                TextView textView=itemView.findViewById(R.id.user);
                textView.setText(name);
                textView.setVisibility(View.VISIBLE);
                textView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                textView.setOnClickListener(v -> {
                    Intent intent=new Intent(context,CustomerInfoActivity.class);
                    intent.putExtra("0","");
                    intent.putExtra("UID",uid);
                    intent.putExtra("MOBILE",mobile);
                    context.startActivity(intent);
                });
            }
            if (img!=null&&!img.isEmpty()) {
                ImageView imageView = itemView.findViewById(R.id.image);
                Glide.with(itemView).load(img).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                imageView.setOnClickListener(v -> {
                    Intent intent=new Intent(context,ImageViewerActivity.class);
                    intent.putExtra("IMG",img);
                    context.startActivity(intent);
                });
            }//else itemView.findViewById(R.id.image).setVisibility(View.GONE);
        }

        public void getAllDataAndImg(boolean have_img, Context context, HashMap<String, Object> map, String uid, String key, int position) {
            getUserData(false,context,map,uid,null,position);
            if (!have_img)
                map.put("img","");
            else{itemView.findViewById(R.id.image).setVisibility(View.VISIBLE);
                StorageReference storageReference= new TransactionDb().getStorageReference(context).getReference("Problem").child(key);
                storageReference.listAll().addOnSuccessListener(listResult -> {
                    int size=listResult.getItems().size();
                    final int[] count = {0};
                    ArrayList<String>images=new ArrayList<>();
                    for (StorageReference st:listResult.getItems()){
                        st.getDownloadUrl().addOnSuccessListener(uri -> {
                            count[0]++;
                            Log.e("Url","->"+uri);
                            images.add(uri.toString());
                            if (count[0]==size)
                            {
                                map.put("images",images);
                                setImageListener(context,images);
                            }
                        });
                    }
                }).addOnFailureListener(e -> {

                });
            }
        }
        private void setImageListener(Context context, ArrayList<String> images) {
            ImageView imageView=itemView.findViewById(R.id.image);
            Glide.with(itemView).load(images.get(0)).into(imageView);
            imageView.setVisibility(View.VISIBLE);
            if (images.size()>1){
                itemView.findViewById(R.id.num_view).setVisibility(View.VISIBLE);
                ((TextView)itemView.findViewById(R.id.numText)).setText(""+images.size());
            }
            imageView.setOnClickListener(v -> {
                Intent intent=new Intent(context,ImageViewerActivity.class);
                intent.putExtra("images",images);
                context.startActivity(intent);
            });
        }
    }
    }




