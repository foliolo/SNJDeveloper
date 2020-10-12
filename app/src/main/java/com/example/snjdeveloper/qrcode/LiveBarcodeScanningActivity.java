/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.snjdeveloper.qrcode;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.snjdeveloper.InfoActivity;
import com.example.snjdeveloper.LoadRecentUsersFragment;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.admin.CustomerInfoActivity;
import com.example.snjdeveloper.barcodedetection.BarcodeProcessor;
import com.example.snjdeveloper.barcodedetection.BarcodeResultFragment;
import com.example.snjdeveloper.camera.CameraSource;
import com.example.snjdeveloper.camera.CameraSourcePreview;
import com.example.snjdeveloper.camera.GraphicOverlay;
import com.example.snjdeveloper.camera.WorkflowModel;
import com.example.snjdeveloper.camera.WorkflowModel.WorkflowState;
import com.example.snjdeveloper.newdata.NewOrderActivity;
import com.example.snjdeveloper.newdata.NewReturnBottlesActivity;
import com.example.snjdeveloper.newdata.NewTransactionActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.common.base.Objects;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//import com.example.snjdeveloper.settings.SettingsActivity;

/**
 * Demonstrates the barcode scanning workflow using camera preview.
 */
public class LiveBarcodeScanningActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "LiveBarcodeActivity";

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private View flashButton;
    private Chip promptChip;
    private AnimatorSet promptChipAnimator;
    private WorkflowModel workflowModel;
    private WorkflowState currentWorkflowState;
    private String type;
    private BottomSheetBehavior<CardView> bottomSheetBehavior;
    private View cancel;
    private EditText mobileEdit;
    private View progressBar;
    private View nothing_found;
    private String searchKey;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> mobile;
    private HashMap<String, String> uid;

    private Runnable input_finish_checker;
    private Handler handler = new Handler();
    private long delay = 1000;
    private long last_text_edit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_barcode);
        preview = findViewById(R.id.camera_preview);
        graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay);
        graphicOverlay.setOnClickListener(this);
        cameraSource = new CameraSource(graphicOverlay);

        promptChip = findViewById(R.id.bottom_prompt_chip);
        promptChipAnimator =
                (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter);
        promptChipAnimator.setTarget(promptChip);

        findViewById(R.id.close_button).setOnClickListener(this);
        flashButton = findViewById(R.id.flash_button);
        flashButton.setOnClickListener(this);

    receive();
    setUpWorkflowModel();
      Fragment fragmentClass =new LoadRecentUsersFragment();
      FragmentTransaction fragmentTransaction2;
      fragmentTransaction2= getSupportFragmentManager().beginTransaction();
      try{  Bundle bundle=new Bundle();
            bundle.putString("CLASS_NAME",type);
            fragmentClass.setArguments(bundle);
          fragmentTransaction2.replace(R.id.frame, java.util.Objects.requireNonNull(fragmentClass));
          fragmentTransaction2.commit();
      }catch (Exception ignored){}

  }

  private void receive() {
    Intent intent=getIntent();
     type=intent.getStringExtra("CLASS_NAME");
    if(type==null){
      findViewById(R.id.cardview).setVisibility(View.GONE);
    }else{
        progressBar=findViewById(R.id.progressbar);
        mobileEdit=findViewById(R.id.mobileEdit);
        cancel=findViewById(R.id.cancel);
        CardView addressCard = findViewById(R.id.cardview);
        bottomSheetBehavior=BottomSheetBehavior.from(addressCard);
        nothing_found=findViewById(R.id.nothing_found);

        loadMobileSearchDialog();
    }
  }
    public void cancelOnClick(View view) {
        mobileEdit.setEnabled(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        flashButton.setEnabled(true);
        findViewById(R.id.mobileText).setVisibility(View.VISIBLE);
        view.setVisibility(View.INVISIBLE);

        mobileEdit.setHint("");
        cancel.setVisibility(View.INVISIBLE);

    }
  void loadMobileSearchDialog(){
    TextView textView=findViewById(R.id.mobileText);
    textView.setOnClickListener(view -> {
        Log.e("LiveBarcode","CLicked");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        flashButton.setEnabled(false);
        textView.setVisibility(View.GONE);
        findViewById(R.id.cancel).setVisibility(View.VISIBLE);
    });
    bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState==BottomSheetBehavior.STATE_EXPANDED){
            flashButton.setEnabled(false);
            mobileEdit.setHint(R.string.enter_mobile_number);
            cancel.setVisibility(View.VISIBLE);
            mobileEdit.setEnabled(true);
            mobileEdit.setText("");
            textView.setVisibility(View.GONE);

        }else{
            mobileEdit.setText("");
            flashButton.setEnabled(true);
            mobileEdit.setEnabled(false);
            mobileEdit.setHint("");
            cancel.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        }
      }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    });
      firebase();
  }

    void firebase() {
        mobile = new ArrayList<>();
        uid = new HashMap<>();
        initializeSearch();
        ListView listView = findViewById(R.id.listview);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1
                , android.R.id.text1, mobile);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            try {
                Intent intent;
                if (type.contains("Order"))
                    intent = new Intent(LiveBarcodeScanningActivity.this, NewOrderActivity.class);
                else if (type.contains("Bottle"))
                    intent = new Intent(LiveBarcodeScanningActivity.this, NewReturnBottlesActivity.class);
                else
                    intent = new Intent(LiveBarcodeScanningActivity.this, NewTransactionActivity.class);

                intent.putExtra("MOBILE", mobile.get(i));
                intent.putExtra("UID", uid.get(mobile.get(i)));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LiveBarcodeScanningActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("Customers");
        nothing_found.setVisibility(View.GONE);
        mobileEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("CameraDB", "Loading db");
                searchKey = editable.toString();
                mobile.clear();
                uid.clear();
                nothing_found.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                if (!searchKey.isEmpty()) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
                } else {
                    adapter.clear();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initializeSearch() {
        input_finish_checker = () -> {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500) && !searchKey.isEmpty())
                getDataFromDB(searchKey);
            else {
                progressBar.setVisibility(View.GONE);
                nothing_found.setVisibility(View.GONE);
            }
        };
    }

    private void getDataFromDB(String key) {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.orderByChild("MOBILE").startAt("+91" + key)
                .endAt("+91" + key + "\uf8ff").limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();
                mobile.clear();
                uid.clear();
                progressBar.setVisibility(View.GONE);
                Log.e("CameraDB", "Loaded");
                if (dataSnapshot.getChildrenCount() < 1)
                    nothing_found.setVisibility(View.VISIBLE);
                else nothing_found.setVisibility(View.GONE);
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    mobile.add((String) d.child("MOBILE").getValue());
                    uid.put((String) d.child("MOBILE").getValue(), d.getKey());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        workflowModel.markCameraFrozen();

        currentWorkflowState = WorkflowState.NOT_STARTED;
        cameraSource.setFrameProcessor(new BarcodeProcessor(graphicOverlay, workflowModel));
        workflowModel.setWorkflowState(WorkflowState.DETECTING);
    }

    @Override
    protected void onPostResume() {
    super.onPostResume();
    BarcodeResultFragment.dismiss(getSupportFragmentManager());
  }

  @Override
  protected void onPause() {
    super.onPause();
    currentWorkflowState = WorkflowState.NOT_STARTED;
    stopCameraPreview();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
      cameraSource = null;
    }
  }

    @Override
    public void onBackPressed() {
      if(bottomSheetBehavior==null )
      super.onBackPressed();
      else if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
          bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
      else super.onBackPressed();
    }

    @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.close_button) {
      onBackPressed();

    } else if (id == R.id.flash_button) {
      if (flashButton.isSelected()) {
        flashButton.setSelected(false);
        cameraSource.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF);
      } else {
        flashButton.setSelected(true);
        cameraSource.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
      }

    }
  }

  private void startCameraPreview() {
    if (!workflowModel.isCameraLive() && cameraSource != null) {
      try {
        workflowModel.markCameraLive();
        preview.start(cameraSource);
      } catch (IOException e) {
        Log.e(TAG, "Failed to start camera preview!", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  private void stopCameraPreview() {
    if (workflowModel.isCameraLive()) {
      workflowModel.markCameraFrozen();
      flashButton.setSelected(false);
      preview.stop();
    }
  }

  private void setUpWorkflowModel() {
    workflowModel = ViewModelProviders.of(this).get(WorkflowModel.class);

    // Observes the workflow state changes, if happens, update the overlay view indicators and
    // camera preview state.
    workflowModel.workflowState.observe(
        this,
        workflowState -> {
          if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
            return;
          }

          currentWorkflowState = workflowState;
          Log.d(TAG, "Current workflow state: " + currentWorkflowState.name());

          boolean wasPromptChipGone = (promptChip.getVisibility() == View.GONE);

          switch (workflowState) {
            case DETECTING:
              promptChip.setVisibility(View.VISIBLE);
              promptChip.setText(R.string.prompt_point_at_a_barcode);
              startCameraPreview();
              break;
            case CONFIRMING:
              promptChip.setVisibility(View.VISIBLE);
              promptChip.setText(R.string.prompt_move_camera_closer);
              startCameraPreview();
              break;
            case SEARCHING:
              promptChip.setVisibility(View.VISIBLE);
              promptChip.setText(R.string.prompt_searching);
              stopCameraPreview();
              break;
            case DETECTED:
            case SEARCHED:
              promptChip.setVisibility(View.GONE);
              stopCameraPreview();
              break;
            default:
              promptChip.setVisibility(View.GONE);
              break;
          }

          boolean shouldPlayPromptChipEnteringAnimation =
              wasPromptChipGone && (promptChip.getVisibility() == View.VISIBLE);
          if (shouldPlayPromptChipEnteringAnimation && !promptChipAnimator.isRunning()) {
            promptChipAnimator.start();
          }
        });

    workflowModel.detectedBarcode.observe(
        this,
        barcode -> {
          if (barcode != null) {
            String barData=barcode.getRawValue();
            if(java.util.Objects.requireNonNull(barData).contains("+91") ||
                    barData.contains("Order,")|| barData.contains("Transaction,")||
                    barData.contains("ReturnBottle,")){
            Log.d(getClass().getCanonicalName(),barData);
            if(type==null)
            {
              if(java.util.Objects.requireNonNull(barData).contains("Order"))
              {
                Intent intent=new Intent(this, InfoActivity.class);
                  intent.putExtra("0","BarCode");
                intent.putExtra("1","Orders");
                  intent.putExtra("3","1");
                intent.putExtra("DATA",barData.split(",")[1]);
                startActivity(intent);
              }
              else if(barData.contains("Transaction"))
              {
                Intent intent=new Intent(this, InfoActivity.class);
                intent.putExtra("0","BarCode");
                intent.putExtra("1","Transactions");
                intent.putExtra("3","0");

                intent.putExtra("DATA",barData.split(",")[1]);
                startActivity(intent);
              }
              else if(barData.contains("Return"))
              {
                  Intent intent=new Intent(this, InfoActivity.class);
                  intent.putExtra("0","BarCode");
                  intent.putExtra("1","Bottles");
                  intent.putExtra("3","0");
                  intent.putExtra("6","null");
                  intent.putExtra("DATA",barData.split(",")[1]);
                  startActivity(intent);
              }
              else {
                Intent intent=new Intent(this, CustomerInfoActivity.class);
                intent.putExtra("0","BarCode");
                intent.putExtra("UID",barData.split(",")[0]);
                intent.putExtra("MOBILE",barData.split(",")[1]);
                startActivity(intent);
              }
            }
            else {
                if (java.util.Objects.requireNonNull(barData).contains("Order"))
                {
                    Intent intent = new Intent(this, InfoActivity.class);
                    intent.putExtra("0", "BarCode");
                    intent.putExtra("1", "Orders");
                    intent.putExtra("3", "1");
                    intent.putExtra("DATA", barData.split(",")[1]);
                    startActivity(intent);
                }
                else if (barData.contains("Transaction"))
                {
                    Intent intent = new Intent(this, InfoActivity.class);
                    intent.putExtra("0", "BarCode");
                    intent.putExtra("1", "Transactions");
                    intent.putExtra("3", "0");
                    intent.putExtra("DATA", barData.split(",")[1]);
                    startActivity(intent);
                }
                else if(barData.contains("Return"))
                {
                    Intent intent=new Intent(this, InfoActivity.class);
                    intent.putExtra("0","BarCode");
                    intent.putExtra("1","Bottles");
                    intent.putExtra("3","0");
                    intent.putExtra("6","null");
                    intent.putExtra("DATA",barData.split(",")[1]);
                    startActivity(intent);
                }
                else
                {
                    Intent intent;
                    if (type.contains("Order"))
                        intent = new Intent(this, NewOrderActivity.class);

                    else if (type.contains("Transaction"))
                        intent = new Intent(this, NewTransactionActivity.class);
                    else
                        intent = new Intent(this, NewReturnBottlesActivity.class);
                    String[] data = java.util.Objects.requireNonNull(barData).split(",");
                    intent.putExtra("MOBILE", data[1]);
                    intent.putExtra("UID", data[0]);
                    startActivity(intent);
                }
            }
            }else{
                Toast.makeText(LiveBarcodeScanningActivity.this,"Invalid Code",Toast.LENGTH_LONG).show();
            startCameraPreview();}

          }
        });
  }


}
