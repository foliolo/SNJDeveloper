package com.example.snjdeveloper;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.extra.SimpleSliderAdapter;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;
import java.util.Objects;

public class ImageViewerActivity extends AppCompatActivity {
    private ImageView[] pointers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        setTitle("");
        loadImg();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;

    }

    private void loadImg() {
        String img = getIntent().getStringExtra("IMG");
        ArrayList<String>images=getIntent().getStringArrayListExtra("images");
        if (img == null&&images==null) {
            Toast.makeText(this, "No image found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (img!=null) {
            ZoomageView imageView = findViewById(R.id.imageView);
            Glide.with(this).load(img).into(imageView);
            findViewById(R.id.viewPager).setVisibility(View.GONE);
        }else{
            findViewById(R.id.imageView).setVisibility(View.GONE);
            SimpleSliderAdapter adapter=new SimpleSliderAdapter(images,this);
            ViewPager2 viewPager2=findViewById(R.id.viewPager);
            viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            viewPager2.setAdapter(adapter);
            createPointers(viewPager2,images);
        }
    }

    private void createPointers(ViewPager2 viewPager2, ArrayList<String> images) {
        pointers=null;
        if (images.size() > 1)
            pointers = createPointers( images.size());

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.e("Pager","Page change");
                try {
                    if (pointers != null)
                    changePointerColorOnPageChange(pointers, position);
                } catch (Exception e) {
                }
            }
        });
        }

    void changePointerColorOnPageChange(ImageView[] imageViews, int position) {
        try {
            ImageView previousImage;
            ImageView nextImage;
            if (position == 0)
                previousImage = null;
            else previousImage = imageViews[position - 1];
            if (position == imageViews.length - 1)
                nextImage = null;
            else nextImage = imageViews[position + 1];
            imageViews[position].setImageDrawable(getDrawable(R.drawable.dot_blue));
            if (previousImage != null)
                previousImage.setImageDrawable(getDrawable(R.drawable.dot_gray));
            if (nextImage != null)
                nextImage.setImageDrawable(
                        getDrawable(R.drawable.dot_gray));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    ImageView[] createPointers(int size) {
        ImageView[] imageViews = new ImageView[size];
        try {
            LinearLayout layout = findViewById(R.id.dynamic_content);
            for (int i = 0; i < size; i++) {
                ImageView imageView = new ImageView(this);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.dot));
                layout.addView(imageView);
                imageViews[i] = imageView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageViews[0].setImageDrawable(getResources().getDrawable(R.drawable.dot_blue));
        return imageViews;
    }
}
