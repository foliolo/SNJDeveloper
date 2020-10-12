package com.example.snjdeveloper.RecyclerUI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.Constants;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.extra.ColorSelectorFragment;
import com.example.snjdeveloper.extra.ImageSelectionOptionBottomSheet;
import com.example.snjdeveloper.extra.Parameters;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;

public class ReceivedNodeViewHolder extends RecyclerView.ViewHolder {
    TextInputEditText valueEdit;
    TextInputLayout   keyEdit;
    public ReceivedNodeViewHolder(@NonNull View itemView) {
        super(itemView);
        try {
            valueEdit= itemView.findViewById(R.id.edit_data_value);
        }catch (Exception e){}
        keyEdit= itemView.findViewById(R.id.edit_data);
    }

    public void setNodeKey(Object key, HashMap<String, Object> map) {

        if (key!=null)
        {
            keyEdit.setHint(""+key);
            map.put("key_edittext",keyEdit);
            if (key.toString().toLowerCase().equals("desc")){
                if (valueEdit!=null)
                {
                    valueEdit.setInputType(InputType.TYPE_CLASS_TEXT|  InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                    valueEdit.setMinHeight(330);
                }

            }
        }

    }

    public void setNodeValue(Object value, HashMap<String, Object> map) {
        String key=(String)map.get("key");
        if (value!=null)
        {
            if (key.equals("2")){
                valueEdit.setText(Constants.getFormattedTime(value));
            }else
            valueEdit.setText(""+value);
        }else valueEdit.setText("");

        if (TextUtils.isDigitsOnly(key))
        {   keyEdit.setEndIconMode(TextInputLayout.END_ICON_NONE); 
            valueEdit.setKeyListener(null);
        }
        map.put("value_edittext",valueEdit);

    }

    public void setImage(Object key, Object value, Context context,FragmentManager manager, int position, HashMap<String, Object> map) {
        ImageView imageView=itemView.findViewById(R.id.image);
        setNodeValue(value,map);
        setNodeKey(key, map);
        if (value!=null)
          Glide.with(context).load(value).into(imageView);
        imageView.setOnClickListener(v -> {
            BottomSheetDialogFragment sheet=new ImageSelectionOptionBottomSheet(position,"AppUpdate",101,
                    "image/*"
            );
            sheet.show(manager,"AppUpdate");
        });
        final String[] imgLink = new String[1];
        valueEdit.addTextChangedListener(new TextWatcher() {
            @Override            public void beforeTextChanged(CharSequence s, int start, int count, int after) {                            }            @Override            public void onTextChanged(CharSequence s, int start, int before, int count) {            }
            @Override
            public void afterTextChanged(Editable s) {
                imgLink[0] =s.toString();
                if (!s.toString().isEmpty()){
                    Glide.with(context).load(imgLink[0]).into(imageView);
                }
            }
        });
    }

    public void setHelperText(String required) {
        keyEdit.setHelperText(required);
    }

    public void setColorSelector(Object key, Object value, HashMap<String, Object> map, FragmentManager manager,Context context) {

        setNodeKey(key, map);
        View view=itemView.findViewById(R.id.color_display);
        final String[] color = new String[1];
        valueEdit.addTextChangedListener(new TextWatcher() {
            @Override            public void beforeTextChanged(CharSequence s, int start, int count, int after) {                            }            @Override            public void onTextChanged(CharSequence s, int start, int before, int count) {            }
            @Override
            public void afterTextChanged(Editable s) {
                color[0] =s.toString();
                if (!s.toString().isEmpty()){
                    try {
                        view.setBackgroundColor(Color.parseColor(color[0]));
                       if (keyEdit.isErrorEnabled())
                           keyEdit.setErrorEnabled(false);
                    }catch (Exception e){e.printStackTrace();
                      keyEdit.setError("invalid color");
                      keyEdit.requestFocus();

                    }
                }else {
                    if (keyEdit.isErrorEnabled())
                        keyEdit.setErrorEnabled(false);
                }
            }
        });
        setNodeValue(value,map);
        view.setOnClickListener(v -> {
            BottomSheetDialogFragment fragment=new ColorSelectorFragment(valueEdit, (Activity) context, color[0]);
            fragment.show(manager,"AppUpdate");
        });
    }

    public void hideView() {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = 0;
        itemView.setLayoutParams(params);
    }

    public void setMultiOptionView(Object key, Object value, Context context, HashMap<String, Object> map) {
        setNodeKey(key,map);
        AutoCompleteTextView autoCompleteTextView=itemView.findViewById(R.id.edit_data_value);
        if (key.equals("STYLE2")){
            String[] para=new Parameters().getAllBottomStyles();
            ArrayAdapter<String> adapter1=new ArrayAdapter<>(context,
                    R.layout.textview_layout,para);
            autoCompleteTextView.setAdapter(adapter1);
            int val;
            if ( value instanceof Long)
                val=((int)(long)value);
            else val=(int)value;
            if (val!=0){
                autoCompleteTextView.setText(para[val-1],false);
            }
            else autoCompleteTextView.setText(para[0],false);
            Log.e("Style2","Default "+value);
            map.put("value",1);
            autoCompleteTextView.setOnItemClickListener((parent, view, position1, id) -> map.put("value", position1+1));
        }else if (key.equals("Id")){//load all orderno of the user
            map.put("value_edittext",autoCompleteTextView);
            map.put("value_progressbar", itemView.findViewById(R.id.adapter_progressbar));
            Log.e("InfoData","Setting type "+map);
        }else if (key.equals("type")){
            ArrayAdapter<String> adapter1=new ArrayAdapter<>(context,
                    R.layout.textview_layout,new Parameters().getInfoTypeList());
            autoCompleteTextView.setAdapter(adapter1);
            map.put("value_edittext",autoCompleteTextView);
//            ((SendPopupNotificationActivity)context).infoIdInitializer(autoCompleteTextView);
        }
    }
}
