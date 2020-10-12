package com.example.snjdeveloper;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.RecyclerUI.RecyclerUI;
import com.example.snjdeveloper.admin.ControllerActivity;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

public class StatsActivity extends AppCompatActivity implements Spinner.OnItemSelectedListener {
    TreeMap<Integer, Integer> monthRecord;
    TextView filter,totalCost,totalTrip;
    private Spinner spinner;
    private RecyclerView recyclerview;
    ProgressBar progressBar;
    private String date;
    private SimpleDateFormat dateFormat;
    private String type;
    private String uid;
    private Query fireDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        filter = findViewById(R.id.monthText);

        recyclerview = findViewById(R.id.recyclerview);
        totalCost = findViewById(R.id.TotalAmount);
        totalTrip = findViewById(R.id.TotalTrip);
        monthRecord = new TreeMap<>();
        progressBar = findViewById(R.id.progressbar);
        spinner();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        date = dateFormat.format(new Date());
        receive();
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            startActivity(new Intent(this, HomeAdmin.class));
            finish();
        } else
            super.onBackPressed();
    }

    private void receive() {
        Intent intent = getIntent();
        type = intent.getStringExtra("CLASS_NAME");
        uid = intent.getStringExtra("UID");
        if (uid != null) {
            TextView textView = findViewById(R.id.title_small);
            textView.setVisibility(View.VISIBLE);
            textView.append(getIntent().getStringExtra("MOBILE"));
            findViewById(R.id.search).setVisibility(View.GONE);
        }
        if (type != null && type.contains("Transactions"))
            fireDb = new TransactionDb().getReference(this);
        else
            fireDb = FirebaseDatabase.getInstance().
                    getReference(type);
        TextView textView = findViewById(R.id.TotalTrip);
        TextView textView1 = findViewById(R.id.title);
        textView1.setText(type);
        textView.setText(String.format("Loading %s", type));
        query(convertDateTOMillis(date + " 00:00:00"), convertDateTOMillis(date + " 23:59:59"));
    }

    void spinner(){
        spinner = findViewById(R.id.spinner2);
        spinner.setActivated(true);
        String[] years = {"Today","Yesterday","Select day","Select Month","" };
        ArrayAdapter<CharSequence> langAdapter = new ArrayAdapter<>(this, R.layout.spinner_text, years);
        langAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinner.setAdapter(langAdapter);
        spinner.setOnItemSelectedListener(this);
        filter.setOnClickListener(view -> spinner.performClick());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch (i){
            case 0:
                filter.setText("Today ");
                query(convertDateTOMillis( date + " 00:00:00"), convertDateTOMillis(date + " 23:59:59"));
                break;
            case 1:
                filter.setText("Yesterday ");
                Calendar cal2= Calendar.getInstance();
                cal2.add(Calendar.DATE,-1);
                String d=dateFormat.format(cal2.getTime());
                query(convertDateTOMillis( d + " 00:00:00"), convertDateTOMillis(d + " 23:59:59"));
                break;
            case 2:

                spinner.setSelection(4);
                createDaysSelectorDialog();

                break;
            case 3:spinner.setSelection(4);
                dialogMonthList();
                break;
            case 4:
                spinner.setSelection(4);
             try {
                 DatePickerDialog datePickerDialog = createDialogWithoutDateField();
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                     datePickerDialog.setOnDateSetListener((datePicker, i12, i1, i2) -> query(convertDateTOMillis(datePicker.getDayOfMonth() + "/" + datePicker.getMonth() + "/" + datePicker.getYear() + " 00:00:00"), convertDateTOMillis(datePicker.getDayOfMonth() + "/" + datePicker.getMonth() + "/" + datePicker.getYear() + " 23:59:59")));
                 }
             }catch (Exception e){e.printStackTrace();}
                break;
        }
        if (i==2){
//            DatePickerDialog datePickerDialog=    createDialogWithoutDateField();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
//                        @Override
//                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
//                            query(convertDateTOMillis(datePicker.getDayOfMonth()+"/"+datePicker.getMonth()+"/"+datePicker.getYear()+" 00:00:00"),convertDateTOMillis(datePicker.getDayOfMonth()+"/"+datePicker.getMonth()+"/"+datePicker.getYear()+" 23:59:59" ));
//                        }
//                    });
//                }else
            {
//                dialogMonthList();
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    long convertDateTOMillis(String date){
        try {
            return (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).parse(date).getTime());
        } catch (ParseException e) {
            return -1;
        }
    }

    void query(long fromMillis,long toMillis) {
        progressBar.setVisibility(View.VISIBLE);
        //Log.e("UID=",Constants.uid);
        //.orderByChild("KEY").startAt(Constants.uid+fromMillis).
        //         "\uf8ff"       endAt(Constants.uid+toMillis)
        Query query;
        if (uid == null)
            query = fireDb.orderByChild("DATE").startAt("" + fromMillis).
                    endAt(toMillis + "");
        else query = fireDb.orderByChild("KEY").
                startAt(uid + fromMillis).
                endAt(uid + toMillis);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                new RecyclerUI(dataSnapshot, getApplicationContext(), recyclerview, totalCost, totalTrip, dataSnapshot.getKey(), type);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.INVISIBLE);
                totalTrip.setText(R.string.failed_to_connect);
            }

        });
    }

    private void createDaysSelectorDialog(){
        View dialogView = View.inflate( this, R.layout.calendar_layout, null);
        final Dialog dialog = new Dialog(this,R.style.Dialog1);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;
        Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);

        dialog.setContentView(dialogView);


        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
                return true;
            }
            return false;
        });
        CalendarView calendarView=dialogView.findViewById(R.id.calendar);
        Calendar cal1= Calendar.getInstance();
        calendarView.setMaxDate(cal1.getTimeInMillis());
        calendarView.setOnDateChangeListener((calendarView1, year, month, day) -> {
            dialog.dismiss();
            month+=1;
            query(convertDateTOMillis(day + "/"+month+"/" + year + " 00:00:00"), convertDateTOMillis(day + "/" +month+"/"+ year + " 23:59:59"));
            filter.setText(String.format(Locale.UK,"%d/%d/%d ", day, month, year));
        });

        dialog.show();
    }

    private DatePickerDialog createDialogWithoutDateField() {
        DatePickerDialog dpd = new DatePickerDialog(this, null, 2019, 6, 17);
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        if ("mDaySpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            if (dayPicker != null) {
                                ((View) dayPicker).setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return dpd;
    }

    void dialogMonthList()
    {   monthRecord.clear();
        final int[] currentYear = {Integer.parseInt((new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date())))};
        View dialogView = View.inflate( this, R.layout.month_list, null);
        final Dialog dialog = new Dialog(this,R.style.Dialog1);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;
        Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        final TextView firstMonth=dialogView.findViewById(R.id.firstMonth);
        final TextView lastMonth=dialogView.findViewById(R.id.lastMonth);
        final View arrow=dialogView.findViewById(R.id.monthSeparator);

        dialog.setContentView(dialogView);
        dialog.findViewById(R.id.cancel).setOnClickListener(view -> {
            dialog.dismiss();
        });

        final Spinner spinner = dialogView. findViewById(R.id.spinner2);
        final String[] years = {String.valueOf(currentYear[0]), String.valueOf(currentYear[0] -1), String.valueOf(currentYear[0] -2), String.valueOf(currentYear[0] -3)};
        ArrayAdapter<CharSequence> yearAdapter = new ArrayAdapter<>(this, R.layout.spinner_text, years);
        yearAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinner.setAdapter(yearAdapter);

        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
                return true;
            }
            return false;
        });
        dialogView.findViewById(R.id.jan).setOnClickListener(v -> monthRecord =
                checkCheckbox(monthRecord, (CheckBox) v,firstMonth,lastMonth,arrow,1,31));
        dialogView.findViewById(R.id.feb).setOnClickListener(v -> {
            currentYear[0] = Integer.parseInt(years[spinner.getSelectedItemPosition()]);
            if (currentYear[0] %4==0)
                monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 2,29);

            else
                monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 2,28);
        });
        dialogView.findViewById(R.id.mar).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 3,31));
        dialogView.findViewById(R.id.apr).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 4,30));dialogView.findViewById(R.id.may).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 5,31));
        dialogView.findViewById(R.id.june).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 6,30));dialogView.findViewById(R.id.july).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 7,31));dialogView.findViewById(R.id.aug).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 8,31));dialogView.findViewById(R.id.sep).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 9,30));dialogView.findViewById(R.id.oct).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 10,31));dialogView.findViewById(R.id.nov).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 11,30));
        dialogView.findViewById(R.id.dec).setOnClickListener(v -> monthRecord =checkCheckbox(monthRecord, (CheckBox) v, firstMonth, lastMonth, arrow, 12,31));
        dialogView.findViewById(R.id.action_bt).setOnClickListener(v -> {
            dialog.dismiss();
            try {
                currentYear[0] = Integer.parseInt(years[spinner.getSelectedItemPosition()]);
                String first = "01/" + monthRecord.firstKey();
                String last = monthRecord.get(monthRecord.lastKey()) + "/" + monthRecord.lastKey();
                filter.setText(String.format(Locale.UK,"From %s to %s %d ", first, last, currentYear[0]));
                query(convertDateTOMillis(first + "/" + currentYear[0] + " 00:00:00"), convertDateTOMillis(last + "/" + currentYear[0] + " 23:59:59"));
            }catch (Exception ignored){}
        });

        dialog.show();
    }
    String[] month={"","January","February","March","April","May","June","July","August","September","October","November","December"};
    TreeMap<Integer, Integer> checkCheckbox(TreeMap<Integer, Integer> monthList, CheckBox v, TextView firstMonth, TextView lastMonth, View arrow, int mm, int days){
        if (  v.isChecked()){

            monthList.put(mm,days);
        }else{
            monthList.remove(mm);
        }
    try {

        if (monthList.firstKey().equals(monthList.lastKey())) {
            firstMonth.setText(month[monthList.firstKey()]);
            arrow.setVisibility(View.GONE);
            lastMonth.setVisibility(View.GONE);
        } else {
            lastMonth.setVisibility(View.VISIBLE);
            firstMonth.setText(month[monthList.firstKey()]);
            arrow.setVisibility(View.VISIBLE);
            lastMonth.setText(month[monthList.lastKey()]);
        }
    }catch (Exception e){monthList.clear();firstMonth.setText("");
                        e.printStackTrace();}
        return monthList;
    }


    public void backOnClicked(View view) {
        onBackPressed();
    }

    public void searchuttonOnclick(View view) {
        Intent intent=new Intent(this, ControllerActivity.class);
        intent.putExtra("CLASS_NAME",type);
        startActivity(intent);
    }
}

