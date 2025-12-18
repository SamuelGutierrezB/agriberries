package com.agriberriesmx.agriberries;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.NotificationAdapter;
import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private NotificationAdapter notificationAdapter;
    private final List<Notification> notificationList = new ArrayList<>();
    private LinearLayout linearLayoutNoNotifications;
    private RecyclerView rvNotifications;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        setup();
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    protected void onResume() {
        super.onResume();

        // Get notifications and update lists
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        notificationList.clear();
        notificationList.addAll(agriBerries.getGlobalNotificationList());

        // Notify about any change
        notificationAdapter.notifyDataSetChanged();

        // Update visibility of the recycler view and linear layout
        if (notificationList.size() == 0) {
            linearLayoutNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            linearLayoutNoNotifications.setVisibility(View.GONE);
        }
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        linearLayoutNoNotifications = findViewById(R.id.linearLayoutNoNotifications);
        rvNotifications = findViewById(R.id.rvNotifications);

        // Initialize toolbar
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(getResources().getString(R.string.manageNotifications));

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvNotifications.setLayoutManager(linearLayoutManager);

        // Get current user's UID
        String uid = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) uid = user.getUid();

        // Set adapter for recycler view
        notificationAdapter = new NotificationAdapter(notificationList, uid);
        rvNotifications.setAdapter(notificationAdapter);
    }

}
