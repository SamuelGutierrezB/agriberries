package com.agriberriesmx.agriberries.Admin;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateNotificationAdmin extends AppCompatActivity {
    private Notification notification;
    private Calendar begin, end;
    private EditText etvTitle, etvText, etvLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_update_notification);
        setup();
    }

    private void setup() {
        // Link XML to Java
        etvTitle = findViewById(R.id.etvTitle);
        etvText = findViewById(R.id.etvText);
        etvLink = findViewById(R.id.etvLink);
        TextView tvBegin = findViewById(R.id.tvBegin);
        TextView tvEnd = findViewById(R.id.tvEnd);
        ImageButton btnPaste = findViewById(R.id.btnPaste);
        Button btnUpdateNotification = findViewById(R.id.btnUpdateNotification);
        Button btnDeleteNotification = findViewById(R.id.btnDeleteNotification);

        // Get notification
        notification = getIntent().getParcelableExtra("notification");

        // Initialize calendars and simple date format
        begin = Calendar.getInstance();
        begin.setTime(notification.getBegin());
        end = Calendar.getInstance();
        end.setTime(notification.getEnd());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Initialize views
        etvTitle.setText(notification.getTitle());
        etvText.setText(notification.getText());
        etvLink.setText(notification.getLink());
        tvBegin.setText(sdf.format(notification.getBegin()));
        tvEnd.setText(sdf.format(notification.getEnd()));

        // Add listeners
        tvBegin.setOnClickListener(v -> {
            int day = begin.get(Calendar.DAY_OF_MONTH);
            int month = begin.get(Calendar.MONTH);
            int year = begin.get(Calendar.YEAR);

            // Create DatePicker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(UpdateNotificationAdmin.this, (view, updatedYear, updatedMonth, updatedDay) -> {
                // Update begin date
                begin.set(Calendar.YEAR, updatedYear);
                begin.set(Calendar.MONTH, updatedMonth);
                begin.set(Calendar.DAY_OF_MONTH, updatedDay);

                // Get formatted begin date and show it
                tvBegin.setText(sdf.format(begin.getTime()));
            }, year, month, day);

            // Show DatePicker dialog
            datePickerDialog.show();
        });
        tvEnd.setOnClickListener(v -> {
            int day = end.get(Calendar.DAY_OF_MONTH);
            int month = end.get(Calendar.MONTH);
            int year = end.get(Calendar.YEAR);

            // Create DatePicker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(UpdateNotificationAdmin.this, (view, updatedYear, updatedMonth, updatedDay) -> {
                // Update end date
                end.set(Calendar.YEAR, updatedYear);
                end.set(Calendar.MONTH, updatedMonth);
                end.set(Calendar.DAY_OF_MONTH, updatedDay);

                // Get formatted end date and show it
                tvEnd.setText(sdf.format(end.getTime()));
            }, year, month, day);

            // Show DatePicker dialog
            datePickerDialog.show();
        });
        btnPaste.setOnClickListener(v -> pasteFromClipboard());
        btnUpdateNotification.setOnClickListener(v -> updateNotification());
        btnDeleteNotification.setOnClickListener(v -> deleteNotification());
    }

    private void updateNotification() {
        // Get attributes to verify that they are not empty
        String title = etvTitle.getText().toString().trim();
        String text = etvText.getText().toString().trim();
        String link = etvLink.getText().toString().trim();

        if (!title.isEmpty() && !text.isEmpty()) {
            // Verify if end date is greater than begin date
            Calendar currentDate = Calendar.getInstance();

            if (end.compareTo(begin) > 0 && end.compareTo(currentDate) > 0) {
                // Change link to a valid one
                if (!link.isEmpty()) {
                    if (!link.startsWith("http://") && !link.startsWith("https://")) {
                        // Transform link to a valid one
                        String previousLink = link;
                        link = "http://" + previousLink;
                    }
                }

                // Connect to Firebase Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection("notifications").document(notification.getId());

                // Update notification info
                notification.setTitle(title);
                notification.setText(text);
                notification.setLink(link);
                notification.setBegin(begin.getTime());
                notification.setEnd(end.getTime());

                // Update notification info
                documentReference.set(notification);
                Toast.makeText(UpdateNotificationAdmin.this, getResources().getString(R.string.notificationUpdated), Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, getResources().getString(R.string.invalidDates), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    private void deleteNotification() {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

        // Link XML to Java
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Initialize views
        tvMessage.setText(getResources().getString(R.string.ask_for_notification_deletion));
        btnPositive.setText(getResources().getString(R.string.elimination));

        // Create Alert Dialog to ask confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Add listeners
        btnPositive.setOnClickListener(v -> {
            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("notifications").document(notification.getId());

            // Delete notification
            documentReference.delete();
            Toast.makeText(UpdateNotificationAdmin.this, getResources().getString(R.string.notification_deleted), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            finish();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Show Alert Dialog
        dialog.show();
    }

    private void pasteFromClipboard() {
        // Get clipboard service and content (if exists)
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();

        // Verify if there is content
        if (clip != null && clip.getItemCount() > 0) {
            // Convert to string
            CharSequence link = clip.getItemAt(0).getText();
            etvLink.setText(link);
        }
    }

}
