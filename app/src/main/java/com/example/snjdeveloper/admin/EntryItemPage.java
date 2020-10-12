package com.example.snjdeveloper.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.qrcode.LiveBarcodeScanningActivity;

public class EntryItemPage extends Fragment {
    private String type;

    private enum EntryMode {
        ENTRY_ORDER(R.string.new_order, R.string.entry_order_subtitle),
        ENTRY_TRANSACTION(R.string.entry_transaction_title, R.string.entry_transaction_subtitle),
        ENTRY_BOTTLES(R.string.entry_bottle, R.string.entry_bottle_subtitle),
        ;

        private final int titleResId;
        private final int subtitleResId;

        EntryMode(int titleResId, int subtitleResId) {
            this.titleResId = titleResId;
            this.subtitleResId = subtitleResId;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler_layout, container, false);

        RecyclerView entryRecyclerView = v.findViewById(R.id.entry_recycler_view);
       // entryRecyclerView.setHasFixedSize(true);
        entryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        entryRecyclerView.setAdapter(new EntryItemAdapter(EntryMode.values()));


        return v;
    }

    private class EntryItemAdapter extends RecyclerView.Adapter<EntryItemAdapter.EntryItemViewHolder> {

        private final EntryMode[] entryModes;

        EntryItemAdapter(EntryMode[] entryModes) {
            this.entryModes = entryModes;
        }

        @NonNull
        @Override
        public EntryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new EntryItemViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.entry_items, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull EntryItemViewHolder entryItemViewHolder, int position) {
            entryItemViewHolder.bindEntryMode(entryModes[position]);
        }

        @Override
        public int getItemCount() {
            return entryModes.length;
        }

        private class EntryItemViewHolder extends RecyclerView.ViewHolder {

            private final TextView titleView;
            private final TextView subtitleView;

            EntryItemViewHolder(@NonNull View view) {
                super(view);
                titleView = view.findViewById(R.id.entry_title);
                subtitleView = view.findViewById(R.id.entry_subtitle);
            }

            void bindEntryMode(final EntryMode entryMode) {
                titleView.setText(entryMode.titleResId);
                subtitleView.setText(entryMode.subtitleResId);
                itemView.setOnClickListener(view -> {
                    Intent intent=new Intent(getActivity(), LiveBarcodeScanningActivity.class);
                    loadType(entryMode);
                    intent.putExtra("CLASS_NAME",type);
                    startActivity(intent);
                });
            }
        }
    }
    private void loadType(EntryMode entryMode) {
      //  TextView textView= Objects.requireNonNull(getActivity()).findViewById(R.id.title);
        switch (entryMode){
            case ENTRY_ORDER:
                //textView.setText("New Order");
                type="Order";
                break;
                //return new NewOrderFragment();
            case ENTRY_TRANSACTION:
                type="Transaction";
                break;
                //return new NewTransactionFragment();
            case ENTRY_BOTTLES:
                type="Bottle";
                break;
//                textView.setText("New Customer");
//                return new NewCustomerFragment();
        }
       // return null;
    }

}
