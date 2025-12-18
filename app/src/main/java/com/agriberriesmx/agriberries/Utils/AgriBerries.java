package com.agriberriesmx.agriberries.Utils;

import android.app.Application;

import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.POJO.Plague;
import com.agriberriesmx.agriberries.POJO.Treatment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AgriBerries extends Application {
    private NotificationListUpdateListener notificationListUpdateListener;
    private List<Crop> globalCropList;
    private List<Plague> globalPlagueList;
    private List<Treatment> globalTreatmentList;
    private List<Notification> globalNotificationList;

    public interface NotificationListUpdateListener {
        void onNotificationListUpdated();
    }

    public void setNotificationListUpdateListener(NotificationListUpdateListener listener) {
        this.notificationListUpdateListener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize arrays
        globalCropList = new ArrayList<>();
        globalPlagueList = new ArrayList<>();
        globalTreatmentList = new ArrayList<>();
        globalNotificationList = new ArrayList<>();

        // Connect to Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get crop info
        CollectionReference cropCollection = db.collection("crops");
        cropCollection.orderBy("deleted", Query.Direction.DESCENDING)
                .orderBy("name")
                .addSnapshotListener((value, error) -> {
                    if (value != null) globalCropList = value.toObjects(Crop.class);
                });

        // Get plague info
        CollectionReference plagueCollection = db.collection("plagues");
        plagueCollection.orderBy("deleted", Query.Direction.DESCENDING)
                .orderBy("name")
                .addSnapshotListener((value, error) -> {
                    if (value != null) globalPlagueList = value.toObjects(Plague.class);
                });

        // Get treatment info
        CollectionReference treatmentCollection = db.collection("treatments");
        treatmentCollection.orderBy("deleted", Query.Direction.DESCENDING)
                .orderBy("name")
                .addSnapshotListener((value, error) -> {
                    if (value != null) globalTreatmentList = value.toObjects(Treatment.class);
                });

        // Get notifications info
        CollectionReference notificationCollection = db.collection("notifications");
        notificationCollection.whereLessThanOrEqualTo("begin", new Date())
                .orderBy("begin", Query.Direction.DESCENDING)
                .orderBy("title")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        globalNotificationList = value.toObjects(Notification.class);
                        if (notificationListUpdateListener != null) {
                            notificationListUpdateListener.onNotificationListUpdated();
                        }
                    }
                });
    }

    public List<Crop> getGlobalCropList() {
        return globalCropList;
    }

    public List<Plague> getGlobalPlagueList() {
        return globalPlagueList;
    }

    public List<Treatment> getGlobalTreatmentList() {
        return globalTreatmentList;
    }

    public List<Notification> getGlobalNotificationList() {
        return globalNotificationList;
    }

}

