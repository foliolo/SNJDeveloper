package com.example.snjdeveloper.storage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.AdvanceViewHolder;
import com.example.snjdeveloper.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<AdvanceViewHolder> {
    Context context;
    ArrayList<String> list;
    int layout;
    private AdvanceViewHolder.ItemClickListener mClickListener;

    public ImageAdapter(Context context, ArrayList<String> list, int layout) {
        this.context = context;
        this.list = list;
        this.layout = layout;
    }

    @NonNull
    @Override
    public AdvanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view_big_adapter, parent, false);
        return new AdvanceViewHolder(view,mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdvanceViewHolder holder, int position) {
        String img=list.get(position);
        ImageView imageView=(ImageView)holder.itemView.findViewById(R.id.image);
        if (img!=null &&!img.contains(".apk")||!img.contains("."))
            Glide.with(holder.itemView).load(list.get(position)).into(imageView);
        else{
            Glide.with(holder.itemView).load(R.drawable.ic_folder).into(imageView);
            String fileName=getFileName(img);
            TextView textView=((TextView)holder.itemView.findViewById(R.id.fileName));
            textView.setText(fileName);
            textView.setVisibility(View.VISIBLE);
            setLayoutHeight(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
   public void setClickListener(AdvanceViewHolder.ItemClickListener itemClickListener) {
        mClickListener = itemClickListener;
    }
    private void setLayoutHeight(View itemView) {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = 130;
        itemView.setLayoutParams(params);
    }

    private String getFileName(String img) {
        if (img==null)
            return "";
        if (img.contains("%2F"))
            img=img.substring(img.lastIndexOf("%2F")+3,img.indexOf("?"));
        else img=img.substring(img.lastIndexOf("/")+1,img.indexOf("?"));
        return img;
    }
}

