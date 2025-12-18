package com.agriberriesmx.agriberries;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.KnowledgeAdapter;
import com.agriberriesmx.agriberries.Adapter.SpinnerCropAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Knowledge;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KnowledgeCenterActivity extends AppCompatActivity {
    private ListenerRegistration knowledgeListener;
    private KnowledgeAdapter knowledgeAdapter;
    private final List<Knowledge> knowledgeList = new ArrayList<>();
    private final List<Knowledge> filteredKnowledgeList = new ArrayList<>();
    private SearchView svKnowledge;
    private Spinner spinnerCrop;
    private LinearLayout linearLayoutNoKnowledge;
    private RecyclerView rvKnowledge;
    private String title = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge_center);
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get knowledge info and clear focus
        svKnowledge.clearFocus();
        if (knowledgeListener == null) getKnowledge();

        // Get crops names
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        List<Crop> cropList = agriBerries.getGlobalCropList();
        List<String> cropNameList = new ArrayList<>();
        cropNameList.add(getResources().getString(R.string.all));
        for (Crop crop : cropList)  if (crop.getDeleted() == null) cropNameList.add(crop.getName());

        // Create spinner adapter
        SpinnerCropAdapter spinnerCropAdapter = new SpinnerCropAdapter(KnowledgeCenterActivity.this, cropNameList);
        spinnerCrop.setAdapter(spinnerCropAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove listener (if exists)
        if (knowledgeListener != null) {
            knowledgeListener.remove();
            knowledgeListener = null;
        }
    }

    private void setup() {
        // Link XML to Java
        svKnowledge = findViewById(R.id.svKnowledge);
        linearLayoutNoKnowledge = findViewById(R.id.linearLayoutNoKnowledge);
        rvKnowledge = findViewById(R.id.rvKnowledge);
        spinnerCrop = findViewById(R.id.spinnerCrop);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvKnowledge.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        knowledgeAdapter = new KnowledgeAdapter(filteredKnowledgeList, false);
        rvKnowledge.setAdapter(knowledgeAdapter);

        // Add listeners
        svKnowledge.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                title = newText;
                filterKnowledge();

                return true;
            }
        });
        spinnerCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterKnowledge();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getKnowledge() {
        // Connect to Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("knowledge");

        // Get all knowledge sorted by title
        knowledgeListener = collectionReference.whereEqualTo("deleted", null)
                .orderBy("title")
                .addSnapshotListener((value, error) -> {
                    // Clear all lists
                    filteredKnowledgeList.clear();
                    knowledgeList.clear();

                    for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(value).getDocuments()) {
                        Knowledge knowledge = documentSnapshot.toObject(Knowledge.class);
                        knowledgeList.add(knowledge);
                        filteredKnowledgeList.add(knowledge);
                    }

                    filterKnowledge();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterKnowledge() {
        // Filter knowledge base on the title
        String crop = spinnerCrop.getSelectedItem().toString();
        filteredKnowledgeList.clear();

        for (Knowledge knowledge : knowledgeList) {
            if (knowledge.getTitle().toLowerCase().contains(title.toLowerCase())) {
                if (crop.equals(getResources().getString(R.string.all))) filteredKnowledgeList.add(knowledge);
                else if (knowledge.getCrops().contains(crop)) filteredKnowledgeList.add(knowledge);
            }
        }

        // Notify about the changes on the list
        knowledgeAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        // Update visibility of the recycler view and linear layout
        if (filteredKnowledgeList.size() == 0) {
            linearLayoutNoKnowledge.setVisibility(View.VISIBLE);
            rvKnowledge.setVisibility(View.GONE);
        } else {
            rvKnowledge.setVisibility(View.VISIBLE);
            linearLayoutNoKnowledge.setVisibility(View.GONE);
        }
    }

}
