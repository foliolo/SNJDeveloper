package com.example.snjdeveloper.extra;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.snjdeveloper.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;

public class ColorSelectorFragment extends BottomSheetDialogFragment {
    TextInputEditText editText;
    Activity activity;
    int initialColor=0;
    private View colorViewer;
    private EditText colorValue;
    View invalidColorView;
    boolean isValidColor=true;
    public ColorSelectorFragment(TextInputEditText editText, Activity activity,String color) {
        this.editText = editText;
        this.activity=activity;
        if (!color.isEmpty())
            initialColor=Color.parseColor(color);
//        onViewCreated(null,null);
    }
//    ColorPickerView colorPickerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_color_selector,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      ColorPickerView colorPickerView= view.findViewById(R.id.colorPicker);
      invalidColorView=view.findViewById(R.id.invalidColor);
      if (initialColor!=0)
        colorPickerView.setInitialColor(initialColor);
      else colorPickerView.setInitialColor(ContextCompat.getColor(getActivity(),R.color.colorAccent));
      colorViewer=view.findViewById(R.id.color_viewer);
      colorValue=view.findViewById(R.id.colorValue);
      ColorObserver colorObserver= (color, fromUser, shouldPropagate) -> colorValue.setText(colorHex(color));
      colorPickerView.subscribe(colorObserver);
      view.findViewById(R.id.chooseBt).setOnClickListener(v -> {
          if (!isValidColor)
              showError();
          else {
              String color = colorValue.getText().toString().trim();
              editText.setText(color);
              dismiss();
          }
      });
      colorValue.addTextChangedListener(new TextWatcher() {         @Override          public void beforeTextChanged(CharSequence s, int start, int count, int after) {                        }          @Override          public void onTextChanged(CharSequence s, int start, int before, int count) {          }
          @Override
          public void afterTextChanged(Editable s) {
            try {
                if (!s.toString().isEmpty()){
                    int color=Color.parseColor(s.toString());
                    if (colorPickerView.getColor()!=color){
                        colorPickerView.unsubscribe(colorObserver);
                        colorPickerView.setInitialColor(color);
                        colorPickerView.subscribe(colorObserver);
                    }
                    colorViewer.setBackgroundColor(colorPickerView.getColor());
                    checkColorValidity();

                }
            }catch (Exception e){e.printStackTrace();
                showError();
            }
              Log.e("ColorValidity","->"+isValidColor);
          }
      });
    }
    private void showError(){
        isValidColor=false;
        invalidColorView.setVisibility(View.VISIBLE);
        colorValue.setTextColor(Color.RED);
        ObjectAnimator
                .ofFloat(invalidColorView, "translationX", 0, 25, -25, 25, -25,15, -15, 6, -6, 0)
                .setDuration(500)
                .start();
    }
    private void checkColorValidity() {
        if (!isValidColor){
            invalidColorView.setVisibility(View.GONE);
            colorValue.setTextColor(Color.BLACK);
            isValidColor=true;
        }
    }

    private String colorHex(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "#%02X%02X%02X%02X", a,r, g, b);
    }
    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog=(BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dia) {
                BottomSheetDialog dialog = (BottomSheetDialog) dia;
                FrameLayout bottomSheet =  dialog .findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(false);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return bottomSheetDialog;
    }

}
