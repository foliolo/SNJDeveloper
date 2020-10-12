package com.example.snjdeveloper.extra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.ServerImagesManagement;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ImageSelectionOptionBottomSheet extends BottomSheetDialogFragment {
    private final String type;
    private final String from;
    private final int data;
    private final int reqCode;

    public ImageSelectionOptionBottomSheet(int data, String from,int reqCode, String type) {
        this.type=type;
        this.from=from;
        this.data=data;
        this.reqCode=reqCode;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_selection_option,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.server).setOnClickListener(v -> {
            Intent intent=new Intent(getActivity(), ServerImagesManagement.class);
            if (from.toLowerCase().contains("update"))
            {   intent.putExtra("FOLDER_NAME","extra");
                intent.putExtra("DB_NAME","test1-b0b2c");
            }else if (from.toLowerCase().contains("apk")) {
                intent.putExtra("FOLDER_NAME", "apps");
            }
            intent.putExtra("DATA", data);
            ((Activity)getActivity()).startActivityForResult(intent,reqCode);
            dismiss();

        });
        view.findViewById(R.id.storage).setOnClickListener(v -> {
            Intent intent=new Intent(getActivity(), FileSelector.class);
            intent.putExtra("DATA",data);
            intent.putExtra("FROM",from);
            intent.putExtra("TYPE", type);
            if (reqCode!=-1)
                intent.putExtra("RequestCode", reqCode);
            ((Activity)getActivity()).startActivityForResult(intent, reqCode);
            dismiss();
        });
    }
}
