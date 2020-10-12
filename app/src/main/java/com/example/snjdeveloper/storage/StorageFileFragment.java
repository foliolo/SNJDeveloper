package com.example.snjdeveloper.storage;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.AdvanceViewHolder;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.extra.Utility;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class StorageFileFragment extends Fragment implements AdvanceViewHolder.ItemClickListener{
    private  String[] dbList;
    StorageReference reference;
    private ImageAdapter adapter;
    private String folderName;
    ArrayList<String>files;
    private RecyclerView recyclerView;

    public StorageFileFragment(StorageReference reference, String folderName) {
        this.reference = reference;
        this.folderName = folderName;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView=view.findViewById(R.id.entry_recycler_view);
        files=new ArrayList<>();
        loadIconsToSelect();
        getView().findViewById(R.id.entry_progressbar).setVisibility(View.VISIBLE);
        if (!folderName.isEmpty())
            getActivity().setTitle(folderName);

    }
    private void loadIconsToSelect() {
        configureRecyclerview();
        reference.listAll().addOnSuccessListener(listResult -> {
            int size=listResult.getItems().size();
            Log.e("Items","->"+listResult.getItems());
            final int[] count = {0};
            for (StorageReference st:listResult.getItems()){
                st.getDownloadUrl().addOnSuccessListener(uri -> {
                    count[0]++;
                    Log.e("Url","->"+uri);
                    files.add(uri.toString());
                    if (count[0]==size)
                    {
                        getView().findViewById(R.id.entry_progressbar).setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }

                });
            }
        });
    }

    private void configureRecyclerview() {
        adapter=new ImageAdapter(getContext(),files,R.layout.image);
        int column;
        if (folderName.toLowerCase().contains("app"))
            column=1;
        else column= Utility.calculateNoOfColumns(getContext(),150);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), column));
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.e("Item Click","clicked at"+position);
    }
}
