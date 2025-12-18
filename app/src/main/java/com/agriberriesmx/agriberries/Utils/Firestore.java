package com.agriberriesmx.agriberries.Utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;

public class Firestore {

    public static void deleteDocuments(String collection, String field) {
        // Connect to Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection(collection);

        collectionReference.whereLessThanOrEqualTo(field, new Date())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Get document in elimination process
                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        batch.delete(documentSnapshot.getReference());
                    }

                    // Delete all documents
                    if (!queryDocumentSnapshots.isEmpty()) batch.commit();
                });
    }

}
