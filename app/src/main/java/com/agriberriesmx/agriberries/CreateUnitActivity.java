package com.agriberriesmx.agriberries;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.agriberriesmx.agriberries.POJO.Block;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateUnitActivity extends AppCompatActivity {
    private static final int rows = 5;
    private static final int cols = 5;
    private static final int TIME = 2000;
    private final List<Block> blocks = new ArrayList<>();
    private final Button[][] gridButtons = new Button[rows][cols];
    private TextView tvCoordinates;
    private EditText etvName, etvLocation, etvAltitude, etvHectares;
    private Spinner spinnerState, spinnerCrop, spinnerSoil, spinnerManagement, spinnerModality;
    private DocumentReference unitDocumentReference;
    private int previousCropPosition = 0;
    private double latitude = -1000, longitude = -1000;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_unit);
        setup();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // The back button was pressed again
            super.onBackPressed();

            return;
        }

        // Message to the user
        Toast.makeText(this, getResources().getString(R.string.press_back_once_again), Toast.LENGTH_LONG).show();

        // Was pressed once
        doubleBackToExitPressedOnce = true;

        // Timer to return boolean variable to its default value
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, TIME);
    }

    private void setup() {
        // Link XML to Java
        tvCoordinates = findViewById(R.id.tvCoordinates);
        etvName = findViewById(R.id.etvName);
        spinnerState = findViewById(R.id.spinnerState);
        etvLocation = findViewById(R.id.etvLocation);
        etvAltitude = findViewById(R.id.etvAltitude);
        etvHectares = findViewById(R.id.etvHectares);
        spinnerCrop = findViewById(R.id.spinnerCrop);
        spinnerSoil = findViewById(R.id.spinnerSoil);
        spinnerManagement = findViewById(R.id.spinnerManagement);
        spinnerModality = findViewById(R.id.spinnerModality);
        GridLayout blocksLayout = findViewById(R.id.blocksLayout);
        ImageButton btnEditLocation = findViewById(R.id.btnEditLocation);
        Button btnCreateUnit = findViewById(R.id.btnCreateUnit);

        // Get crop list name
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        List<Crop> cropList = agriBerries.getGlobalCropList();
        List<String> cropNameList = new ArrayList<>();
        for (Crop crop : cropList) cropNameList.add(crop.getName());

        // Create spinner adapters
        ArrayAdapter<CharSequence> adapterState = ArrayAdapter.createFromResource(CreateUnitActivity.this, R.array.mexican_states, android.R.layout.simple_spinner_item);
        adapterState.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterCrop = new ArrayAdapter<>(CreateUnitActivity.this, android.R.layout.simple_spinner_item, cropNameList);
        adapterCrop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapterSoil = ArrayAdapter.createFromResource(CreateUnitActivity.this, R.array.soil, android.R.layout.simple_spinner_item);
        adapterSoil.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapterManagement = ArrayAdapter.createFromResource(CreateUnitActivity.this, R.array.management, android.R.layout.simple_spinner_item);
        adapterManagement.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapterModality = ArrayAdapter.createFromResource(CreateUnitActivity.this, R.array.modality, android.R.layout.simple_spinner_item);
        adapterModality.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Initialize views
        spinnerState.setAdapter(adapterState);
        spinnerCrop.setAdapter(adapterCrop);
        spinnerSoil.setAdapter(adapterSoil);
        spinnerManagement.setAdapter(adapterManagement);
        spinnerModality.setAdapter(adapterModality);

        // Initialize variables
        String clientId = getIntent().getStringExtra("clientId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        unitDocumentReference = db.collection("clients").document(clientId)
                .collection("units").document();

        // Initialize blocks layout
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Save button into array
                gridButtons[row][col] = new Button(CreateUnitActivity.this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(col, 1f);
                params.rowSpec = GridLayout.spec(row, 1f);
                gridButtons[row][col].setLayoutParams(params);
                gridButtons[row][col].setBackgroundResource(R.drawable.bg_button);
                int finalRow = row;
                int finalCol = col;

                gridButtons[row][col].setOnClickListener(v -> {
                    // Create special Alert Dialog to create block
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateUnitActivity.this, R.style.CustomAlertDialog);

                    // Inflate XML design
                    View popupView = getLayoutInflater().inflate(R.layout.popup_block, null);
                    builder.setView(popupView);

                    // Link XML to Java
                    LinearLayout linearLayoutPlantation = popupView.findViewById(R.id.linearLayoutPlantation);
                    LinearLayout linearLayoutHarvest = popupView.findViewById(R.id.linearLayoutHarvest);
                    TextView tvPlantation = popupView.findViewById(R.id.tvPlantation);
                    TextView tvHarvest = popupView.findViewById(R.id.tvHarvest);
                    EditText etvBlock = popupView.findViewById(R.id.etvBlock);
                    EditText etvFurrowDistance = popupView.findViewById(R.id.etvFurrowDistance);
                    EditText etvPlantDistance = popupView.findViewById(R.id.etvPlantDistance);
                    Spinner spinnerTypes = popupView.findViewById(R.id.spinnerTypes);
                    Button btnSaveBlock = popupView.findViewById(R.id.btnSaveBlock);
                    Button btnDeleteBlock = popupView.findViewById(R.id.btnDeleteBlock);

                    // Initialize calendar plantation
                    Calendar calendarPlantation = Calendar.getInstance();
                    calendarPlantation.set(Calendar.HOUR_OF_DAY, 0);
                    calendarPlantation.set(Calendar.MINUTE, 0);
                    calendarPlantation.set(Calendar.SECOND, 0);
                    calendarPlantation.set(Calendar.MILLISECOND, 0);

                    // Initialize calendar harvest
                    Calendar calendarHarvest = Calendar.getInstance();
                    calendarHarvest.set(Calendar.HOUR_OF_DAY, 23);
                    calendarHarvest.set(Calendar.MINUTE, 59);
                    calendarHarvest.set(Calendar.SECOND, 59);
                    calendarHarvest.set(Calendar.MILLISECOND, 999);

                    // Create type adapter
                    ArrayAdapter<String> adapterType = new ArrayAdapter<>(CreateUnitActivity.this, android.R.layout.simple_spinner_item, cropList.get(spinnerCrop.getSelectedItemPosition()).getTypes());
                    adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTypes.setAdapter(adapterType);

                    // Get block info (if exists)
                    for (Block block : blocks) {
                        if (block.getRow() == finalRow && block.getCol() == finalCol) {
                            // Block found (initialize view)
                            calendarPlantation.setTime(block.getPlantationDate());
                            calendarHarvest.setTime(block.getHarvestDate());
                            etvBlock.setText(block.getName());
                            etvFurrowDistance.setText(String.valueOf(block.getFurrowDistance()));
                            etvPlantDistance.setText(String.valueOf(block.getPlantDistance()));
                            spinnerTypes.setSelection(adapterType.getPosition(block.getType()));
                            btnDeleteBlock.setVisibility(View.VISIBLE);
                        }
                    }

                    // Initialize views
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    tvPlantation.setText(sdf.format(calendarPlantation.getTime()));
                    tvHarvest.setText(sdf.format(calendarHarvest.getTime()));

                    // Add listeners to linear layouts
                    linearLayoutPlantation.setOnClickListener(v1 -> {
                        // Create a DatePickerDialog
                        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateUnitActivity.this, (view, year, month, dayOfMonth) -> {
                            // Update calendar plantation variable and UI
                            calendarPlantation.set(year, month, dayOfMonth);
                            tvPlantation.setText(sdf.format(calendarPlantation.getTime()));
                        }, calendarPlantation.get(Calendar.YEAR), calendarPlantation.get(Calendar.MONTH), calendarPlantation.get(Calendar.DAY_OF_MONTH));

                        // Show DatePickerDialog
                        datePickerDialog.show();
                    });

                    linearLayoutHarvest.setOnClickListener(v1 -> {
                        // Create a DatePickerDialog
                        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateUnitActivity.this, (view, year, month, dayOfMonth) -> {
                            // Update calendar harvest variable and UI
                            calendarHarvest.set(year, month, dayOfMonth);
                            tvHarvest.setText(sdf.format(calendarHarvest.getTime()));
                        }, calendarHarvest.get(Calendar.YEAR), calendarHarvest.get(Calendar.MONTH), calendarHarvest.get(Calendar.DAY_OF_MONTH));

                        // Show DatePickerDialog
                        datePickerDialog.show();
                    });

                    // Add negative button to cancel and create dialog
                    builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
                    AlertDialog dialogBlock = builder.create();

                    // Add listeners to buttons
                    btnSaveBlock.setOnClickListener(v1 -> {
                        // Get variable to verify emptiness
                        String name = etvBlock.getText().toString().trim().toUpperCase();
                        String furrowDistanceText = etvFurrowDistance.getText().toString().trim();
                        String plantDistanceText = etvPlantDistance.getText().toString().trim();
                        String type = spinnerTypes.getSelectedItem().toString();

                        if (!name.isEmpty() && !furrowDistanceText.isEmpty() && !plantDistanceText.isEmpty()) {
                            // Verify the correct format of the dates
                            if (calendarHarvest.compareTo(calendarPlantation) > 0) {
                                // Verify if the name is already in used
                                boolean used = false;
                                for (Block block : blocks) {
                                    if (block.getName().equals(name)) {
                                        used = true;
                                        break;
                                    }
                                }

                                if (!used) {
                                    // Create new block
                                    String id =  unitDocumentReference.collection("blocks").document().getId();
                                    Block block = new Block();
                                    block.setId(id);
                                    block.setName(name);
                                    block.setFurrowDistance(Double.parseDouble(furrowDistanceText));
                                    block.setPlantDistance(Double.parseDouble(plantDistanceText));
                                    block.setType(type);
                                    block.setRow(finalRow);
                                    block.setCol(finalCol);
                                    block.setPlantationDate(calendarPlantation.getTime());
                                    block.setHarvestDate(calendarHarvest.getTime());

                                    // Add created block to List and change button text
                                    blocks.add(block);
                                    gridButtons[finalRow][finalCol].setText(name);

                                    // Clear focus
                                    etvName.clearFocus();
                                    etvHectares.clearFocus();
                                    etvLocation.clearFocus();

                                    // Inform user
                                    Toast.makeText(CreateUnitActivity.this, getResources().getString(R.string.blockSaved), Toast.LENGTH_SHORT).show();
                                    dialogBlock.dismiss();
                                } else Toast.makeText(CreateUnitActivity.this, getResources().getString(R.string.block_name_already_used), Toast.LENGTH_SHORT).show();
                            } else Toast.makeText(CreateUnitActivity.this, getResources().getString(R.string.second_date_must_be_greater), Toast.LENGTH_SHORT).show();
                        } else Toast.makeText(CreateUnitActivity.this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
                    });

                    btnDeleteBlock.setOnClickListener(v2 -> {
                        // Delete block info based on position
                        blocks.removeIf(block -> block.getRow() == finalRow && block.getCol() == finalCol);
                        gridButtons[finalRow][finalCol].setText("");
                        Toast.makeText(CreateUnitActivity.this, getResources().getString(R.string.blockDeleted), Toast.LENGTH_SHORT).show();

                        dialogBlock.dismiss();
                    });

                    dialogBlock.show();
                });
                blocksLayout.addView(gridButtons[row][col]);
            }
        }

        // Add listeners
        spinnerCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (blocks.size() > 0 && position != previousCropPosition) {
                    // Return to previous crop and show Alert Dialog
                    spinnerCrop.setSelection(previousCropPosition);

                    // Inflate custom content dialog
                    LayoutInflater inflater = LayoutInflater.from(CreateUnitActivity.this);
                    View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

                    // Link XML to Java
                    TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
                    Button btnPositive = dialogView.findViewById(R.id.btnPositive);
                    Button btnNegative = dialogView.findViewById(R.id.btnNegative);

                    // Initialize views
                    tvMessage.setText(getResources().getString(R.string.askCropChange));
                    btnPositive.setText(getResources().getString(R.string.confirm));

                    // Create Alert Dialog to ask confirmation
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateUnitActivity.this, R.style.CustomAlertDialog);
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    // Add listeners
                    btnPositive.setOnClickListener(v -> {
                        // Clear list and go to new spinner position
                        blocks.clear();
                        spinnerCrop.setSelection(position);
                        previousCropPosition = position;

                        // Clear buttons' text
                        for (int counter = 0; counter < rows; counter++)
                            for (int auxCounter = 0; auxCounter < cols; auxCounter++)
                                gridButtons[counter][auxCounter].setText("");

                        dialog.dismiss();
                    });
                    btnNegative.setOnClickListener(v -> dialog.dismiss());

                    // Show Alert Dialog
                    dialog.show();
                } else previousCropPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnEditLocation.setOnClickListener(v -> {
            // Create new intent and start it
            Intent intent = new Intent(this, MapActivity.class);
            String location = etvLocation.getText().toString().trim();
            if (!location.isEmpty()) intent.putExtra("location", location);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            mapActivityResultLauncher.launch(intent);
        });
        btnCreateUnit.setOnClickListener(v -> createUnit());
    }

    // Create activity launcher
    private final ActivityResultLauncher<Intent> mapActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Get latitude and longitude
                    latitude = result.getData().getDoubleExtra("latitude", -1000);
                    longitude = result.getData().getDoubleExtra("longitude", -1000);

                    // Show coordinates
                    String coordinates = latitude + ", " + longitude;
                    tvCoordinates.setText(coordinates);
                }
            }
    );

    private void createUnit() {
        // Get attributes to verify that they are not empty
        String name = Formatting.capitalizeName(etvName.getText().toString().trim());
        String state = spinnerState.getSelectedItem().toString();
        String location = etvLocation.getText().toString().trim();
        String textAltitude = etvAltitude.getText().toString().trim();
        String crop = spinnerCrop.getSelectedItem().toString();
        String soil = spinnerSoil.getSelectedItem().toString();
        String management = spinnerManagement.getSelectedItem().toString();
        String modality = spinnerModality.getSelectedItem().toString();
        String textHectares = etvHectares.getText().toString().trim();

        if (!name.isEmpty() && !location.isEmpty() && !textAltitude.isEmpty() && !textHectares.isEmpty() && !blocks.isEmpty()) {
            // Create batch
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            WriteBatch batch = db.batch();

            // Create new unit info
            Unit unit = new Unit();
            unit.setId(unitDocumentReference.getId());
            unit.setName(name);
            unit.setState(state);
            unit.setLocation(location);
            unit.setAltitude(Double.parseDouble(textAltitude));
            unit.setHectares(Double.parseDouble(textHectares));
            unit.setCrop(crop);
            unit.setSoil(soil);
            unit.setManagement(management);
            unit.setModality(modality);
            unit.setLatitude(latitude);
            unit.setLongitude(longitude);
            unit.setDeleted(null);

            // Create all blocks
            for (Block block : blocks) {
                // Get document reference and add it to the batch
                DocumentReference documentReference = unitDocumentReference
                        .collection("blocks").document(block.getId());
                batch.set(documentReference, block);
            }

            // Save new unit info into database
            batch.set(unitDocumentReference, unit);
            batch.commit();
            Toast.makeText(CreateUnitActivity.this, getResources().getString(R.string.unitCreated), Toast.LENGTH_SHORT).show();
            finish();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();

    }

}
