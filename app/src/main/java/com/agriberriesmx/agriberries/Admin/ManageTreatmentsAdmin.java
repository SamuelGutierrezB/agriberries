package com.agriberriesmx.agriberries.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.TreatmentAdapter;
import com.agriberriesmx.agriberries.POJO.Treatment;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.Firestore;

import java.util.ArrayList;
import java.util.List;

public class ManageTreatmentsAdmin extends AppCompatActivity {
    private TreatmentAdapter treatmentAdapter;
    private final List<Treatment> treatmentList = new ArrayList<>();
    private final List<Treatment> filteredTreatmentList = new ArrayList<>();
    private SearchView svTreatments;
    private Spinner spinnerIngredient;
    private LinearLayout linearLayoutNoTreatments;
    private RecyclerView rvTreatments;
    private String name = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firestore.deleteDocuments("treatments", "deleted");
        setContentView(R.layout.admin_manage_treatments);
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get treatments and update lists
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        treatmentList.clear();
        treatmentList.addAll(agriBerries.getGlobalTreatmentList());
        filteredTreatmentList.clear();
        filteredTreatmentList.addAll(agriBerries.getGlobalTreatmentList());

        // Filter treatments and clear focus
        filterTreatments();
        svTreatments.clearFocus();
    }

    private void setup() {
        // Link XML to Java
        svTreatments = findViewById(R.id.svTreatments);
        linearLayoutNoTreatments = findViewById(R.id.linearLayoutNoTreatments);
        rvTreatments = findViewById(R.id.rvTreatments);
        spinnerIngredient = findViewById(R.id.spinnerIngredient);
        Button btnCreateTreatment = findViewById(R.id.btnCreateTreatment);

        // Get string array for spinner
        ArrayAdapter<CharSequence> adapterIngredients = ArrayAdapter.createFromResource(this, R.array.ingredients, android.R.layout.simple_spinner_item);
        adapterIngredients.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIngredient.setAdapter(adapterIngredients);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvTreatments.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        treatmentAdapter = new TreatmentAdapter(filteredTreatmentList);
        rvTreatments.setAdapter(treatmentAdapter);

        // Add listeners
        svTreatments.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                name = newText;
                filterTreatments();

                return true;
            }
        });
        spinnerIngredient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterTreatments();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnCreateTreatment.setOnClickListener(v -> startActivity(new Intent(this, CreateProductAdmin.class)));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterTreatments() {
        // Filter treatments base on the ingredient
        String ingredient = spinnerIngredient.getSelectedItem().toString();
        filteredTreatmentList.clear();

        for (Treatment treatment : treatmentList) {
            if (treatment.getName().toLowerCase().contains(name.toLowerCase())) {
                if (ingredient.equals(getResources().getString(R.string.all))) filteredTreatmentList.add(treatment);
                else if (treatment.getIngredient().contains(ingredient)) filteredTreatmentList.add(treatment);
            }
        }

        // Notify about the changes on the list
        treatmentAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        // Update visibility of the recycler view and linear layout
        if (filteredTreatmentList.size() == 0) {
            linearLayoutNoTreatments.setVisibility(View.VISIBLE);
            rvTreatments.setVisibility(View.GONE);
        } else {
            rvTreatments.setVisibility(View.VISIBLE);
            linearLayoutNoTreatments.setVisibility(View.GONE);
        }
    }

}
