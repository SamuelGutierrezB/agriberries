package com.agriberriesmx.agriberries.Admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.google.firebase.functions.FirebaseFunctions;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateConsultantAdmin extends AppCompatActivity {
    private Calendar birth;
    private CountryCodePicker ccp;
    private EditText etvUser, etvPassword, etvName;
    private Spinner spinnerCategory;
    private ProgressBar progressBar;
    private Button btnSignUpConsultant;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_create_consultant);
        setup();
    }

    private void setup() {
        // Link XML to Java
        etvUser = findViewById(R.id.etvUser);
        etvPassword = findViewById(R.id.etvPassword);
        etvName = findViewById(R.id.etvName);
        ccp = findViewById(R.id.ccp);
        EditText etvPhone = findViewById(R.id.etvPhone);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        TextView tvBirth = findViewById(R.id.tvBirth);
        ImageButton btnGeneratePassword = findViewById(R.id.btnGeneratePassword);
        progressBar = findViewById(R.id.progressBar);
        btnSignUpConsultant = findViewById(R.id.btnSignUpConsultant);

        // Get formatted birth date
        birth =  Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedBirthDate = sdf.format(birth.getTime());

        // Get string array for spinner
        ArrayAdapter<CharSequence> adapterCategories = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Link CCP to Edit Text
        ccp.registerCarrierNumberEditText(etvPhone);

        // Initialize views
        spinnerCategory.setAdapter(adapterCategories);
        spinnerCategory.setSelection(1);
        tvBirth.setText(formattedBirthDate);

        // Add listener
        tvBirth.setOnClickListener(v -> {
            int day = birth.get(Calendar.DAY_OF_MONTH);
            int month = birth.get(Calendar.MONTH);
            int year = birth.get(Calendar.YEAR);

            // Create DatePicker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateConsultantAdmin.this, (view, updatedYear, updatedMonth, updatedDay) -> {
                // Update birth date
                birth.set(Calendar.YEAR, updatedYear);
                birth.set(Calendar.MONTH, updatedMonth);
                birth.set(Calendar.DAY_OF_MONTH, updatedDay);

                // Get formatted birth date and show it
                tvBirth.setText(sdf.format(birth.getTime()));
            }, year, month, day);

            // Show DatePicker dialog
            datePickerDialog.show();

        });
        btnGeneratePassword.setOnClickListener(v -> generatePassword());
        btnSignUpConsultant.setOnClickListener(v -> signUpConsultant());
    }

    private void signUpConsultant() {
        // Get attributes to verify that they are not empty
        String username = etvUser.getText().toString().trim().toLowerCase();
        String password = etvPassword.getText().toString().trim();
        String name = Formatting.capitalizeName(etvName.getText().toString().trim());
        String phone = ccp.getFullNumberWithPlus();
        int category = spinnerCategory.getSelectedItemPosition();

        if (!username.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
            if (!username.contains(" ")) {
                if (password.length() >= 6) {
                    if (ccp.isValidFullNumber()) {
                        // Create user and document
                        createUserAndDocument(username, password, name, phone, category, birth.getTime());
                    } else Toast.makeText(this, getResources().getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                } else Toast.makeText(this, getResources().getString(R.string.passwordLength), Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, getResources().getString(R.string.userInvalid), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    private void createUserAndDocument(String username, String password, String name, String phone, int category, Date birth) {
        // Change visibility
        btnSignUpConsultant.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Connect to Firebase Functions
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        // Create Hash Map of consultant
        Map<String, Object> consultant = new HashMap<>();
        consultant.put("username", username);
        consultant.put("password", password);
        consultant.put("name", name);
        consultant.put("phone", phone);
        consultant.put("category", category);
        consultant.put("birth", birth.getTime());

        mFunctions
                .getHttpsCallable("createUserAndDocument")
                .call(consultant)
                .continueWith(task -> {
                    // Get result
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();

                    if (result != null) {
                        // Get success text
                        Boolean success = (Boolean) result.get("success");

                        if (success != null && success) {
                            // Inform user
                            Toast.makeText(CreateConsultantAdmin.this, getResources().getString(R.string.user_created), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // Get error message and inform user
                            String error = (String) result.get("error");

                            if (error != null && error.contains("already")) Toast.makeText(CreateConsultantAdmin.this, getResources().getString(R.string.user_in_use), Toast.LENGTH_SHORT).show();
                            else Toast.makeText(CreateConsultantAdmin.this, getResources().getString(R.string.user_not_created), Toast.LENGTH_SHORT).show();

                            // Change visibility
                            progressBar.setVisibility(View.GONE);
                            btnSignUpConsultant.setVisibility(View.VISIBLE);
                        }
                    }

                    return result;
                });
    }

    private void generatePassword() {
        // Characters that will be use to generate the password and length of the password
        String characters = getResources().getString(R.string.characters);
        int length = 16;

        // Generate random password
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int)(characters.length() * Math.random());
            sb.append(characters.charAt(index));
        }

        // Show password in EditText
        String password = sb.toString();
        etvPassword.setText(password);
    }

}
