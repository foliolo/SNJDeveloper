package com.example.snjdeveloper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.RecyclerUI.ReceivedNodeViewHolder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;

public class ReceivedNodeAdapter extends RecyclerView.Adapter<ReceivedNodeViewHolder> {
    private final FragmentManager manager;
    int image_adapter_layout = R.layout.image_adapter_layout;
    int input_text_layout = R.layout.input_text_layout;
    int color_adapter_layout = R.layout.color_adapter_layout;
    int multi_option_adapter_layout = R.layout.multi_option_adapter_layout;
    int hidden_text_input_layout = R.layout.hidden_text_input_layout;

    public ArrayList<HashMap<String,Object>> data;
    Context context;

    public ReceivedNodeAdapter(ArrayList<HashMap<String, Object>> data, Context context, FragmentManager manager) {
        this.data = data;
        this.context = context;
        this.manager=manager;
    }

    @Override
    public int getItemViewType(int position) {
        String key=data.get(position).get("key").toString().toLowerCase();
        if (data.get(position).get("isImg")!=null||key.equals("img")||key.equals("icon"))
            return image_adapter_layout;
        else if (key.contains("color")) {
            return color_adapter_layout;
        }else if (key.equals("style2")||key.equals("type")) {
            return multi_option_adapter_layout;
        }else if (key.equals("style")||key.equals("action")||key.equals("intent")|| key.equals("1")) {
            return hidden_text_input_layout;
        }
        return input_text_layout;
    }
    @NonNull
    @Override
    public ReceivedNodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ReceivedNodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceivedNodeViewHolder holder, int position) {
        HashMap<String,Object>map=data.get(position);
        Object key;
        if (map.containsKey("temp_key"))
            key=map.get("temp_key");
        else key =map.get("key");
        Object value=map.get("value");
        if (map.containsKey("value_edditext")&&value==null){
            Object valueView=map.get("value_edittext");
          try {
              if (valueView!=null) {
                  if (valueView instanceof AutoCompleteTextView)
                      value = ((AutoCompleteTextView) valueView).getText().toString();
                  else value = ((TextInputEditText) valueView).getText().toString();
              }
          }catch (Exception ignored){}
        }
        if (getItemViewType(position)==color_adapter_layout)
            holder.setColorSelector(key,value,map, manager,context);
        else   if (getItemViewType(position) == image_adapter_layout)
            holder.setImage(key, value, context,manager, position, map);
        else if (getItemViewType(position)==multi_option_adapter_layout)
            holder.setMultiOptionView(key, value, context, map);
        else{
            holder.setNodeValue(value, map);
            holder.setNodeKey(key, map);
        }
        Log.e("Key","->"+key);
//        if (key!=null&&(key.equals("STYLE")||key.equals("ACTION")||key.equals("intent")||key.toString().toLowerCase().equals("id")))
//        {   Log.e("Hiding","hiding key "+key+" data->"+map);
//            holder.hideView();
//        }
//        else Log.e("Hiding","Not hiding key "+key);
        if (map.containsKey("required"))
            holder.setHelperText("required");

    }

    private void setRemoveListener(View itemView, HashMap<String, Object> map){
        itemView.findViewById(R.id.cancel).setOnClickListener(v -> {
            int index=data.indexOf(map);
            data.remove(map);
            notifyItemRemoved(index);
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }
}
