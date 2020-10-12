package com.example.snjdeveloper.extra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ViewHolder;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;

public class SimpleSliderAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {

private final Context context;
        ArrayList<String>images;
public SimpleSliderAdapter(ArrayList<String>images, Context context){
        this.images=images;
        this.context=context;
        }

    @NonNull
    @Override
    public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder.FoodViewHolder( LayoutInflater.from(context).inflate(R.layout.zoomage_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder holder, int position) {
            ZoomageView imageView= holder.itemView.findViewById(R.id.zoomage);
            Glide.with(holder.itemView).load(images.get(position)).into(imageView);
    }

    @Override
    public int getItemCount() {
            return images.size();
    }
}
