package com.example.snjdeveloper;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class AdvanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final ItemClickListener  clickListener;

    public AdvanceViewHolder(View itemView, ItemClickListener clickListener) {
        super(itemView);
        this.clickListener=clickListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (clickListener != null) clickListener.onItemClick(view, getAbsoluteAdapterPosition());
    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}


