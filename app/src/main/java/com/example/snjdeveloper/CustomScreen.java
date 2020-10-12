package com.example.snjdeveloper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.notification.NotificationAction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

public class CustomScreen {
    public CustomScreen(int screenName, Activity context, RecyclerView recyclerView) {
        fetchScreenDataFromRemoteConfig(context, screenName, recyclerView);
    }

    private void fetchScreenDataFromRemoteConfig(Activity context, int screenName, RecyclerView recyclerView) {
        FirebaseConfig config = new FirebaseConfig(context);
        config.fetch()
                .addOnCompleteListener(context, task -> {
                    // If is successful, activated fetched
                    try {
                        config.storeCacheExpiration(context, config.mFirebaseRemoteConfig.getLong(context.getString(R.string.fetch_interval)));
                        String customData = config.mFirebaseRemoteConfig.getString(context.getString(screenName));
                        Log.e("RemoteConfig", "Fetched->" + customData);
                        if (!customData.isEmpty())
                            initializeRecyclerAdapter(context, convertStringToHashMap(customData), recyclerView);
                        else Log.e("CustomScreen", "Not found");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void initializeRecyclerAdapter(Activity context, HashMap<String, HashMap<String, Object>> data, RecyclerView recyclerView) {
        if (data != null && !data.isEmpty()) {
            LinearLayoutManager mLayoutManager =
                    new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);
            recyclerView.setNestedScrollingEnabled(true);
            recyclerView.setLayoutManager(mLayoutManager);
            CustomScreenAdapter adapter = new CustomScreenAdapter(context, data);
            recyclerView.setAdapter(adapter);
        } else Log.e("CustomScreen", "Data not fond =->" + data);
    }

    private HashMap<String, HashMap<String, Object>> convertStringToHashMap(String customData) {
        HashMap<String, HashMap<String, Object>> data = null;
        try {
            data = new Gson().fromJson(customData, new TypeToken<HashMap<String, HashMap<String, Object>>>() {
            }.getType());
            Log.e("StringToHash", "Hashmap->" + data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    public static class CustomScreenAdapter extends RecyclerView.Adapter<ViewHolder> {
        Context context;
        HashMap<String, HashMap<String, Object>> data;

        public CustomScreenAdapter(Context context, HashMap<String, HashMap<String, Object>> data) {
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_items, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HashMap<String, Object> map = data.get("" + position);
            try {
                if (map != null) {
                    holder.setTitle((String) map.get("Title"));
                    holder.setDesc((String) map.get("Desc"));
                    holder.setListener((String) map.get("Title"), (String) map.get("Link"),map, context);
                } else holder.itemView.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
                holder.itemView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setTitle(String title) {
            ((TextView) itemView.findViewById(R.id.entry_title)).setText(title);
        }

        public void setDesc(String desc) {
            ((TextView) itemView.findViewById(R.id.entry_subtitle)).setText(desc);
        }

        public void setListener(String title, String link, HashMap<String, Object> map, Context context) {
            itemView.setOnClickListener(v -> {
                Class<?> aClass= (Class<?>) map.get("Class");
                if (aClass==null)
                    aClass=LoadWebViewActivity.class;
                Intent intent = new Intent(context, aClass);
                intent.putExtra("TITLE", title);
                intent.putExtra("HTML", link);
                new NotificationAction().addDataInIntent(map,intent);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            });
        }
    }
}
