package com.agriberriesmx.agriberries.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.ConsultantAdapter;
import com.agriberriesmx.agriberries.Adapter.SpinnerStatusAdapter;
import com.agriberriesmx.agriberries.POJO.Consultant;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.Firestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ManageConsultantsAdmin extends AppCompatActivity {
    private ListenerRegistration consultantListener;
    private ConsultantAdapter consultantAdapter;
    private final List<Consultant> consultantList = new ArrayList<>();
    private final List<Consultant> filteredConsultantList = new ArrayList<>();
    private SearchView svConsultants;
    private LinearLayout linearLayoutNoConsultants;
    private RecyclerView rvConsultants;
    private String name = "";
    private boolean blocked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_consultants);
        Firestore.deleteDocuments("consultants", "deleted");
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get consultants info and clear focus
        svConsultants.clearFocus();
        if (consultantListener == null) getConsultants();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove listener (if exists)
        if (consultantListener != null) {
            consultantListener.remove();
            consultantListener = null;
        }
    }

    private void setup() {
        //Link XML to Java
        svConsultants = findViewById(R.id.svConsultants);
        linearLayoutNoConsultants = findViewById(R.id.linearLayoutNoConsultants);
        rvConsultants = findViewById(R.id.rvConsultants);
        Spinner spinnerBlocked = findViewById(R.id.spinnerBlocked);
        Button btnCreateConsultant = findViewById(R.id.btnCreateConsultant);

        // Get string array for spinner
        List<String> blockedValues = Arrays.asList(getResources().getStringArray(R.array.blocked));
        SpinnerStatusAdapter adapterBlocked = new SpinnerStatusAdapter(this, blockedValues);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvConsultants.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        consultantAdapter = new ConsultantAdapter(filteredConsultantList);
        rvConsultants.setAdapter(consultantAdapter);

        // Initialize views
        spinnerBlocked.setAdapter(adapterBlocked);

        //Add listeners
        svConsultants.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                name = newText;
                filterConsultants();

                return true;
            }
        });
        spinnerBlocked.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Change blocked status
                blocked = position != 0;
                getConsultants();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnCreateConsultant.setOnClickListener(v -> startActivity(new Intent(this, CreateConsultantAdmin.class)));
    }

    private void getConsultants() {
        // Connect to Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("consultants");

        // Get all consultants sorted by name
        consultantListener = collectionReference.whereEqualTo("blocked", blocked)
                .orderBy("deleted", Query.Direction.DESCENDING)
                .orderBy("name")
                .addSnapshotListener((value, error) -> {
                    // Clear all lists
                    filteredConsultantList.clear();
                    consultantList.clear();

                    for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(value).getDocuments()) {
                        Consultant consultant = documentSnapshot.toObject(Consultant.class);
                        consultantList.add(consultant);
                        filteredConsultantList.add(consultant);
                    }

                    filterConsultants();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterConsultants() {
        // Filter consultants base on the name
        filteredConsultantList.clear();

        for (Consultant consultant : consultantList) {
            if (consultant.getName().toLowerCase().contains(name.toLowerCase()))
                filteredConsultantList.add(consultant);
        }

        // Notify about the changes on the list
        consultantAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        // Update visibility of the recycler view and linear layout
        if (filteredConsultantList.size() == 0) {
            linearLayoutNoConsultants.setVisibility(View.VISIBLE);
            rvConsultants.setVisibility(View.GONE);
        } else {
            rvConsultants.setVisibility(View.VISIBLE);
            linearLayoutNoConsultants.setVisibility(View.GONE);
        }
    }

}
