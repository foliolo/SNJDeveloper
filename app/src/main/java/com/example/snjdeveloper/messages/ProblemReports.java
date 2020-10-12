package com.example.snjdeveloper.messages;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.CustomScreen;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.notification.SavedNotificationActivity;

import java.util.HashMap;

public class ProblemReports extends AppCompatActivity {
    String[] title={"App Reports","Bill reports"};
    String[] desc={"Problems about the app functionality","Reports about the billing issues"};
    Class<?>[]aClass={GeneralProblemActivity.class,SavedNotificationActivity.class};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.problem_report_activity);
        CustomScreen.CustomScreenAdapter adapter=new CustomScreen.CustomScreenAdapter(this,setData());
        RecyclerView recyclerView=findViewById(R.id.recyclerview);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private HashMap<String, HashMap<String, Object>> setData() {
        HashMap<String, HashMap<String, Object>> data=new HashMap<>();
        for (int i=0;i<title.length;i++){
            HashMap<String,Object>map=new HashMap<>();
            map.put("Title",title[i]);
            map.put("Desc",desc[i]);
            map.put("Class",aClass[i]);
            map.put("NodeName","Bill Problem Reports");
            data.put(""+i,map);
        }
        return data;
    }
}
