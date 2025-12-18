package com.agriberriesmx.agriberries.Admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateCropAdmin extends AppCompatActivity {
    private TextAdapter textAdapter;
    private final List<String> types = new ArrayList<>();
    private EditText etvName, etvType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_create_crop);
        setup();
    }

    private void setup() {
        // Link XML to Java
        etvName = findViewById(R.id.etvName);
        etvType = findViewById(R.id.etvType);
        RecyclerView rvTypes = findViewById(R.id.rvTypes);
        ImageButton btnAddType = findViewById(R.id.btnAddType);
        Button btnCreateCrop = findViewById(R.id.btnCreateCrop);

        // Initialize views
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        textAdapter = new TextAdapter(types);
        rvTypes.setLayoutManager(linearLayoutManager);
        rvTypes.setAdapter(textAdapter);

        // Enable swipe to delete and undo
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                types.remove(position);
                textAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvTypes);

        // Add listeners
        btnAddType.setOnClickListener(v -> addType());
        btnCreateCrop.setOnClickListener(v -> createCrop());
    }

    private void addType() {
        // Add type to the list and update adapter
        String type = etvType.getText().toString().trim();

        if (!type.isEmpty()) {
            if (!types.contains(type)) {
                int position = Collections.binarySearch(types, type);

                // Get insert position for alphabetic order
                if (position < 0) position = -(position + 1);

                // Add element
                types.add(position, type);
                textAdapter.notifyItemInserted(position);
                etvType.setText("");
            } else Toast.makeText(this, getResources().getString(R.string.type_already_added), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    private void createCrop() {
        // Get attributes to verify that they are not empty
        String name = etvName.getText().toString().trim();

        if (!name.isEmpty() && types.size() > 0) {
            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String id = db.collection("crops").document().getId();
            DocumentReference documentReference = db.collection("crops").document(id);

            // Create crop info
            Crop crop = new Crop();
            crop.setId(id);
            crop.setName(name);
            crop.setTypes(types);
            crop.setDeleted(null);

            // Save new crop info into database
            documentReference.set(crop);
            Toast.makeText(CreateCropAdmin.this, getResources().getString(R.string.cropCreated), Toast.LENGTH_SHORT).show();
            finish();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

}
