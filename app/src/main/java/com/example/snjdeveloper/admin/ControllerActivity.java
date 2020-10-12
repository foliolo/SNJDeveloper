package com.example.snjdeveloper.admin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.snjdeveloper.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static com.example.snjdeveloper.Constants.searchKey;

public class ControllerActivity extends AppCompatActivity {
    static public int Totalcount=0;
    private String receive;
    private ChildEventListener listener;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_activity);
        Intent intent=getIntent();
         receive = intent.getStringExtra("CLASS_NAME");
        initiateRealtimeListener();
        Fragment fragmentClass = loadFragmentClassObject(receive);
        FragmentTransaction fragmentTransaction2;
        fragmentTransaction2= getSupportFragmentManager().beginTransaction();
       try{ Bundle bundle=new Bundle();
           bundle.putString("CLASS_NAME",receive);
            fragmentClass.setArguments(bundle);
           fragmentTransaction2.replace(R.id.frameBack, Objects.requireNonNull(fragmentClass));
        fragmentTransaction2.commit();
       }catch (Exception ignored){}
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            databaseReference.removeEventListener(listener);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            databaseReference.removeEventListener(listener);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            databaseReference.removeEventListener(listener);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void finish() {
        super.finish();
        try {
            databaseReference.removeEventListener(listener);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            databaseReference.addChildEventListener(listener);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            databaseReference.addChildEventListener(listener);
        }catch (Exception e){e.printStackTrace();}
    }

    void initiateRealtimeListener(){
        databaseReference= FirebaseDatabase.getInstance().getReference("Extra");
       listener= databaseReference.addChildEventListener(new ChildEventListener(){
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("Controller","Child added");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private Fragment loadFragmentClassObject(String receive) {
        TextView title=findViewById(R.id.title);

        try {


            switch (receive){
                case "Customers":
                    title.setText(getString(R.string.customers));
//                    findViewById(R.id.cancelsearcBar).setVisibility(View.VISIBLE);
                    return new AllUserFragment();
                case "Transactions":
                case "Orders":
                    findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
                    return new PaymentsFragment();
            }
        }catch (Exception e){e.printStackTrace();}
        return null;
    }


    public void searchuttonOnclick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation(true,view);
        }else{
            findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void animation(boolean show,View view) {
        final RelativeLayout cardView = findViewById(R.id.searchBar);

        int height = cardView.getHeight();
        int width = cardView.getWidth();
        int endRadius = (int) Math.hypot(width, height);
        int cx = (int) (view.getX());// + (cardView.getWidth()));
        int cy = (int) (view.getY());// + cardView.getHeight();

        if (show) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(cardView, cx, cy, cy, endRadius);
            revealAnimator.setDuration(400);
            revealAnimator.start();
            cardView.setVisibility(View.VISIBLE);
            // showZoomIn();
        } else {
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(cardView, cx, cy, cx, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    cardView.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(400);
            anim.start();
        }

    }

    public void endActivity(View view) {
        finish();
    }

    public void emptySearchbarClicked(View view) {
        EditText editText=findViewById(R.id.searchEdit);


        if (!editText.getText().toString().trim().isEmpty())
            editText.setText("");
        else endsearch(findViewById(R.id.searchBar));
        searchKey="";
    }

    public void endsearch(View view) {
        if(receive.equals("Customers")) {
            EditText editText = findViewById(R.id.searchEdit);
            if (!editText.getText().toString().trim().isEmpty())
                editText.setText("");
            searchKey = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animation(false, findViewById(R.id.search));
            } else {
                findViewById(R.id.searchBar).setVisibility(View.INVISIBLE);
            }
        }//else finish();
    }

    @Override
    public void onBackPressed() {

        if (receive.equals("Customers") && findViewById(R.id.searchBar).getVisibility()==View.VISIBLE){
            endsearch(findViewById(R.id.searchBar));
        }else
        {
            try {
                databaseReference.removeEventListener(listener);
            }catch (Exception e){e.printStackTrace();}
            super.onBackPressed();

        }

    }


    public void backOnClicked(View view) {
        finish();
    }

}
