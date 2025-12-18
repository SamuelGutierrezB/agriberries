package com.agriberriesmx.agriberries.Admin;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Plague;
import com.agriberriesmx.agriberries.POJO.Treatment;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UpdateProductAdmin extends AppCompatActivity {
    private Treatment treatment;
    private MenuItem deleteRestoreItem;
    private TextAdapter plagueAdapter;
    private final List<String> plagues = new ArrayList<>();
    private final List<String> treatments = new ArrayList<>();
    private EditText etvName, etvGroup, etvHarvest, etvMinAmount, etvMaxAmount,
            etvQuantityPresentation, etvMaxApplications, etvPrice;
    private Spinner spinnerIngredient, spinnerPlague, spinnerMeasurementUnit, spinnerMeasurementUnitPresentation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_update_product);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        etvName = findViewById(R.id.etvName);
        etvGroup = findViewById(R.id.etvGroup);
        etvHarvest = findViewById(R.id.etvHarvest);
        etvMinAmount = findViewById(R.id.etvMinAmount);
        etvMaxAmount = findViewById(R.id.etvMaxAmount);
        etvQuantityPresentation = findViewById(R.id.etvQuantityPresentation);
        etvMaxApplications = findViewById(R.id.etvMaxApplications);
        etvPrice = findViewById(R.id.etvPrice);
        spinnerIngredient = findViewById(R.id.spinnerIngredient);
        spinnerPlague = findViewById(R.id.spinnerPlague);
        spinnerMeasurementUnit = findViewById(R.id.spinnerMeasurementUnit);
        spinnerMeasurementUnitPresentation = findViewById(R.id.spinnerMeasurementUnitPresentation);
        RecyclerView rvPlagues = findViewById(R.id.rvPlagues);
        ImageButton btnAddPlague = findViewById(R.id.btnAddPlague);
        Button btnUpdateTreatment = findViewById(R.id.btnUpdateTreatment);

        // Get treatment and initialize plagues
        treatment = getIntent().getParcelableExtra("treatment");
        plagues.addAll(treatment.getPlagues());

        // Initialize toolbar
        setSupportActionBar(toolbar);
        setTitle("");
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

        // Get plague names
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        List<Plague> plagueList = agriBerries.getGlobalPlagueList();
        List<String> plagueNameList = new ArrayList<>();
        for (Plague plague : plagueList) if (plague.getDeleted() == null)
            plagueNameList.add(Formatting.capitalizeFirstLetter(plague.getName()));

        // Get treatment names
        List<Treatment> treatmentList = agriBerries.getGlobalTreatmentList();
        for (Treatment treatment : treatmentList) treatments.add(treatment.getName());

        // Initialize recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        plagueAdapter = new TextAdapter(plagues);
        rvPlagues.setLayoutManager(linearLayoutManager);
        rvPlagues.setAdapter(plagueAdapter);

        // Create spinners adapter
        ArrayAdapter<String> plaguesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plagueNameList);
        plaguesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlague.setAdapter(plaguesAdapter);
        List<String> ingredientsList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ingredients)));
        ingredientsList.remove(0);
        ArrayAdapter<String> ingredientsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ingredientsList);
        ingredientsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIngredient.setAdapter(ingredientsAdapter);
        ArrayAdapter<CharSequence> measurementUnitsAdapter = ArrayAdapter.createFromResource(this, R.array.measurementUnits, android.R.layout.simple_spinner_item);
        measurementUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurementUnit.setAdapter(measurementUnitsAdapter);
        ArrayAdapter<CharSequence> measurementUnitsAdapterPresentation = ArrayAdapter.createFromResource(this, R.array.measurementUnits, android.R.layout.simple_spinner_item);
        measurementUnitsAdapterPresentation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurementUnitPresentation.setAdapter(measurementUnitsAdapterPresentation);

        // Initialize views
        etvName.setText(treatment.getName());
        etvGroup.setText(treatment.getGroup());
        etvHarvest.setText(String.valueOf(treatment.getHarvest()));
        etvMinAmount.setText(String.valueOf(treatment.getMinAmount()));
        etvMaxAmount.setText(String.valueOf(treatment.getMaxAmount()));
        etvQuantityPresentation.setText(String.valueOf(treatment.getQuantityPresentation()));
        etvMaxApplications.setText(String.valueOf(treatment.getMaxApplications()));
        etvPrice.setText(String.valueOf(treatment.getPrice()));
        spinnerIngredient.setSelection(ingredientsAdapter.getPosition(treatment.getIngredient()));
        spinnerMeasurementUnit.setSelection(measurementUnitsAdapter.getPosition(treatment.getUnit()));
        spinnerMeasurementUnitPresentation.setSelection(measurementUnitsAdapter.getPosition(treatment.getUnitPresentation()));

        // Enable swipe to delete and undo
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                plagues.remove(position);
                plagueAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvPlagues);

        // Add listeners
        btnAddPlague.setOnClickListener(v -> addPlague());
        btnUpdateTreatment.setOnClickListener(v -> updateTreatment());
    }

    private void addPlague() {
        // Add plague to the list and update adapter
        String plague = spinnerPlague.getSelectedItem().toString().toUpperCase();

        if (!plague.isEmpty()) {
            if (!plagues.contains(plague)) {
                int position = Collections.binarySearch(plagues, plague);

                // Get insert position for alphabetic order
                if (position < 0) position = -(position + 1);

                // Add element
                plagues.add(position, plague);
                plagueAdapter.notifyItemInserted(position);
            } else Toast.makeText(this, getResources().getString(R.string.plagueAlreadyAdded), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.plagueNotAdd), Toast.LENGTH_SHORT).show();
    }

    private void updateTreatment() {
        // Get attributes to verify that they are not empty
        String name = etvName.getText().toString().trim().toUpperCase();
        String group = etvGroup.getText().toString().trim();
        String ingredient = spinnerIngredient.getSelectedItem().toString();
        String harvestText = etvHarvest.getText().toString().trim();
        String unit = spinnerMeasurementUnit.getSelectedItem().toString();
        String unitPresentation = spinnerMeasurementUnitPresentation.getSelectedItem().toString();
        String minAmountText = etvMinAmount.getText().toString().trim();
        String maxAmountText = etvMaxAmount.getText().toString().trim();
        String quantityPresentationText = etvQuantityPresentation.getText().toString().trim();
        String maxApplicationsText = etvMaxApplications.getText().toString().trim();
        String priceText = etvPrice.getText().toString().trim();

        if (!name.isEmpty() && !group.isEmpty() && !harvestText.isEmpty() &&
                !minAmountText.isEmpty() && !maxAmountText.isEmpty()  &&
                !quantityPresentationText.isEmpty() && !maxApplicationsText.isEmpty() &&
                !priceText.isEmpty() && !plagues.isEmpty()) {
            if (name.equals(treatment.getName()) || !treatments.contains(name)) {
                // Connect to Firebase Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection("treatments").document(treatment.getId());

                // Create treatment info
                treatment.setName(name);
                treatment.setGroup(group);
                treatment.setIngredient(ingredient);
                treatment.setUnit(unit);
                treatment.setUnitPresentation(unitPresentation);
                treatment.setMaxApplications(Integer.parseInt(maxApplicationsText));
                treatment.setHarvest(Integer.parseInt(harvestText));
                treatment.setMinAmount(Double.parseDouble(minAmountText));
                treatment.setMaxAmount(Double.parseDouble(maxAmountText));
                treatment.setQuantityPresentation(Double.parseDouble(quantityPresentationText));
                treatment.setPrice(Double.parseDouble(priceText));
                treatment.setPlagues(plagues);

                // Save new treatment info into database
                documentReference.set(treatment);
                Toast.makeText(this, getResources().getString(R.string.productUpdated), Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, getResources().getString(R.string.error_product_already_created), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    private void showDeleteTreatmentDialog() {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

        // Link XML to Java
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Initialize views
        if (treatment.getDeleted() == null) {
            // To delete
            tvMessage.setText(getResources().getString(R.string.askDeleteProduct));
            btnPositive.setText(getResources().getString(R.string.delete));
        } else {
            // To restore
            tvMessage.setText(getResources().getString(R.string.askRestoreProduct));
            btnPositive.setText(getResources().getString(R.string.restore));
        }

        // Create Alert Dialog to ask confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Add listeners
        btnPositive.setOnClickListener(v -> {
            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("treatments").document(treatment.getId());

            if (treatment.getDeleted() == null) {
                // Delete crop
                documentReference.update("deleted", Formatting.addSevenDaysAndFormat(new Date()));
                treatment.setDeleted(Formatting.addSevenDaysAndFormat(new Date()));
                deleteRestoreItem.setTitle(getResources().getString(R.string.restore));
                Toast.makeText(UpdateProductAdmin.this, getResources().getString(R.string.productDeleted), Toast.LENGTH_SHORT).show();
            } else {
                // Restore crop
                documentReference.update("deleted", null);
                treatment.setDeleted(null);
                deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
                Toast.makeText(UpdateProductAdmin.this, getResources().getString(R.string.productRestored), Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Show Alert Dialog
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.deleteRestore) {
            showDeleteTreatmentDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        // Change text
        deleteRestoreItem = menu.findItem(R.id.deleteRestore);
        if (treatment.getDeleted() == null)
            deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
        else
            deleteRestoreItem.setTitle(getResources().getString(R.string.restore));

        return true;
    }

}
