package com.agriberriesmx.agriberries.Services;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // Send token to Firebase Firestore
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();

        if (uid != null) {
            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("consultants").document(uid);
            documentReference.update("token", token);
        }
    }

}
