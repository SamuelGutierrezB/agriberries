package com.agriberriesmx.agriberries;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.agriberriesmx.agriberries.POJO.Block;
import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Item;
import com.agriberriesmx.agriberries.POJO.Nutrient;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.Utils.CustomSeekBarDiagnostic;
import com.agriberriesmx.agriberries.Utils.CustomSeekBarPercentageAdd;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.agriberriesmx.agriberries.Utils.PdfGenerator;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NutrientsActivity extends AppCompatActivity {
    private Diagnostic diagnostic;
    private Unit unit;
    private Button btnCreateDiagnostic;
    private List<Block> blocks;
    private List<Item> items;
    private List<Nutrient> nutrients = new ArrayList<>();
    private final ArrayList<EditText[]> editTextArray = new ArrayList<>();
    private String clientId, clientName;
    private int position = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrients);
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enable button (if it is disabled)
        if (!btnCreateDiagnostic.isEnabled()) btnCreateDiagnostic.setEnabled(true);
    }

    private void setup() {
        // Link XML to Java
        RadioButton rBtnDropper = findViewById(R.id.rBtnDropper);
        RadioButton rBtnDrainage = findViewById(R.id.rBtnDrainage);
        RadioButton rBtnSubstratum = findViewById(R.id.rBtnSubstratum);
        RadioButton rBtnFoliage = findViewById(R.id.rBtnFoliage);
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        btnCreateDiagnostic = findViewById(R.id.btnCreateDiagnostic);

        // Get diagnostic and blocks
        diagnostic = getIntent().getParcelableExtra("diagnostic");
        unit = getIntent().getParcelableExtra("unit");
        blocks = Formatting.getSortedBlockList(getIntent().getParcelableArrayListExtra("blocks"));
        items = Formatting.getSortedItemList(getIntent().getParcelableArrayListExtra("items"));
        clientId = getIntent().getStringExtra("clientId");
        clientName = getIntent().getStringExtra("clientName");

        // Get nutrients (if exist)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("clients").document(clientId)
                .collection("diagnostics").document(diagnostic.getId()).collection("nutrients");
        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Get all nutrients and check if it is empty
            nutrients.addAll(queryDocumentSnapshots.toObjects(Nutrient.class));
            nutrients = Formatting.getSortedNutrientList(nutrients);
            boolean empty = nutrients.isEmpty();

            // Create Grid Layout
            for (int counter = 0; counter < blocks.size(); counter++) {
                // Get block
                Block block = blocks.get(counter);

                if (empty) {
                    // Create nutrients
                    Nutrient nutrient = new Nutrient();
                    nutrient.setRow(block.getRow());
                    nutrient.setCol(block.getCol());
                    nutrients.add(nutrient);
                }

                // TextView with block's name
                TextView textView = new TextView(NutrientsActivity.this);
                textView.setText(block.getName());
                textView.setTypeface(null, Typeface.BOLD);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundResource(R.drawable.border_rectangular);

                GridLayout.LayoutParams textParams = new GridLayout.LayoutParams();
                textParams.width = 0;
                textParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
                textParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                textParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                textView.setLayoutParams(textParams);
                gridLayout.addView(textView);

                // Create and add EditText
                EditText[] editTextsForRow = new EditText[6];
                for (int i = 0; i < 6; i++) {
                    // Create EditText
                    EditText editText = new EditText(NutrientsActivity.this);
                    if (i < 4) editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    else editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    editText.setGravity(Gravity.CENTER);
                    editText.setBackgroundResource(R.drawable.border_rectangular);
                    int minHeightInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
                    editText.setMinHeight(minHeightInPx);

                    // Set params
                    GridLayout.LayoutParams editParams = new GridLayout.LayoutParams();
                    editParams.width = 0;
                    editParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    editParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    editParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    editText.setLayoutParams(editParams);
                    editText.setSelectAllOnFocus(true);

                    // Add listeners
                    int finalI = i;
                    int finalCounter = counter;
                    editText.addTextChangedListener(new TextWatcher() {
                        boolean isUserInput = true;

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence text, int start, int before, int count) {
                            if (!isUserInput) return;

                            try {
                                if (finalI < 4) {
                                    // Integer
                                    int value = Integer.parseInt(text.toString());
                                    if (value >= 0) {
                                        if (finalI == 0) nutrients.get(finalCounter).getNitrate().set(position, value);
                                        else if (finalI == 1) nutrients.get(finalCounter).getCalcium().set(position, value);
                                        else if (finalI == 2) nutrients.get(finalCounter).getSodium().set(position, value);
                                        else nutrients.get(finalCounter).getPotassium().set(position, value);
                                    } else {
                                        if (finalI == 0) nutrients.get(finalCounter).getNitrate().set(position, 0);
                                        else if (finalI == 1) nutrients.get(finalCounter).getCalcium().set(position, 0);
                                        else if (finalI == 2) nutrients.get(finalCounter).getSodium().set(position, 0);
                                        else nutrients.get(finalCounter).getPotassium().set(position, 0);
                                    }
                                } else {
                                    // Double
                                    double value = Double.parseDouble(text.toString());
                                    if (value >= 0) {
                                        if (finalI == 4) {
                                            double correctedValue = Math.min(Math.max(value, 0.0), 14.0);
                                            nutrients.get(finalCounter).getPh().set(position, correctedValue);
                                            if (value != correctedValue) updateEditText(editText, correctedValue);
                                        }
                                        else nutrients.get(finalCounter).getConductivity().set(position, value);
                                    } else {
                                        if (finalI == 4) nutrients.get(finalCounter).getPh().set(position, 0.0);
                                        else nutrients.get(finalCounter).getConductivity().set(position, 0.0);
                                    }
                                }
                            } catch (Exception e) {
                                if (finalI == 0) nutrients.get(finalCounter).getNitrate().set(position, 0);
                                else if (finalI == 1) nutrients.get(finalCounter).getCalcium().set(position, 0);
                                else if (finalI == 2) nutrients.get(finalCounter).getSodium().set(position, 0);
                                else if (finalI == 3) nutrients.get(finalCounter).getPotassium().set(position, 0);
                                if (finalI == 4) nutrients.get(finalCounter).getPh().set(position, 0.0);
                                else nutrients.get(finalCounter).getConductivity().set(position, 0.0);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }

                        private void updateEditText(EditText editText, double value) {
                            // Change edit text shown text
                            isUserInput = false;
                            editText.setText(String.format(Locale.getDefault(), "%.2f", value));
                            editText.setSelection(editText.getText().length());
                            isUserInput = true;
                        }
                    });

                    gridLayout.addView(editText);
                    editTextsForRow[i] = editText;
                }
                editTextArray.add(editTextsForRow);
            }
            gridLayout.setRowCount(blocks.size());
            updateView(position);
        });

        // Add listeners
        rBtnDropper.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) updateView(0);
        });
        rBtnDrainage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) updateView(1);
        });
        rBtnSubstratum.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) updateView(2);
        });
        rBtnFoliage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) updateView(3);
        });
        btnCreateDiagnostic.setOnClickListener(v -> createDiagnostic());
    }

    private void updateView(int newPosition) {
        // Update position value
        position = newPosition;

        // Change EditText values
        for (int counter = 0; counter < nutrients.size(); counter++) {
            for (int auxCounter = 0; auxCounter < 6; auxCounter++) {
                String textValue = "0";
                switch (auxCounter) {
                    case 0:
                        textValue = String.valueOf(nutrients.get(counter).getNitrate().get(position));
                        break;
                    case 1:
                        textValue = String.valueOf(nutrients.get(counter).getCalcium().get(position));
                        break;
                    case 2:
                        textValue = String.valueOf(nutrients.get(counter).getSodium().get(position));
                        break;
                    case 3:
                        textValue = String.valueOf(nutrients.get(counter).getPotassium().get(position));
                        break;
                    case 4:
                        textValue = String.valueOf(nutrients.get(counter).getPh().get(position));
                        break;
                    case 5:
                        textValue = String.valueOf(nutrients.get(counter).getConductivity().get(position));
                        break;
                }
                editTextArray.get(counter)[auxCounter].setText(textValue);
            }
        }
    }

    private void createDiagnostic() {
        // Create Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.popup_diagnostic, null);

        // Link XML to Java
        TextView tvDevelopment = view.findViewById(R.id.tvDevelopment);
        TextView tvSanity = view.findViewById(R.id.tvSanity);
        TextView tvManagement = view.findViewById(R.id.tvManagement);
        TextView tvTotal = view.findViewById(R.id.tvTotal);
        TextView tvAverage = view.findViewById(R.id.tvAverage);
        TextView tvExecutionApplications = view.findViewById(R.id.tvExecutionApplications);
        TextView tvExecutionFertilizers = view.findViewById(R.id.tvExecutionFertilizers);
        ImageView ivFace = view.findViewById(R.id.ivFace);
        EditText etvObservations = view.findViewById(R.id.etvObservations);
        CustomSeekBarDiagnostic seekBarDevelopment = view.findViewById(R.id.seekBarDevelopment);
        CustomSeekBarDiagnostic seekBarSanity = view.findViewById(R.id.seekBarSanity);
        CustomSeekBarDiagnostic seekBarManagement = view.findViewById(R.id.seekBarManagement);
        CustomSeekBarPercentageAdd seekBarExecutionApplications = view.findViewById(R.id.seekBarExecutionApplications);
        CustomSeekBarPercentageAdd seekBarExecutionFertilizers = view.findViewById(R.id.seekBarExecutionFertilizers);
        Spinner spinnerTendency = view.findViewById(R.id.spinnerTendency);

        // Create spinner adapters tendencies
        ArrayAdapter<CharSequence> adapterTendencies = ArrayAdapter.createFromResource(this, R.array.tendencies, R.layout.spinner_item);
        spinnerTendency.setAdapter(adapterTendencies);

        // Get the diagnostic tendency
        String tendency = diagnostic.getTendency();
        int position = adapterTendencies.getPosition(tendency);

        // If the tendency is not found, default to the first item
        if (position == -1) {
            position = 0;
        }

        // Set the spinner to the correct position
        spinnerTendency.setSelection(position);

        // Create variables for total and average
        final int[] development = {diagnostic.getDevelopment()};
        final int[] sanity = {diagnostic.getSanity()};
        final int[] management = {diagnostic.getManagement()};
        final int[] executionApplications = {diagnostic.getExecutionApplications()};
        final int[] executionFertilizers = {diagnostic.getExecutionFertilizers()};
        if (executionApplications[0] == 0 && executionFertilizers[0] == 0) {
            executionApplications[0] = 100;
            executionFertilizers[0] = 100;
        }
        updateTotalAndAverage(tvTotal, tvAverage, ivFace, development[0], sanity[0], management[0]);

        // Initialize views
        final String[] developmentText = {getResources().getString(R.string.development) + " " + development[0]};
        final String[] sanityText = {getResources().getString(R.string.sanity) + " " + sanity[0]};
        final String[] managementText = {getResources().getString(R.string.managementLevel) + " " + management[0]};
        tvDevelopment.setText(developmentText[0]);
        tvSanity.setText(sanityText[0]);
        tvManagement.setText(managementText[0]);
        etvObservations.setText(diagnostic.getObservations());
        seekBarDevelopment.setProgress(development[0] - 1);
        seekBarSanity.setProgress(sanity[0] - 1);
        seekBarManagement.setProgress(management[0] - 1);

        // Add listeners
        seekBarDevelopment.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                development[0] = progress + 1;
                developmentText[0] = getResources().getString(R.string.development) + " " + development[0];
                tvDevelopment.setText(developmentText[0]);
                updateTotalAndAverage(tvTotal, tvAverage, ivFace, development[0], sanity[0], management[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarSanity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sanity[0] = progress + 1;
                sanityText[0] = getResources().getString(R.string.sanity) + " " + sanity[0];
                tvSanity.setText(sanityText[0]);
                updateTotalAndAverage(tvTotal, tvAverage, ivFace, development[0], sanity[0], management[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarManagement.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                management[0] = progress + 1;
                managementText[0] = getResources().getString(R.string.managementLevel) + " " + management[0];
                tvManagement.setText(managementText[0]);
                updateTotalAndAverage(tvTotal, tvAverage, ivFace, development[0], sanity[0], management[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarExecutionApplications.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                executionApplications[0] = progress;
                tvExecutionApplications.setText(
                        String.format(getString(R.string.execution_applications), progress)
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarExecutionFertilizers.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                executionFertilizers[0] = progress;
                tvExecutionFertilizers.setText(
                        String.format(getString(R.string.execution_fertilizers), progress)
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Set progress
        seekBarExecutionApplications.setProgress(executionApplications[0]);
        seekBarExecutionFertilizers.setProgress(executionFertilizers[0]);

        builder.setView(view).setPositiveButton(getResources().getString(R.string.confirm), (dialog, which) -> {
            // Dismiss dialog and disable button
            btnCreateDiagnostic.setEnabled(false);
            dialog.dismiss();

            // Get observations
            String observations = etvObservations.getText().toString().trim();

            // Update diagnostic
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference diagnosticReference = db.collection("clients").document(clientId)
                    .collection("diagnostics").document(diagnostic.getId());
            diagnostic.setObservations(observations);
            diagnostic.setDevelopment(development[0]);
            diagnostic.setSanity(sanity[0]);
            diagnostic.setManagement(management[0]);
            diagnostic.setExecutionApplications(executionApplications[0]);
            diagnostic.setExecutionFertilizers(executionFertilizers[0]);
            diagnostic.setTendency(spinnerTendency.getSelectedItem().toString());
            diagnosticReference.update("observations", observations);
            diagnosticReference.update("development", development[0]);
            diagnosticReference.update("sanity", sanity[0]);
            diagnosticReference.update("management", management[0]);
            diagnosticReference.update("executionApplications", executionApplications[0]);
            diagnosticReference.update("executionFertilizers", executionFertilizers[0]);
            diagnosticReference.update("tendency", spinnerTendency.getSelectedItem().toString());

            // Save nutrients info
            for (int counter = 0; counter < nutrients.size(); counter++) {
                // Get nutrient info
                Nutrient nutrient = nutrients.get(counter);

                // Get document reference and save ID
                DocumentReference documentReference;
                if (nutrient.getId() != null) documentReference = diagnosticReference.collection("nutrients").document(nutrient.getId());
                else {
                    documentReference = diagnosticReference.collection("nutrients").document();
                    nutrients.get(counter).setId(documentReference.getId());
                }

                // Save information
                documentReference.set(nutrients.get(counter));
            }

            // Get current date and obtain week of the year and year (2 digits)
            Calendar calendar = Calendar.getInstance();
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            SimpleDateFormat yearFormat = new SimpleDateFormat("yy", Locale.getDefault());
            String yearInTwoDigits = yearFormat.format(calendar.getTime());
            String formattedWeekAndYear = String.format(Locale.getDefault(), "%02d.%s", weekOfYear, yearInTwoDigits);

            // Get string path
            String filePath = getCacheDir() + "/Dx." + clientName + "." + unit.getCrop() + "."
                    + unit.getName() + "." + formattedWeekAndYear + ".pdf";

            // Replace path
            filePath = filePath.replace("/", "÷");
            filePath = getCacheDir() + "/" + filePath;

            // Create PDF
            try {
                PdfGenerator.generatePdf(this, diagnostic, unit, blocks, items, nutrients, filePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Show PDF
            Intent intent = new Intent(this, SaveDiagnosticActivity.class);
            intent.putExtra("path", filePath);
            intent.putExtra("client", clientId);
            intent.putExtra("clientName", clientName);
            intent.putExtra("diagnostic", diagnostic);
            intent.putExtra("unit", unit);
            intent.putParcelableArrayListExtra("items", (ArrayList<? extends Parcelable>) items);
            startActivity(intent);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateTotalAndAverage(TextView tvTotal, TextView tvAverage, ImageView ivFace, int development, int sanity, int management) {
        int total = development + sanity + management;
        double average = total / 3.0;
        tvTotal.setText(String.valueOf(total));
        tvAverage.setText(String.format(Locale.getDefault(), "%.1f", average));
        if (average >= 8) ivFace.setBackgroundResource(R.drawable.ic_happy_face);
        else if (average >= 6) ivFace.setBackgroundResource(R.drawable.ic_serious_face);
        else ivFace.setBackgroundResource(R.drawable.ic_sad_face);
    }

}
