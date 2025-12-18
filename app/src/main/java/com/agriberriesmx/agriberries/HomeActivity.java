package com.agriberriesmx.agriberries;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.agriberriesmx.agriberries.Admin.MenuAdmin;
import com.agriberriesmx.agriberries.POJO.Consultant;
import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.SharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements AgriBerries.NotificationListUpdateListener {
    private TextView tvNotifications;
    private boolean first = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((AgriBerries)getApplicationContext()).setNotificationListUpdateListener(this);
        onNotificationListUpdated();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((AgriBerries)getApplicationContext()).setNotificationListUpdateListener(null);
    }

    private void setup() {
        // Link XML to Java
        TextView tvAdministrator = findViewById(R.id.tvAdministrator);
        TextView tvKnowledgeCenter = findViewById(R.id.tvKnowledgeCenter);
        TextView tvClients = findViewById(R.id.tvClients);
        tvNotifications = findViewById(R.id.tvNotifications);
        TextView tvAbout = findViewById(R.id.tvAbout);
        TextView tvSignOut = findViewById(R.id.tvSignOut);

        // Get current user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Pre-initialize it
        boolean admin = false;
        SharedPreferencesManager preferencesManager = SharedPreferencesManager.getInstance(this);
        if (preferencesManager.getCategoryFromPreferences() == 0) {
            tvAdministrator.setVisibility(View.VISIBLE);
            admin = true;
        }

        if (user != null) {
            // Get UID and get document reference
            String uid = user.getUid();
            DocumentReference documentReference = db.collection("consultants").document(uid);

            boolean finalAdmin = admin;
            documentReference.addSnapshotListener((value, error) -> {
                // Verify if the documents exists
                if (value != null) {
                    // Get current user info
                    Consultant consultant = value.toObject(Consultant.class);

                    if (consultant != null) {
                        // Change visibility based on user's category
                        int category = consultant.getCategory();
                        if (category == 0 && !finalAdmin) tvAdministrator.setVisibility(View.VISIBLE);
                        else if (category != 0 && finalAdmin) tvAdministrator.setVisibility(View.GONE);

                        // Welcome user
                        if (first) {
                            // Get welcome message
                            String welcomeMessage = getResources().getString(R.string.welcome) + " " + consultant.getName();
                            Toast.makeText(HomeActivity.this, welcomeMessage, Toast.LENGTH_SHORT).show();
                            first = false;
                        }

                        // Save current user info on Shared Preferences
                        preferencesManager.saveNameFromPreferences(consultant.getName());
                        preferencesManager.saveCategoryFromPreferences(category);
                    }
                }
            });

            // Initialize request permission launcher
            ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isGranted) Toast.makeText(HomeActivity.this, getResources().getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show();
                    });

            // Request permission (if needed)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }

            // Get FCM token
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get FCM token
                        String token = task.getResult();

                        // Connect to Firebase Firestore
                        DocumentReference consultantReference = db.collection("users").document(uid);
                        consultantReference.update("token", token);
                    });

            // Subscribe to "allUsers" topic
            FirebaseMessaging.getInstance().subscribeToTopic("allUsers");
        }

        // Add listeners
        tvAdministrator.setOnClickListener(v -> startActivity(new Intent(this, MenuAdmin.class)));
        tvKnowledgeCenter.setOnClickListener(v -> startActivity(new Intent(this, KnowledgeCenterActivity.class)));
        tvClients.setOnClickListener(v -> startActivity(new Intent(this, ClientsActivity.class)));
        tvNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        tvAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        tvSignOut.setOnClickListener(v -> signOut());
    }

    private void signOut() {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

        // Link XML to Java
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Initialize views
        tvMessage.setText(getResources().getString(R.string.ask_for_sign_out));
        btnPositive.setText(getResources().getString(R.string.confirm));

        // Create Alert Dialog to ask confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Add listeners
        btnPositive.setOnClickListener(v -> {
            // Remove information from Shared Preferences
            SharedPreferencesManager preferencesManager = SharedPreferencesManager.getInstance(this);
            preferencesManager.removeCategoryFromPreferences();

            // SignOut and go to SignIn Activity
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, getResources().getString(R.string.sessionClosed), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();

            dialog.dismiss();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Show Alert Dialog
        dialog.show();
    }

    @Override
    public void onNotificationListUpdated() {
        // Get current user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Get current user's UID
            String uid = user.getUid();

            // Get notifications
            AgriBerries agriBerries = (AgriBerries) getApplicationContext();
            List<Notification> notificationList = agriBerries.getGlobalNotificationList();

            // Verify notifications
            boolean hasUnreadNotification = false;
            for (Notification notification : notificationList) {
                if (!notification.getConsultantsSeen().contains(uid)) {
                    // There is an unread notification
                    hasUnreadNotification = true;
                    break;
                }
            }

            // Change text view
            Drawable leftDrawable;
            Drawable rightDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow);
            if (hasUnreadNotification) leftDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_notifications_unread);
            else leftDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_notifications);
            tvNotifications.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, rightDrawable, null);
        }
    }

}
