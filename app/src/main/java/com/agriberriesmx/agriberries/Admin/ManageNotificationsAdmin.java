package com.agriberriesmx.agriberries.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.AdminNotificationAdapter;
import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.Firestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ManageNotificationsAdmin extends AppCompatActivity {
    private ListenerRegistration notificationListener;
    private AdminNotificationAdapter adminNotificationAdapter;
    private final List<Notification> notificationList = new ArrayList<>();
    private final List<Notification> filteredNotificationList = new ArrayList<>();
    private SearchView svNotifications;
    private LinearLayout linearLayoutNoNotifications;
    private RecyclerView rvNotifications;
    private String title = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_notifications);
        Firestore.deleteDocuments("notifications", "end");
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get notifications info and clear focus
        svNotifications.clearFocus();
        if (notificationListener == null) getNotifications();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove listener (if exists)
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    private void setup() {
        // Link XML to Java
        svNotifications = findViewById(R.id.svNotifications);
        linearLayoutNoNotifications = findViewById(R.id.linearLayoutNoNotifications);
        rvNotifications = findViewById(R.id.rvNotifications);
        Button btnCreateNotification = findViewById(R.id.btnCreateNotification);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvNotifications.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        adminNotificationAdapter = new AdminNotificationAdapter(filteredNotificationList);
        rvNotifications.setAdapter(adminNotificationAdapter);

        // Add listeners
        svNotifications.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                title = newText;
                filterNotifications();

                return true;
            }
        });
        btnCreateNotification.setOnClickListener(v -> startActivity(new Intent(this, CreateNotificationAdmin.class)));
    }

    private void getNotifications() {
        // Connect to Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("notifications");

        // Get all notifications
        Date currentDate = new Date();
        notificationListener = collectionReference.whereGreaterThanOrEqualTo("end", currentDate)
                .orderBy("end", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    // Clear all lists
                    filteredNotificationList.clear();
                    notificationList.clear();

                    for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(value).getDocuments()) {
                        Notification notification = documentSnapshot.toObject(Notification.class);
                        notificationList.add(notification);
                        filteredNotificationList.add(notification);
                    }

                    filterNotifications();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterNotifications() {
        // Filter notifications base on the title
        filteredNotificationList.clear();

        for (Notification notification : notificationList) {
            if (notification.getTitle().toLowerCase().contains(title.toLowerCase()))
                filteredNotificationList.add(notification);
        }

        // Notify about the changes on the list
        adminNotificationAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        // Update visibility of the recycler view and linear layout
        if (filteredNotificationList.size() == 0) {
            linearLayoutNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            linearLayoutNoNotifications.setVisibility(View.GONE);
        }
    }

}
