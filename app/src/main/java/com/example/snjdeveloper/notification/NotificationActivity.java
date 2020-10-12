package com.example.snjdeveloper.notification;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.snjdeveloper.R;

public class NotificationActivity extends AppCompatActivity {
    private Intent intent;
    String from;
    private Dialog dialog;
    private ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_controller);
        intent = new Intent(this, SendNotificationActivity.class);
        TextView textView=findViewById(R.id.title);
        textView.setText("Notification");
    }
    public void backOnClicked(View view) {
        finish();
    }

    public void popupClicked(View view) {
        Intent intent = new Intent(this, SendPopupNotificationActivity.class);
        startActivity(intent);
    }

    public void emailClicked(View view) {
        from="Send email message";
        intent.putExtra("from",from);
        startActivity(intent);
    }

    public void notificationClicked(View view) {
        from="Send notification";
        intent.putExtra("from",from);
        startActivity(intent);
    }
    void showInfoDialog(int stringid,int iconId){
        setTheme(R.style.AppThemeBlack);
        if (dialog==null) {
            final View dialogView = View.inflate(this, R.layout.info_dialog, null);
            dialog = new Dialog(this, R.style.Dialog1);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(dialogView);
            dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
                if (i == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                }
                return false;
            });
            dialog.setOnDismissListener(dialog -> setTheme(R.style.AppTheme));
            scrollView=dialog.findViewById(R.id.scrollView);
            TextView textView=dialog.findViewById(R.id.help_msg);
            textView.setText(stringid);
            Glide.with(dialogView).load(iconId).into((ImageView)dialogView.findViewById(R.id.icon));
            dialogView.findViewById(R.id.okBt).setOnClickListener(view -> dialog.dismiss());
        }else{
            TextView textView=dialog.findViewById(R.id.help_msg);
            textView.setText(stringid);
            Glide.with(dialog.getContext()).load(iconId).into((ImageView)dialog.findViewById(R.id.icon));
            if (scrollView!=null)
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        }
        dialog.show();
    }

    public void openPopupHelp(View view) {
        showInfoDialog(R.string.popup_help_message,R.drawable.inapp_notification);

    }

    public void openNotifyHelp(View view) {
        showInfoDialog(R.string.notification_help_message, R.drawable.status_bar_notification);

    }

    public void openEmailHelp(View view) {
        showInfoDialog(R.string.mail_help_message, R.drawable.ic_email_message);
    }

    public void infoBoxClicked(View view) {
        startActivity(new Intent(this, BottomBoxNotification.class));
    }

    public void openInfoHelp(View view) {
        showInfoDialog(R.string.info_box_help_message, R.drawable.info_box_img);
    }

    public void extraClicked(View view) {
        startActivity(new Intent(this, ExtraNotificationActivity.class));
    }

    public void openSavedNotification(View view) {
       Intent intent= new Intent(this, SavedNotificationActivity.class);
       intent.putExtra("NodeName","Saved Notification");
        startActivity(intent);
    }
}

