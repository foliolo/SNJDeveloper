package com.example.snjdeveloper.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.snjdeveloper.R;

import java.util.Objects;

public class NewRecordActivity  extends AppCompatActivity {
    private TextView textView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);
         textView= (TextView) Objects.requireNonNull((View) findViewById(R.id.title));
        textView.setText("New");
        FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();

        try{ fragmentTransaction2.replace(R.id.frame,new EntryItemPage());
            fragmentTransaction2.commit();
        }catch (Exception e){e.printStackTrace();}

    }

    public void backOnClicked(View view) {
        finish();
    }




}
