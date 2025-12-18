package com.agriberriesmx.agriberries.Admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.agriberriesmx.agriberries.POJO.Client;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateClientAdmin extends AppCompatActivity {
    private Client client;
    private Calendar registration;
    private CountryCodePicker ccp;
    private EditText etvName, etvManager, etvBusiness, etvEmail;
    private Spinner spinnerFrequency;
    private SwitchCompat swBlocked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_update_client);
        setup();
    }

    private void setup() {
        // Link XML to Java
        etvName = findViewById(R.id.etvName);
        etvManager = findViewById(R.id.etvManager);
        etvBusiness = findViewById(R.id.etvBusiness);
        etvEmail = findViewById(R.id.etvEmail);
        swBlocked = findViewById(R.id.swBlocked);
        ccp = findViewById(R.id.ccp);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        EditText etvPhone = findViewById(R.id.etvPhone);
        TextView tvRegistration = findViewById(R.id.tvRegistration);
        Button btnUpdateClient = findViewById(R.id.btnUpdateClient);

        // Get client info
        client = getIntent().getParcelableExtra("client");

        // Get formatted registration date
        registration = Calendar.getInstance();
        registration.setTime(client.getRegistration());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedBirthDate = sdf.format(registration.getTime());

        // Get string array for spinner
        ArrayAdapter<CharSequence> adapterFrequencies = ArrayAdapter.createFromResource(this, R.array.frequencies, android.R.layout.simple_spinner_item);
        adapterFrequencies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapterFrequencies);

        // Link CCP to Edit Text
        ccp.registerCarrierNumberEditText(etvPhone);

        // Initialize views
        spinnerFrequency.setAdapter(adapterFrequencies);
        etvName.setText(client.getName());
        etvManager.setText(client.getManager());
        etvBusiness.setText(client.getBusiness());
        etvEmail.setText(client.getEmail());
        ccp.setFullNumber(client.getPhone());
        swBlocked.setChecked(client.isBlocked());
        tvRegistration.setText(formattedBirthDate);
        spinnerFrequency.setSelection(client.getFrequency());

        // Add listener
        tvRegistration.setOnClickListener(v -> {
            int day = registration.get(Calendar.DAY_OF_MONTH);
            int month = registration.get(Calendar.MONTH);
            int year = registration.get(Calendar.YEAR);

            // Create DatePicker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(UpdateClientAdmin.this, (view, updatedYear, updatedMonth, updatedDay) -> {
                // Update birth date
                registration.set(Calendar.YEAR, updatedYear);
                registration.set(Calendar.MONTH, updatedMonth);
                registration.set(Calendar.DAY_OF_MONTH, updatedDay);

                // Get formatted birth date and show it
                tvRegistration.setText(sdf.format(registration.getTime()));
            }, year, month, day);

            // Show DatePicker dialog
            datePickerDialog.show();

        });
        btnUpdateClient.setOnClickListener(v -> updateClient());
    }

    private void updateClient() {
        // Get attributes to verify that they are not empty
        String name = Formatting.capitalizeName(etvName.getText().toString().trim());
        String manager = etvManager.getText().toString().trim();
        String business = etvBusiness.getText().toString().trim();
        String phone = ccp.getFullNumberWithPlus();
        String email = etvEmail.getText().toString().trim();
        boolean blocked = swBlocked.isChecked();
        int frequency = spinnerFrequency.getSelectedItemPosition();

        if (ccp.isValidFullNumber()) {
            if (!name.isEmpty() && !manager.isEmpty() && !business.isEmpty()) {
                // Connect to Firebase Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection("clients").document(client.getId());

                // Update client info
                documentReference.update("name", name, "manager", manager,
                        "business", business, "phone", phone, "email", email, "blocked", blocked,
                        "frequency", frequency, "prospect", false);
                Toast.makeText(UpdateClientAdmin.this, getResources().getString(R.string.clientUpdated), Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();

    }

}
