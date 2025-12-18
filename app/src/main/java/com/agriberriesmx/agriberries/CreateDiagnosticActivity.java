package com.agriberriesmx.agriberries;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Block;
import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Item;
import com.agriberriesmx.agriberries.POJO.Plague;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.CustomSeekBarLevelAdd;
import com.agriberriesmx.agriberries.Utils.CustomSeekBarPercentage;
import com.agriberriesmx.agriberries.Utils.CustomSeekBarPercentageAdd;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CreateDiagnosticActivity extends AppCompatActivity {
    private static final int rows = 5;
    private static final int cols = 5;
    private Diagnostic diagnostic;
    private Unit unit;
    private DocumentReference diagnosticReference;
    private TextAdapter plagueAdapter, deficiencyAdapter, contingencyAdapter, activityAdapter, taskAdapter;
    private ArrayAdapter<CharSequence> adapterPhenologies, adapterFruitSizes;
    private final List<Block> unitBlocks = new ArrayList<>();
    private final List<Item> diagnosticItems = new ArrayList<>();
    private final List<String> plagues = new ArrayList<>();
    private final List<String> deficiencies = new ArrayList<>();
    private final List<String> contingencies = new ArrayList<>();
    private final List<String> activities = new ArrayList<>();
    private final List<String> tasks = new ArrayList<>();
    private final Button[][] gridButtons = new Button[rows][cols];
    private LinearLayout linearLayoutDone, linearLayoutHarvest;
    private ScrollView scrollView;
    private TextView tvType;
    private EditText etvHeight, etvLongitude, etvWeeklyGrowing, etvNote, etvFruitPerPlantMeter, etvGramPerFruit;
    private CheckBox checkBoxPlague, checkBoxDeficiency, checkBoxContingency, checkBoxActivity, checkBoxTask;
    private Spinner spinnerPhenology, spinnerFruitSize;
    private CustomSeekBarPercentage seekBarShrinkagePercentage;
    private Button btnNext;
    private int position;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_diagnostic);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        linearLayoutDone = findViewById(R.id.linearLayoutDone);
        linearLayoutHarvest = findViewById(R.id.linearLayoutHarvest);
        scrollView = findViewById(R.id.scrollView);
        RecyclerView rvPlagues = findViewById(R.id.rvPlagues);
        RecyclerView rvDeficiencies = findViewById(R.id.rvDeficiencies);
        RecyclerView rvContingencies = findViewById(R.id.rvContingencies);
        RecyclerView rvActivities = findViewById(R.id.rvActivities);
        RecyclerView rvTasks = findViewById(R.id.rvTasks);
        GridLayout blocksLayout = findViewById(R.id.blocksLayout);
        TextView tvShrinkagePercentage = findViewById(R.id.tvShrinkagePercentage);
        tvType = findViewById(R.id.tvType);
        etvHeight = findViewById(R.id.etvHeight);
        etvLongitude = findViewById(R.id.etvLongitude);
        etvWeeklyGrowing = findViewById(R.id.etvWeeklyGrowing);
        etvNote = findViewById(R.id.etvNote);
        etvFruitPerPlantMeter = findViewById(R.id.etvFruitPerPlantMeter);
        etvGramPerFruit = findViewById(R.id.etvGramPerFruit);
        spinnerPhenology = findViewById(R.id.spinnerPhenology);
        spinnerFruitSize = findViewById(R.id.spinnerFruitSize);
        checkBoxPlague = findViewById(R.id.checkBoxPlague);
        checkBoxDeficiency = findViewById(R.id.checkBoxDeficiency);
        checkBoxContingency = findViewById(R.id.checkBoxContingency);
        checkBoxActivity = findViewById(R.id.checkBoxActivity);
        checkBoxTask = findViewById(R.id.checkBoxTask);
        seekBarShrinkagePercentage = findViewById(R.id.seekBarShrinkagePercentage);
        ImageButton btnAddPlague = findViewById(R.id.btnAddPlague);
        ImageButton btnAddDeficiency = findViewById(R.id.btnAddDeficiency);
        ImageButton btnAddContingency = findViewById(R.id.btnAddContingency);
        ImageButton btnAddActivity = findViewById(R.id.btnAddActivity);
        ImageButton btnAddTask = findViewById(R.id.btnAddTask);
        Button btnCreateDiagnosticItem = findViewById(R.id.btnCreateDiagnosticItem);
        btnNext = findViewById(R.id.btnNext);

        // Initialize toolbar
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setTitleTextColor(getColor(R.color.white));
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

        // Get diagnostic, unit, client id and diagnostic id (if exists)
        diagnostic = getIntent().getParcelableExtra("diagnostic");
        unit = getIntent().getParcelableExtra("unit");
        String clientId = getIntent().getStringExtra("clientId");

        // Connect to Firebase Firestore and get diagnostic reference
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (diagnostic == null) diagnosticReference = db.collection("clients").document(clientId).collection("diagnostics").document();
        else diagnosticReference = db.collection("clients").document(clientId).collection("diagnostics").document(diagnostic.getId());

        // Create spinner adapters phenology
        adapterPhenologies = ArrayAdapter.createFromResource(this, R.array.phenologies, R.layout.spinner_item);
        adapterPhenologies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create spinner adapters fruit sizes
        adapterFruitSizes = ArrayAdapter.createFromResource(this, R.array.fruit_sizes, android.R.layout.simple_spinner_item);
        adapterFruitSizes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create layout manager and adapter for plagues
        LinearLayoutManager linearLayoutManagerPlagues = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        plagueAdapter = new TextAdapter(plagues);
        rvPlagues.setLayoutManager(linearLayoutManagerPlagues);
        rvPlagues.setAdapter(plagueAdapter);

        // Create layout manager and adapter for deficiencies
        LinearLayoutManager linearLayoutManagerDeficiencies = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        deficiencyAdapter = new TextAdapter(deficiencies);
        rvDeficiencies.setLayoutManager(linearLayoutManagerDeficiencies);
        rvDeficiencies.setAdapter(deficiencyAdapter);

        // Create layout manager and adapter for contingencies
        LinearLayoutManager linearLayoutManagerContingencies = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        contingencyAdapter = new TextAdapter(contingencies);
        rvContingencies.setLayoutManager(linearLayoutManagerContingencies);
        rvContingencies.setAdapter(contingencyAdapter);

        // Create layout manager and adapter for activities
        LinearLayoutManager linearLayoutManagerActivities = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        activityAdapter = new TextAdapter(activities);
        rvActivities.setLayoutManager(linearLayoutManagerActivities);
        rvActivities.setAdapter(activityAdapter);

        // Create layout manager and adapter for tasks
        LinearLayoutManager linearLayoutManagerTasks = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        taskAdapter = new TextAdapter(tasks);
        rvTasks.setLayoutManager(linearLayoutManagerTasks);
        rvTasks.setAdapter(taskAdapter);

        // Initialize spinners
        spinnerPhenology.setAdapter(adapterPhenologies);
        spinnerFruitSize.setAdapter(adapterFruitSizes);

        // Get blocks
        CollectionReference blocksReference = db.collection("clients").document(clientId)
                .collection("units").document(unit.getId()).collection("blocks");
        blocksReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Copy all blocks to the Array List
            unitBlocks.addAll(queryDocumentSnapshots.toObjects(Block.class));
            filterAndDeleteDuplicates(unitBlocks);

            // Initialize blocks layout
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    // Save button into array
                    gridButtons[row][col] = new Button(CreateDiagnosticActivity.this);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = 0;
                    params.columnSpec = GridLayout.spec(col, 1f);
                    params.rowSpec = GridLayout.spec(row, 1f);
                    gridButtons[row][col].setLayoutParams(params);
                    gridButtons[row][col].setBackgroundResource(R.drawable.bg_button);
                    boolean matched = false;
                    int finalRow = row;
                    int finalCol = col;

                    for (Block block : unitBlocks) {
                        if (block.getRow() == row && block.getCol() == col) {
                            gridButtons[row][col].setText(block.getName());
                            matched = true;

                            break;
                        }
                    }

                    // Change visibility (if not exists)
                    if (!matched) gridButtons[row][col].setVisibility(View.INVISIBLE);

                    gridButtons[row][col].setOnClickListener(v -> {
                        // Change information and position
                        int counter = 0;
                        for (Block block : unitBlocks) {
                            if (block.getRow() == finalRow && block.getCol() == finalCol) {
                                showBlockInformation(counter);

                                break;
                            }

                            counter++;
                        }
                    });
                    blocksLayout.addView(gridButtons[row][col]);
                }
            }

            // Get items
            if (diagnostic != null) {
                CollectionReference itemsReference = db.collection("clients").document(clientId)
                        .collection("diagnostics").document(diagnostic.getId()).collection("items");
                itemsReference.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    if (queryDocumentSnapshots1 != null) diagnosticItems.addAll(queryDocumentSnapshots1.toObjects(Item.class));

                    // Change color of all filled item
                    for (Item item : diagnosticItems) gridButtons[item.getRow()][item.getCol()].setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));

                    // Show first block information and change visibility
                    showBlockInformation(0);
                    btnCreateDiagnosticItem.setVisibility(View.VISIBLE);
                    if (diagnosticItems.size() == unitBlocks.size()) btnNext.setVisibility(View.VISIBLE);
                });
            } else {
                // Show first block information and change visibility
                showBlockInformation(0);
                btnCreateDiagnosticItem.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> Toast.makeText(CreateDiagnosticActivity.this, getResources().getString(R.string.serverFailed), Toast.LENGTH_SHORT).show());

        // Enable swipe to delete and undo for plagues
        SwipeToDeleteCallback swipeToDeleteCallbackPlagues = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                plagues.remove(position);
                plagueAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchHelperPlague = new ItemTouchHelper(swipeToDeleteCallbackPlagues);
        itemTouchHelperPlague.attachToRecyclerView(rvPlagues);

        // Enable swipe to delete and undo for deficiencies
        SwipeToDeleteCallback swipeToDeleteCallbackDeficiencies = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                deficiencies.remove(position);
                deficiencyAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchHelperDeficiency = new ItemTouchHelper(swipeToDeleteCallbackDeficiencies);
        itemTouchHelperDeficiency.attachToRecyclerView(rvDeficiencies);

        // Enable swipe to delete and undo for contingencies
        SwipeToDeleteCallback swipeToDeleteCallbackContingencies = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                contingencies.remove(position);
                contingencyAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchHelperContingency = new ItemTouchHelper(swipeToDeleteCallbackContingencies);
        itemTouchHelperContingency.attachToRecyclerView(rvContingencies);

        // Enable swipe to delete and undo for activities
        SwipeToDeleteCallback swipeToDeleteCallbackActivities = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                activities.remove(position);
                activityAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchHelperActivity = new ItemTouchHelper(swipeToDeleteCallbackActivities);
        itemTouchHelperActivity.attachToRecyclerView(rvActivities);

        // Enable swipe to delete and undo for tasks
        SwipeToDeleteCallback swipeToDeleteCallbackTasks = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                tasks.remove(position);
                taskAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchHelperTask = new ItemTouchHelper(swipeToDeleteCallbackTasks);
        itemTouchHelperTask.attachToRecyclerView(rvTasks);

        // Add check box listeners
        checkBoxPlague.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Hide views
                rvPlagues.setVisibility(View.GONE);
                btnAddPlague.setVisibility(View.INVISIBLE);
            } else {
                // Show views
                rvPlagues.setVisibility(View.VISIBLE);
                btnAddPlague.setVisibility(View.VISIBLE);
            }
        });
        checkBoxDeficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Hide views
                rvDeficiencies.setVisibility(View.GONE);
                btnAddDeficiency.setVisibility(View.INVISIBLE);
            } else {
                // Show views
                rvDeficiencies.setVisibility(View.VISIBLE);
                btnAddDeficiency.setVisibility(View.VISIBLE);
            }
        });
        checkBoxContingency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Hide views
                rvContingencies.setVisibility(View.GONE);
                btnAddContingency.setVisibility(View.INVISIBLE);
            } else {
                // Show views
                rvContingencies.setVisibility(View.VISIBLE);
                btnAddContingency.setVisibility(View.VISIBLE);
            }
        });
        checkBoxActivity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Hide views
                rvActivities.setVisibility(View.GONE);
                btnAddActivity.setVisibility(View.INVISIBLE);
            } else {
                // Show views
                rvActivities.setVisibility(View.VISIBLE);
                btnAddActivity.setVisibility(View.VISIBLE);
            }
        });
        checkBoxTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Hide views
                rvTasks.setVisibility(View.GONE);
                btnAddTask.setVisibility(View.INVISIBLE);
            } else {
                // Show views
                rvTasks.setVisibility(View.VISIBLE);
                btnAddTask.setVisibility(View.VISIBLE);
            }
        });

        // Add spinner listeners
        spinnerPhenology.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get last item selected
                String selectedItem = parent.getItemAtPosition(position).toString();

                // Verify if the text contains the next words (Harvest)
                if (selectedItem.contains(getResources().getString(R.string.harvest_text)))
                    linearLayoutHarvest.setVisibility(View.VISIBLE);
                else linearLayoutHarvest.setVisibility(View.GONE);

                // Verify if the text contains the next words (Done)
                if (selectedItem.contains(getResources().getString(R.string.done)))
                    linearLayoutDone.setVisibility(View.GONE);
                else linearLayoutDone.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing has been selected
            }
        });

        // Add seekbar listeners
        seekBarShrinkagePercentage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update text view based on seekbar percentage
                tvShrinkagePercentage.setText(getString(R.string.shrinkage_percentage_format, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Add button listeners
        btnAddPlague.setOnClickListener(v -> showAlertDialogLevelAdd(0));
        btnAddDeficiency.setOnClickListener(v -> showAlertDialogLevelAdd(1));
        btnAddContingency.setOnClickListener(v -> showAlertDialogLevelAdd(2));
        btnAddActivity.setOnClickListener(v -> showAlertDialogPercentageAdd(3));
        btnAddTask.setOnClickListener(v -> showAlertDialogPercentageAdd(4));
        btnCreateDiagnosticItem.setOnClickListener(v -> createDiagnosticItem());
        btnNext.setOnClickListener(v -> createDiagnostic());
    }

    private void createDiagnosticItem() {
        // Get all parameters
        String phenology = spinnerPhenology.getSelectedItem().toString();
        String heightText = etvHeight.getText().toString().trim();
        String longitudeText = etvLongitude.getText().toString().trim();
        String weeklyGrowingText = etvWeeklyGrowing.getText().toString().trim();
        String note = etvNote.getText().toString().trim();
        int row = unitBlocks.get(position).getRow();
        int col = unitBlocks.get(position).getCol();

        if (phenology.equals(getResources().getString(R.string.done)) || (!phenology.equals(getResources().getString(R.string.select_phenology)) && !heightText.isEmpty()
                && !longitudeText.isEmpty() && !weeklyGrowingText.isEmpty() &&
                (!plagues.isEmpty() || checkBoxPlague.isChecked()) &&
                (!deficiencies.isEmpty() || checkBoxDeficiency.isChecked()) &&
                (!contingencies.isEmpty() || checkBoxContingency.isChecked()) &&
                (!activities.isEmpty() || checkBoxActivity.isChecked()) &&
                (!tasks.isEmpty() || checkBoxTask.isChecked()))) {

            // Convert text to correct format
            double height = heightText.isEmpty() ? 0.0 : Double.parseDouble(heightText);
            double longitude = longitudeText.isEmpty() ? 0.0 : Double.parseDouble(longitudeText);
            double weeklyGrowing = weeklyGrowingText.isEmpty() ? 0.0 : Double.parseDouble(weeklyGrowingText);

            if (longitude <= height) {
                if (weeklyGrowing <= 30) {
                    // Verify harvest
                    String fruitSize = "NA";
                    int fruitPerPlantMeter = 0;
                    double gramPerFruit = 0.0;
                    int shrinkagePercentage = seekBarShrinkagePercentage.getProgress();
                    if (linearLayoutHarvest.getVisibility() == View.VISIBLE) {
                        // Get information
                        fruitSize = spinnerFruitSize.getSelectedItem().toString();
                        String fruitPerPlantMeterText = etvFruitPerPlantMeter.getText().toString();
                        String gramPerFruitText = etvGramPerFruit.getText().toString();

                        // Verify emptiness
                        if (!fruitPerPlantMeterText.isEmpty() && !gramPerFruitText.isEmpty()) {
                            // Convert text to correct format
                            fruitPerPlantMeter = Integer.parseInt(fruitPerPlantMeterText);
                            gramPerFruit = Double.parseDouble(gramPerFruitText);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Create batch and get document reference
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    WriteBatch batch = db.batch();
                    DocumentReference documentReference;
                    if (!isItemFilled(row, col)) documentReference = diagnosticReference.collection("items").document();
                    else documentReference = diagnosticReference.collection("items").document(Objects.requireNonNull(getItem(row, col)).getId());

                    // Create diagnostic item
                    String id = documentReference.getId();
                    Item item = new Item();
                    item.setId(id);
                    item.setPhenology(phenology);
                    if (checkBoxPlague.isChecked()) item.setPlagues(new ArrayList<>());
                    else item.setPlagues(new ArrayList<>(plagues));
                    if (checkBoxDeficiency.isChecked()) item.setDeficiencies(new ArrayList<>());
                    else item.setDeficiencies(new ArrayList<>(deficiencies));
                    if (checkBoxContingency.isChecked()) item.setContingencies(new ArrayList<>());
                    else item.setContingencies(new ArrayList<>(contingencies));
                    if (checkBoxActivity.isChecked()) item.setActivities(new ArrayList<>());
                    else item.setActivities(new ArrayList<>(activities));
                    if (checkBoxTask.isChecked()) item.setTasks(new ArrayList<>());
                    else item.setTasks(new ArrayList<>(tasks));
                    item.setHeight(height);
                    item.setLongitude(longitude);
                    item.setWeeklyGrowing(weeklyGrowing);
                    item.setNote(note);
                    item.setRow(row);
                    item.setCol(col);
                    item.setFruitSize(fruitSize);
                    item.setFruitPerPlantMeter(fruitPerPlantMeter);
                    item.setGramPerFruit(gramPerFruit);
                    item.setShrinkagePercentage(shrinkagePercentage);

                    // Save it in Firebase Firestore
                    batch.set(documentReference, item);
                    if (diagnostic == null)  {
                        // Create diagnostic
                        Diagnostic newDiagnostic = new Diagnostic();
                        newDiagnostic.setId(diagnosticReference.getId());
                        newDiagnostic.setUnit(unit.getId());
                        newDiagnostic.setUnitName(unit.getName());
                        newDiagnostic.setConsultant("");
                        newDiagnostic.setObservations("");
                        newDiagnostic.setCreation(new Date());
                        newDiagnostic.setFinished(false);
                        newDiagnostic.setDevelopment(1);
                        newDiagnostic.setSanity(1);
                        newDiagnostic.setManagement(1);

                        // Add to batch
                        batch.set(diagnosticReference, newDiagnostic);
                    }
                    batch.commit();
                    Toast.makeText(this, getResources().getString(R.string.item_created), Toast.LENGTH_SHORT).show();

                    // Remove item from list (if exists) and add item to the list
                    removeItemById(id);
                    diagnosticItems.add(item);

                    // Return to the beginning of the scroll view and clear focus
                    View view = getCurrentFocus();
                    scrollView.scrollTo(0, 0);
                    if (view != null) view.clearFocus();

                    // Verify visibility
                    if (diagnosticItems.size() == unitBlocks.size() && btnNext.getVisibility() == View.GONE)
                        btnNext.setVisibility(View.VISIBLE);
                } else Toast.makeText(this, getResources().getString(R.string.weekly_growing_too_high), Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, getResources().getString(R.string.longitude_too_high), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    private void createDiagnostic() {
        // Go to nutrients activity
        Intent intent = new Intent(this, NutrientsActivity.class);
        intent.putExtra("diagnostic", diagnostic);
        intent.putExtra("unit", unit);
        intent.putParcelableArrayListExtra("blocks", new ArrayList<Parcelable>(unitBlocks));
        intent.putParcelableArrayListExtra("items", new ArrayList<Parcelable>(diagnosticItems));
        intent.putExtra("clientId", getIntent().getStringExtra("clientId"));
        intent.putExtra("clientName", getIntent().getStringExtra("clientName"));
        startActivity(intent);
    }

    private void showAlertDialogLevelAdd(int type) {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_level_add, null);

        // Link XML to Java
        Spinner spinner = dialogView.findViewById(R.id.spinner);
        TextView tvIntensity = dialogView.findViewById(R.id.tvIntensity);
        CustomSeekBarLevelAdd seekBar = dialogView.findViewById(R.id.seekBar);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Create adapter and initialize spinner
        switch (type) {
            case 0:
                // Get plague names
                AgriBerries agriBerries = (AgriBerries) getApplicationContext();
                List<Plague> plagueList = agriBerries.getGlobalPlagueList();
                List<String> plagueNameList = new ArrayList<>();
                for (Plague plague : plagueList) if (plague.getDeleted() == null && plague.getCrops().contains(unit.getCrop()))
                    plagueNameList.add(Formatting.capitalizeFirstLetter(plague.getName()));

                // Create spinner plague adapter
                ArrayAdapter<String> plaguesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, plagueNameList);
                plaguesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(plaguesAdapter);
                break;
            case 1:
                // Create deficiencies adapter
                ArrayAdapter<CharSequence> deficienciesAdapter = ArrayAdapter.createFromResource(this, R.array.deficiencies, R.layout.spinner_item);
                deficienciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(deficienciesAdapter);
                break;
            case 2:
                // Create contingencies adapter
                ArrayAdapter<CharSequence> contingenciesAdapter = ArrayAdapter.createFromResource(this, R.array.contingencies, R.layout.spinner_item);
                contingenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(contingenciesAdapter);
                break;
        }

        // Initialize variables
        final int[] intensity = {1};

        // Initialize views
        tvIntensity.setText(getResources().getString(R.string.level1));

        // Create Alert Dialog to ask confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Add button listeners
        btnPositive.setOnClickListener(v -> {
            // Verify if the element is not on the list
            String element = spinner.getSelectedItem().toString();
            boolean found = false;
            switch (type) {
                case 0:
                    // Plagues
                    for (String plague : plagues) if (plague.contains(element)) {
                        found = true;

                        break;
                    }
                    break;
                case 1:
                    // Deficiencies
                    for (String deficiency : deficiencies) if (deficiency.contains(element)) {
                        found = true;

                        break;
                    }
                    break;
                case 2:
                    // Contingencies
                    for (String contingency : contingencies) if (contingency.contains(element)) {
                        found = true;

                        break;
                    }
                    break;
            }

            if (!found) {
                // Add element to the list and update adapter
                element += " (" + intensity[0] + ")";
                switch (type) {
                    case 0:
                        // Add element to plagues and update adapter
                        plagues.add(element);
                        plagueAdapter.notifyItemInserted(plagues.size() - 1);
                        break;
                    case 1:
                        // Add element to deficiencies and update adapter
                        deficiencies.add(element);
                        deficiencyAdapter.notifyItemInserted(deficiencies.size() - 1);
                        break;
                    case 2:
                        // Add element to contingencies and update adapter
                        contingencies.add(element);
                        contingencyAdapter.notifyItemInserted(contingencies.size() - 1);
                        break;
                }

                dialog.dismiss();
            } else Toast.makeText(this, getResources().getString(R.string.element_already_added), Toast.LENGTH_SHORT).show();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Add listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Get equivalent
                String intensityText = getResources().getString(R.string.level);
                float intensityValue = 1 + (progress * 4.0f / 100.0f);
                intensity[0] = Math.round(intensityValue);
                intensityText += " " + intensity[0];
                tvIntensity.setText(intensityText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Show Alert Dialog
        dialog.show(); builder.create();
        dialog.show();
    }

    private void showAlertDialogPercentageAdd(int type) {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_percentage_add, null);

        // Link XML to Java
        Spinner spinner = dialogView.findViewById(R.id.spinner);
        TextView tvPercentage = dialogView.findViewById(R.id.tvPercentage);
        CustomSeekBarPercentageAdd seekBar = dialogView.findViewById(R.id.seekBar);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Create adapter and initialize spinner
        switch (type) {
            case 3:
                // Create activities adapter
                ArrayAdapter<CharSequence> activitiesAdapter = ArrayAdapter.createFromResource(this, R.array.activities, R.layout.spinner_item);
                activitiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(activitiesAdapter);
                break;
            case 4:
                // Create tasks adapter
                ArrayAdapter<CharSequence> tasksAdapter = ArrayAdapter.createFromResource(this, R.array.tasks, R.layout.spinner_item);
                tasksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(tasksAdapter);
                break;
        }

        // Initialize variables
        final int[] percentage = {0};

        // Initialize views
        tvPercentage.setText(getResources().getString(R.string.percentage0));

        // Create Alert Dialog to ask confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Add button listeners
        btnPositive.setOnClickListener(v -> {
            // Verify if the element is not on the list
            String element = spinner.getSelectedItem().toString();
            boolean found = false;
            switch (type) {
                case 3:
                    // Activities
                    for (String activity : activities) if (activity.contains(element)) {
                        found = true;

                        break;
                    }
                    break;
                case 4:
                    // Tasks
                    for (String task : tasks) if (task.contains(element)) {
                        found = true;

                        break;
                    }
                    break;
            }

            if (!found) {
                // Add element to the list and update adapter
                element += " (" + percentage[0] + "%)";
                switch (type) {
                    case 3:
                        // Add element to activities and update adapter
                        activities.add(element);
                        activityAdapter.notifyItemInserted(activities.size() - 1);
                        break;
                    case 4:
                        // Add element to tasks and update adapter
                        tasks.add(element);
                        taskAdapter.notifyItemInserted(tasks.size() - 1);
                        break;
                }

                dialog.dismiss();
            } else Toast.makeText(this, getResources().getString(R.string.element_already_added), Toast.LENGTH_SHORT).show();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Add listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Get equivalent
                String intensityText = getResources().getString(R.string.percentage);
                percentage[0] = progress;
                intensityText += " " + percentage[0] + "%";
                tvPercentage.setText(intensityText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Show Alert Dialog
        dialog.show(); builder.create();
        dialog.show();
    }

    private void showBlockInformation(int newPosition) {
        // Get previous block and current block
        Block previousBlock = unitBlocks.get(position);
        Block currentBlock = unitBlocks.get(newPosition);

        // Remove all items from adapter
        plagueAdapter.notifyItemRangeRemoved(0, plagues.size());
        deficiencyAdapter.notifyItemRangeRemoved(0, deficiencies.size());
        contingencyAdapter.notifyItemRangeRemoved(0, contingencies.size());
        activityAdapter.notifyItemRangeRemoved(0, activities.size());
        taskAdapter.notifyItemRangeRemoved(0, tasks.size());

        // Clear all lists
        plagues.clear();
        deficiencies.clear();
        contingencies.clear();
        activities.clear();
        tasks.clear();

        // Get previous and current row and col
        int previousRow = previousBlock.getRow();
        int previousCol = previousBlock.getCol();
        int currentRow = currentBlock.getRow();
        int currentCol = currentBlock.getCol();

        // Clear all views
        spinnerPhenology.setSelection(0);
        spinnerFruitSize.setSelection(0);
        checkBoxPlague.setChecked(false);
        checkBoxDeficiency.setChecked(false);
        checkBoxContingency.setChecked(false);
        checkBoxActivity.setChecked(false);
        checkBoxTask.setChecked(false);
        etvHeight.setText("");
        etvLongitude.setText("");
        etvWeeklyGrowing.setText("");
        etvNote.setText("");
        etvFruitPerPlantMeter.setText("");
        etvGramPerFruit.setText("");
        seekBarShrinkagePercentage.setProgress(5);

        // Restore information if the item is filled
        if (isItemFilled(currentRow, currentCol)) {
            // Get item information
            Item item = getItem(currentRow, currentCol);

            if (item != null) {
                // Fill lists
                plagues.addAll(item.getPlagues());
                deficiencies.addAll(item.getDeficiencies());
                contingencies.addAll(item.getContingencies());
                activities.addAll(item.getActivities());
                tasks.addAll(item.getTasks());

                // Notify adapters
                plagueAdapter.notifyItemRangeInserted(0, plagues.size());
                deficiencyAdapter.notifyItemRangeInserted(0, deficiencies.size());
                contingencyAdapter.notifyItemRangeInserted(0, contingencies.size());
                activityAdapter.notifyItemRangeInserted(0, activities.size());
                taskAdapter.notifyItemRangeInserted(0, tasks.size());

                // Initialize views
                spinnerPhenology.setSelection(adapterPhenologies.getPosition(item.getPhenology()));
                if (item.getFruitSize() != null && !item.getFruitSize().isEmpty())
                    spinnerFruitSize.setSelection(adapterFruitSizes.getPosition(item.getFruitSize()));
                checkBoxPlague.setChecked(plagues.isEmpty());
                checkBoxDeficiency.setChecked(deficiencies.isEmpty());
                checkBoxContingency.setChecked(contingencies.isEmpty());
                checkBoxActivity.setChecked(activities.isEmpty());
                checkBoxTask.setChecked(tasks.isEmpty());
                etvHeight.setText(String.valueOf(item.getHeight()));
                etvLongitude.setText(String.valueOf(item.getLongitude()));
                etvWeeklyGrowing.setText(String.valueOf(item.getWeeklyGrowing()));
                etvNote.setText(item.getNote());
                etvFruitPerPlantMeter.setText(String.valueOf(item.getFruitPerPlantMeter()));
                etvGramPerFruit.setText(String.valueOf(item.getGramPerFruit()));
                seekBarShrinkagePercentage.setProgress(item.getShrinkagePercentage());
            }
        }

        // Change color of previous button
        if (isItemFilled(previousRow, previousCol)) gridButtons[previousRow][previousCol].setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        else gridButtons[previousRow][previousCol].setBackgroundResource(R.drawable.bg_button);

        // Initialize views
        tvType.setText(currentBlock.getType());
        gridButtons[currentRow][currentCol].setBackgroundColor(ContextCompat.getColor(this, R.color.primary));

        // Change position
        position = newPosition;
    }

    private void removeItemById(String id) {
        Iterator<Item> iterator = diagnosticItems.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (item.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    private Item getItem(int row, int col) {
        for (Item item : diagnosticItems) if (item.getRow() == row && item.getCol() == col) return item;

        return null;
    }

    private boolean isItemFilled(int row, int col) {
        for (Item item : diagnosticItems) if (item.getRow() == row && item.getCol() == col) return true;

        return false;
    }

    private void filterAndDeleteDuplicates(List<Block> blocks) {
        Set<String> uniqueBlocks = new HashSet<>();
        Iterator<Block> iterator = blocks.iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();
            String key = block.getRow() + "," + block.getCol();

            if (!uniqueBlocks.add(key)) {
                iterator.remove();
                deleteBlockFromFirestore(block);
            }
        }
    }

    private void deleteBlockFromFirestore(Block block) {
        // Get instance of Firebase Firestore
        String clientId = getIntent().getStringExtra("clientId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (clientId != null) {
            DocumentReference documentReference = db.collection("clients")
                    .document(clientId).collection("units").document(unit.getId())
                    .collection("blocks").document(block.getId());
            documentReference.delete();
        }
    }

    private void showItemsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateDiagnosticActivity.this);
        builder.setTitle(getResources().getString(R.string.select_block));

        // Verify if there are at least one item
        if (!diagnosticItems.isEmpty()) {
            // Get block names based on items
            List<String> itemNames = new ArrayList<>();
            for (Item item : diagnosticItems) {
                for (Block block : unitBlocks) {
                    if (item.getCol() == block.getCol() && item.getRow() == block.getRow()) {
                        // Add name
                        itemNames.add(block.getName());
                        break;
                    }
                }
            }

            // Create string adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateDiagnosticActivity.this, android.R.layout.simple_list_item_1, itemNames);

            // Create builder
            builder.setAdapter(adapter, (dialog, which) -> {
                Item selectedItem = diagnosticItems.get(which);
                showBlocksDialog(selectedItem);
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.not_enough_items), Toast.LENGTH_SHORT).show();
        }
    }

    private void showBlocksDialog(Item selectedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateDiagnosticActivity.this);
        builder.setTitle(getResources().getString(R.string.copy_to));

        // Get block names
        List<String> blockNames = new ArrayList<>();
        List<Block> visibleBlocks = new ArrayList<>();
        for (Block block : unitBlocks) {
            if (selectedItem.getRow() != block.getRow() || selectedItem.getCol() != block.getCol()) {
                visibleBlocks.add(block);
                blockNames.add(block.getName());
            }
        }

        // Create a boolean array to track selected items
        boolean[] checkedItems = new boolean[blockNames.size()];

        builder.setMultiChoiceItems(blockNames.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
            // Update the selected state of the item
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton(getResources().getString(R.string.confirm), (dialog, id) -> {
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    Block selectedBlock = visibleBlocks.get(i);
                    int row = selectedBlock.getRow();
                    int col = selectedBlock.getCol();

                    // Get document reference
                    DocumentReference documentReference;
                    if (!isItemFilled(row, col)) {
                        documentReference = diagnosticReference.collection("items").document();
                    } else {
                        documentReference = diagnosticReference.collection("items").document(Objects.requireNonNull(getItem(row, col)).getId());
                    }

                    // Create diagnostic item
                    String id2 = documentReference.getId();
                    Item item = new Item();
                    item.setId(id2);
                    item.setPhenology(selectedItem.getPhenology());
                    item.setPlagues(selectedItem.getPlagues());
                    item.setDeficiencies(selectedItem.getDeficiencies());
                    item.setContingencies(selectedItem.getContingencies());
                    item.setActivities(selectedItem.getActivities());
                    item.setTasks(selectedItem.getTasks());
                    item.setHeight(selectedItem.getHeight());
                    item.setLongitude(selectedItem.getLongitude());
                    item.setWeeklyGrowing(selectedItem.getWeeklyGrowing());
                    item.setNote(selectedItem.getNote());
                    item.setRow(row);
                    item.setCol(col);
                    item.setFruitSize(selectedItem.getFruitSize());
                    item.setFruitPerPlantMeter(selectedItem.getFruitPerPlantMeter());
                    item.setGramPerFruit(selectedItem.getGramPerFruit());
                    item.setShrinkagePercentage(selectedItem.getShrinkagePercentage());

                    // Save diagnostic item
                    documentReference.set(item);
                    Toast.makeText(this, getResources().getString(R.string.item_created), Toast.LENGTH_SHORT).show();

                    // Remove item from list (if exists) and add item to the list
                    removeItemById(id2);
                    diagnosticItems.add(item);

                    // Change color
                    gridButtons[row][col].setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                }
            }

            // Return to the beginning of the scroll view and clear focus
            View view = getCurrentFocus();
            scrollView.scrollTo(0, 0);
            if (view != null) view.clearFocus();

            // Verify visibility
            if (diagnosticItems.size() == unitBlocks.size() && btnNext.getVisibility() == View.GONE) {
                btnNext.setVisibility(View.VISIBLE);
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.copy) {
            // Show items dialog
            showItemsDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_diagnostic, menu);

        return true;
    }

}
