package com.agriberriesmx.agriberries.Admin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.SpinnerCropAdapter;
import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Knowledge;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateKnowledgeAdmin extends AppCompatActivity {
    private TextAdapter cropAdapter;
    private final List<String> crops = new ArrayList<>();
    private Spinner spinnerCrop;
    private EditText etvTitle, etvLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_create_knowledge);
        setup();
    }

    private void setup() {
        // Link XML to Java
        spinnerCrop = findViewById(R.id.spinnerCrop);
        etvTitle = findViewById(R.id.etvTitle);
        etvLink = findViewById(R.id.etvLink);
        RecyclerView rvCrops = findViewById(R.id.rvCrops);
        ImageButton btnPaste = findViewById(R.id.btnPaste);
        ImageButton btnAddCrop = findViewById(R.id.btnAddCrop);
        Button btnCreateKnowledge = findViewById(R.id.btnCreateKnowledge);

        // Get crops names
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        List<Crop> cropList = agriBerries.getGlobalCropList();
        List<String> cropNameList = new ArrayList<>();
        for (Crop crop : cropList)  if (crop.getDeleted() == null) cropNameList.add(crop.getName());

        // Create spinner adapter
        SpinnerCropAdapter spinnerCropAdapter = new SpinnerCropAdapter(this, cropNameList);
        spinnerCrop.setAdapter(spinnerCropAdapter);

        // Initialize views
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cropAdapter = new TextAdapter(crops);
        rvCrops.setLayoutManager(linearLayoutManager);
        rvCrops.setAdapter(cropAdapter);

        // Enable swipe to delete and undo
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                crops.remove(position);
                cropAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvCrops);

        // Add listeners
        btnAddCrop.setOnClickListener(v -> addCrop());
        btnPaste.setOnClickListener(v -> pasteFromClipboard());
        btnCreateKnowledge.setOnClickListener(v -> createKnowledge());
    }

    private void addCrop() {
        // Add crop to the list and update adapter
        String crop = spinnerCrop.getSelectedItem().toString();

        if (!crop.isEmpty()) {
            if (!crops.contains(crop)) {
                int position = Collections.binarySearch(crops, crop);

                // Get insert position for alphabetic order
                if (position < 0) position = -(position + 1);

                // Add element
                crops.add(position, crop);
                cropAdapter.notifyItemInserted(position);
            } else Toast.makeText(this, getResources().getString(R.string.cropAlreadyAdded), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.cropNotAdd), Toast.LENGTH_SHORT).show();
    }

    private void createKnowledge() {
        // Get attributes to verify that they are not empty
        String title = etvTitle.getText().toString().trim();
        String link = etvLink.getText().toString().trim();

        if (!title.isEmpty() && !link.isEmpty() && !crops.isEmpty()) {
            // Change link to a valid one
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                // Transform link to a valid one
                String previousLink = link;
                link = "http://" + previousLink;
            }

            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String id = db.collection("knowledge").document().getId();
            DocumentReference documentReference = db.collection("knowledge").document(id);

            // Create knowledge info
            Knowledge knowledge = new Knowledge();
            knowledge.setId(id);
            knowledge.setTitle(title);
            knowledge.setLink(link);
            knowledge.setCrops(crops);
            knowledge.setDeleted(null);

            // Save new knowledge info into database
            documentReference.set(knowledge);
            Toast.makeText(CreateKnowledgeAdmin.this, getResources().getString(R.string.knowledgeCreated), Toast.LENGTH_SHORT).show();
            finish();
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
