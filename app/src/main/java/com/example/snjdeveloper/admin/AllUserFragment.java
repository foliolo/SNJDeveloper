package com.example.snjdeveloper.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.RecyclerUI.RecyclerUI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
/*
    To send from activity to fragment
    Intent intent=getActivity().getIntent();
           Bundle bundle = new Bundle();
           Log.e("bottom_nav","URL=>"+intent.getStringExtra("URL"));
           bundle.putString("URL",intent.getStringExtra("URL"));
           WebFrame web = new WebFrame();
           web.setArguments(bundle);
 */
/*
*to receive from activity by fragment
*          url=this.getArguments().getString("URL");

 * */
public class AllUserFragment extends Fragment {

    private View progressBar;
    private String type;
    private RecyclerView recyclerView;
    private View nothing_found;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_all_user, container, false);
        Log.e("Customers","Loaded");
        recyclerView = v.findViewById(R.id.recycler_Chat);
        progressBar=  v.findViewById(R.id.progressbar);
        nothing_found=  v.findViewById(R.id.nothing_found);
        progressBar.setVisibility(View.GONE);
        type=this.getArguments() != null ? this.getArguments().getString("CLASS_NAME") : "";;
        Log.e("Type",type);

            searchBarListner();

        return v;
    }
    private void loadDatabase(String keyword){
    Query query=FirebaseDatabase.getInstance().
            getReference(type);
          query.orderByChild("TIME").
                  startAt(Long.parseLong(keyword)).limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("DataSnapshot",""+dataSnapshot);
                if (dataSnapshot.getChildrenCount()>0)
                new RecyclerUI(dataSnapshot,getContext(),recyclerView,dataSnapshot.getKey(),type);
                else
                { if(progressBar.getVisibility()==View.VISIBLE)
                {nothing_found.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


        private void searchBarListner(){
        EditText searchbar=Objects.requireNonNull(getActivity()). findViewById(R.id.searchEdit);
            searchbar.setHint("Search by "+type.substring(0,type.length()-1)+" number");
         searchbar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
             nothing_found.setVisibility(View.GONE);
             String key=editable.toString().trim();
             if (key.isEmpty()) {
                 recyclerView.setVisibility(View.GONE);
                 progressBar.setVisibility(View.GONE);
             }
             else {progressBar.setVisibility(View.VISIBLE);
                 recyclerView.setVisibility(View.VISIBLE);
                 loadDatabase(key);
             }
            Log.e("AllUser ","Search key changed =>"+key+" notifying to adapter");
            }
        });
    }


}
