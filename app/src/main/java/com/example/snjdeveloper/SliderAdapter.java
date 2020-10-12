package com.example.snjdeveloper;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class SliderAdapter extends PagerAdapter {
    private final ViewPager viewpager;
    private final ProgressBar progress;
    private Context mContext;
    private ArrayList<String> orginalkey;
    private ArrayList<String> mImage;
    private ImageView copy;
    private TextView title;
    private ArrayList<String> linkname;
    private int position;
    ImageView[] pointers;
    private FragmentTransaction fragmentTransaction;

    SliderAdapter(Context context, ViewPager viewPager, ImageView delete, TextView title,
                  LinearLayout linear, ProgressBar progressBar, View progress_frag,
                  FragmentTransaction fragment, View v) {
        mContext = context;
        orginalkey = new ArrayList<>();
        linkname = new ArrayList<>();
        mImage = new ArrayList<>();
        this.copy = delete;
        this.title = title;
        fragmentTransaction = fragment;
        this.viewpager = viewPager;
        this.progress = progressBar;
        extractDetails(viewPager, linear, progress_frag, v);

    }

    private void extractDetails(ViewPager viewPager, LinearLayout linear, View progress_frag, View v) {
        FirebaseDatabase.getInstance().getReference("Admin").child("QR").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progress_frag.setVisibility(View.GONE);
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String temp = (String) data.child("NAME").getValue();
                    orginalkey.add(temp);
                    Log.e("QRs", data.getKey());
                    linkname.add(data.getKey());
                    mImage.add("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/qr%2F" +
                            data.getKey() + "_qr.png?alt=media&token=479a3ebb-6ef0-4ef8-b111-3e30fa2efb");
                }
                linear.setVisibility(View.VISIBLE);
                try {
                    String first = orginalkey.get(0);
                    if (first.toLowerCase().contains("app") && first.toLowerCase().contains("customer"))
                        copy.setVisibility(View.INVISIBLE);
                    else copy.setVisibility(View.VISIBLE);
                    title.setText(first);
                    notifyDataSetChanged();
                    copy.setOnClickListener(view -> delete());
                    if (mImage.size() > 1)
                        pointers = createPointers(v, mImage.size());
                    else v.findViewById(R.id.dynamic_content).setVisibility(View.GONE);
                } catch (Exception ignored) {
                    copy.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                SliderAdapter.this.position = position;
                if (pointers != null)
                    changePointerColorOnPageChange(pointers, position);
                try {
                    String s = orginalkey.get(position);
                    if (s.toLowerCase().contains("app") && s.toLowerCase().contains("customer")) {
                        copy.setVisibility(View.INVISIBLE);
                        copy.setEnabled(false);
                        copy.setClickable(false);
                    } else {
                        copy.setVisibility(View.VISIBLE);
                        copy.setEnabled(true);
                        copy.setClickable(true);
                    }
                    title.setText(orginalkey.get(position));
                } catch (Exception e) {
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    ImageView[] createPointers(View v, int size) {
        ImageView[] imageViews = new ImageView[size];
        try {
            LinearLayout layout = v.findViewById(R.id.dynamic_content);
            for (int i = 0; i < size; i++) {
                ImageView imageView = new ImageView(mContext);
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.dot));
                layout.addView(imageView);
                imageViews[i] = imageView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageViews[0].setImageDrawable(mContext.getResources().getDrawable(R.drawable.dot_blue));
        return imageViews;
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
            imageViews[position].setImageDrawable(mContext.getDrawable(R.drawable.dot_blue));
            if (previousImage != null)
                previousImage.setImageDrawable(mContext
                        .getDrawable(R.drawable.dot_gray));
            if (nextImage != null)
                nextImage.setImageDrawable(mContext.
                        getDrawable(R.drawable.dot_gray));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void delete() {
        String s = linkname.get(position);
        Log.e("SlideImage", s);
        String url = "qr/" + s + "_qr.png";
        Log.e("IMG", url);
        int pos = position;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(url);
        progress.setVisibility(View.VISIBLE);
        copy.setEnabled(false);
        copy.setClickable(false);

        storageReference.delete().addOnSuccessListener(aVoid -> {
            FirebaseDatabase.getInstance().getReference("Admin/QR").child(s).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    try {
                        copy.setEnabled(true);
                        copy.setClickable(true);
                        title.setText("");
                        fragmentTransaction.replace(R.id.frame, new ImageSliderFragment());
                        fragmentTransaction.commit();
                    }catch (Exception e){e.printStackTrace();}
                    progress.setVisibility(View.GONE);
                }
            });


        }).addOnFailureListener(e -> {

            copy.setEnabled(true);
            copy.setClickable(true);
            progress.setVisibility(View.GONE);
            Toast.makeText(mContext,"Failed to delete "+s+" QR code",Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getCount() {
        return mImage.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }

    @NotNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);
        //imageView.setScaleType(ImageView.ScaleType.CENTER);
        String s = mImage.get(position);
        if (s.toLowerCase().contains("app") && s.toLowerCase().contains("customer"))
            Glide.with(mContext).load(s).diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).into(imageView);
        else
            Glide.with(mContext).load(s).into(imageView);

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ImageViewerActivity.class);
            intent.putExtra("IMG", s);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((ImageView) object);
    }
}