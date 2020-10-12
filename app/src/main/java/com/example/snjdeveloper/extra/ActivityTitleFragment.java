package com.example.snjdeveloper.extra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.snjdeveloper.R;

public class ActivityTitleFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.simple_header, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getTag()!=null)
        ((TextView)(view.findViewById(R.id.title))).setText(getTag());
        view.findViewById(R.id.back).setOnClickListener(v -> {
            if (getActivity()!=null)
            getActivity().onBackPressed();
        });
    }
}
