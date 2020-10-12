package com.example.snjdeveloper.notification;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ReceivedNodeAdapter;
import com.example.snjdeveloper.extra.Parameters;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class EditBottomInfoFragment extends BottomSheetDialogFragment {
    ArrayList<HashMap<String,Object>>data;
    private ReceivedNodeAdapter adapter;



    public EditBottomInfoFragment(HashMap<String, Object> map) {
       data= new Parameters().mapToList(map);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_info,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview_parameters);
        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        adapter=new ReceivedNodeAdapter(data,getActivity(),getParentFragmentManager());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        ((BottomBoxNotification)getActivity()).fragmentVisible=false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("AppUpdate","Inside activity result");
        if (data!=null) {
            if (requestCode==101 ) {
                int position = data.getIntExtra("EXTRA", -1);
                if (position != -1) {
                    Log.e("AppUpdate", "Received->" + position);
                    String uri = data.getStringExtra("DATA");
                    HashMap<String, Object> map = this.adapter.data.get(position);
                    map.put("value", uri);
                    Log.e("AppUpdate", "Setting received data\ndata->" + map);
                    adapter.notifyItemChanged(position);
                } else Log.e("Position is ", "" + position);
            }
        }

    }
    @NotNull
    @Override
    public Dialog  onCreateDialog(Bundle savedInstanceState) {
        ((BottomBoxNotification)getActivity()).fragmentVisible=true;
        BottomSheetDialog bottomSheetDialog=(BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dia -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dia;
            FrameLayout bottomSheet =  dialog .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet!=null)
            {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet).setHideable(false);
            BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return bottomSheetDialog;
    }
}
