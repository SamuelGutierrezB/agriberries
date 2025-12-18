package com.agriberriesmx.agriberries.Admin;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CreateNotificationAdmin extends AppCompatActivity {
    private Calendar begin, end;
    private EditText etvTitle, etvText, etvLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_create_notification);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        etvTitle = findViewById(R.id.etvTitle);
        etvText = findViewById(R.id.etvText);
        etvLink = findViewById(R.id.etvLink);
        TextView tvBegin = findViewById(R.id.tvBegin);
        TextView tvEnd = findViewById(R.id.tvEnd);
        ImageButton btnPaste = findViewById(R.id.btnPaste);
        Button btnCreateNotification = findViewById(R.id.btnCreateNotification);

        // Get formatted begin and end date
        begin = Calendar.getInstance();
        end = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(begin.getTime());

        // Change begin date
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);

        // Change end date
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        // Initialize views
        tvBegin.setText(formattedDate);
        tvEnd.setText(formattedDate);

        // Initialize toolbar
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(getResources().getString(R.string.createNotification));

        // Add listeners
        tvBegin.setOnClickListener(v -> {
            int day = begin.get(Calendar.DAY_OF_MONTH);
            int month = begin.get(Calendar.MONTH);
            int year = begin.get(Calendar.YEAR);

            // Create DatePicker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateNotificationAdmin.this, (view, updatedYear, updatedMonth, updatedDay) -> {
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateNotificationAdmin.this, (view, updatedYear, updatedMonth, updatedDay) -> {
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
        btnCreateNotification.setOnClickListener(v -> createNotification());
    }

    private void createNotification() {
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
                String id = db.collection("notifications").document().getId();
                DocumentReference documentReference = db.collection("notifications").document(id);

                // Create notification info
                Notification notification = new Notification();
                notification.setId(id);
                notification.setTitle(title);
                notification.setText(text);
                notification.setLink(link);
                notification.setConsultantsSeen(new ArrayList<>());
                notification.setBegin(begin.getTime());
                notification.setEnd(end.getTime());

                // Save new notification info into database
                documentReference.set(notification);
                Toast.makeText(CreateNotificationAdmin.this, getResources().getString(R.string.notificationCreated), Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, getResources().getString(R.string.invalidDates), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
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
