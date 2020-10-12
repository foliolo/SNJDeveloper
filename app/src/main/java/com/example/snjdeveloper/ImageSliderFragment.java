package com.example.snjdeveloper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import java.util.Objects;

public class ImageSliderFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_slider_layout, container, false);
        ViewPager viewPager = v.findViewById(R.id.viewPager);

        try {
            Objects.requireNonNull(getActivity()).findViewById(R.id.frag_progressbar).setVisibility(View.VISIBLE);
            FragmentTransaction fragmentTransaction2 = null;

            try{fragmentTransaction2=getActivity(). getSupportFragmentManager().beginTransaction();
            }catch (Exception ignored){}

            ImageView copy = Objects.requireNonNull(getActivity()).findViewById(R.id.copy);
            SliderAdapter adapter = new SliderAdapter(getActivity(), viewPager, copy,
                    getActivity().findViewById(R.id.qr_title),
                    getActivity().findViewById(R.id.mainLinear),
                    getActivity().findViewById(R.id.progressbar_qr),
                    getActivity().findViewById(R.id.frag_progressbar), fragmentTransaction2, v);
            viewPager.setAdapter(adapter);
        }catch (Exception ignored){}
        return v;
    }
}
