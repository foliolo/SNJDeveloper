package com.example.snjdeveloper.notification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ViewPagerInitializer {
    private  ArrayList<HashMap<String, Object>> htmlFilesList;
    private ProgressBar progressbar;
    ViewPager2 viewPager2;
    Context context;
    Button actionbt;
    HashMap<String,Object>selectedHtml;

    ViewPagerInitializer(ViewPager2 viewPager2, Context context, Button actionbt,
                         ProgressBar progressBar, HashMap<String, Object> selectedFile, WebView webView, View displayParentWebLayout, View displayImageView) {
        this.viewPager2 = viewPager2;
        this.context = context;
        this.actionbt = actionbt;
        this.selectedHtml = selectedFile;
        this.progressbar = progressBar;
        htmlFilesList = new ArrayList<>();
        actionbt.setOnClickListener(view -> {
            try {
                if (viewPager2.getAdapter() != null) {
                    int index = viewPager2.getCurrentItem();
                    Log.e("CurrentPage", "->" + index);
                    selectedHtml.clear();
                    selectedHtml.putAll(htmlFilesList.get(index));
                    String html = ((String) Objects.requireNonNull(selectedFile.get("HTML")));
                    if ((int) selectedHtml.get("STYLE") > NotificationAction.NOTIFICATION_DIALOG) {
                        String encode = Base64.encodeToString(html
                                .getBytes(), Base64.NO_PADDING);
                        webView.loadData(encode, "text/html", "base64");
                        webView.setVisibility(View.VISIBLE);
                    } else {
                        Glide.with(context).load(html).into((ImageView) displayImageView);
                        displayImageView.setVisibility(View.VISIBLE);
                    }
                    displayParentWebLayout.setVisibility(View.VISIBLE);
                    ((Activity) context).onBackPressed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loadFirebase();
    }

    private void loadFirebase() {
        FirebaseStorage.getInstance().getReference("Layouts").listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                progressbar.setVisibility(View.GONE);
                actionbt.setVisibility(View.VISIBLE);
                try {
                    if (task.isSuccessful()) {
                        ViewPagerAdapter adapter = initializeViewPager();
                        List<StorageReference> list = task.getResult().getItems();
                        if (list.isEmpty()) {
                            actionbt.setBackgroundColor(context.getResources().getColor(R.color.red));
                            actionbt.setText(R.string.no_layout_found);
                        } else
                            for (StorageReference reference : list) {
                                getLayoutData(reference, adapter);
                            }
                        Log.e("Files", "->" + task.getResult().getItems().get(0));
                    } else actionbt.setText("Failed to load layouts");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getLayoutData(StorageReference reference, ViewPagerAdapter adapter) {
        reference.getMetadata().addOnSuccessListener(storageMetadata -> {
            try {
                HashMap<String, Object> map = new HashMap<>();
                int style = storageMetadata.getCustomMetadata("STYLE") != null ?
                        Integer.parseInt(Objects.requireNonNull
                                (storageMetadata.getCustomMetadata("STYLE"))) : 1;
                map.put("STYLE", style);
                if (style > NotificationAction.NOTIFICATION_DIALOG) {
                    loadHTMLContent(storageMetadata, reference, map, adapter);
                } else {
                    map.put("ICON", storageMetadata.getCustomMetadata("ICON"));
                    map.put("IMG", storageMetadata.getCustomMetadata("IMG"));
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        map.put("HTML", uri.toString());
                        htmlFilesList.add(map);
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void loadHTMLContent(StorageMetadata storageMetadata, StorageReference reference, HashMap<String, Object> map, ViewPagerAdapter adapter) {
        try {
            String h_h = storageMetadata.getCustomMetadata("HTML_HEIGHT") != null ?
                    storageMetadata.getCustomMetadata("HTML_HEIGHT") : "400";
            String h_w = storageMetadata.getCustomMetadata("HTML_WIDTH") != null ? storageMetadata.
                    getCustomMetadata("HTML_WIDTH") : "400";
            String h = storageMetadata.getCustomMetadata("HEIGHT") != null ?
                    storageMetadata.getCustomMetadata("HEIGHT") : "400";
            String w = storageMetadata.getCustomMetadata("WIDTH") != null ? storageMetadata.
                    getCustomMetadata("WIDTH") : "400";
            map.put("HTML_HEIGHT", Integer.parseInt(h_h));
            map.put("HTML_WIDTH", Integer.parseInt(h_w));
            map.put("HEIGHT", Integer.parseInt(h));
            map.put("WIDTH", Integer.parseInt(w));
            reference.getStream().addOnSuccessListener(taskSnapshot -> {
                Log.e("HtmlFile", "->" + taskSnapshot.getStream());
                new RetrieveData(taskSnapshot, map, adapter).execute();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ViewPagerAdapter initializeViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(htmlFilesList);
        viewPager2.setAdapter(adapter);
        return adapter;
    }

    class RetrieveData extends AsyncTask<String, Void, String> {

        private final StreamDownloadTask.TaskSnapshot taskSnapshot;
        private final ViewPagerAdapter adapter;
        //        StorageReference reference;
        HashMap<String, Object> map;

        RetrieveData(StreamDownloadTask.TaskSnapshot taskSnapshot, HashMap<String, Object> map, ViewPagerAdapter adapter) {
            this.taskSnapshot = taskSnapshot;
//            this.reference=reference;
            this.map = map;
            this.adapter = adapter;
        }

        @Override
        protected String doInBackground(String... strings) {
            InputStreamReader reader = new InputStreamReader(taskSnapshot.getStream());
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder buffer = new StringBuilder();
            String str;
            while (true) try {
                str = bufferedReader.readLine();
                if (((str) == null)) break;
                buffer.append(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("PostExecute","html->"+s);
          try {
              if (s!=null) {
                  map.put("HTML", s);
                  htmlFilesList.add(map);
                  adapter.notifyDataSetChanged();

//                  reference.getMetadata().addOnSuccessListener(storageMetadata -> {
//
//                  });
              }
          }catch (Exception e){e.printStackTrace();}
        }
    }
    private int dpToPx(int dp) {
        Resources r =context. getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    class ViewPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<HashMap<String,Object>> list;

        ViewPagerAdapter(ArrayList<HashMap<String, Object>> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.webview_element, parent, false)) {
            };
        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
           try {

               HashMap<String, Object> map = list.get(position);
               if ((int) map.get("STYLE") > NotificationAction.NOTIFICATION_DIALOG) {
                   WebView webView = holder.itemView.findViewById(R.id.webview);
                   webView.getSettings().setJavaScriptEnabled(true);
                   Log.e("WebViewHolder", "->" + map);
                   if (map.get("HTML_HEIGHT") != null) {
                       webView.getLayoutParams().height = dpToPx((int) map.get("HTML_HEIGHT"));
                       webView.getLayoutParams().width = dpToPx((int) map.get("HTML_WIDTH"));
                   }
                   String html = ((String) Objects.requireNonNull(map.get("HTML")));
                   String encode = Base64.encodeToString(html
                           .getBytes(), Base64.NO_PADDING);
                   webView.loadData(encode, "text/html", "base64");
                   webView.setVisibility(View.VISIBLE);
               } else {
                   ImageView imageView = holder.itemView.findViewById(R.id.imageView);
                   imageView.setVisibility(View.VISIBLE);
                   Glide.with(holder.itemView).load(map.get("HTML")).into(imageView);
               }
           }catch (Exception e){e.printStackTrace();}
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
