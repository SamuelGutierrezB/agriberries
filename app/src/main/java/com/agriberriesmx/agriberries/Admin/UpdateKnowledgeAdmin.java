package com.agriberriesmx.agriberries.Admin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import com.agriberriesmx.agriberries.Adapter.SpinnerCropAdapter;
import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Knowledge;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UpdateKnowledgeAdmin extends AppCompatActivity {
    private Knowledge knowledge;
    private MenuItem deleteRestoreItem;
    private TextAdapter cropAdapter;
    private List<String> crops;
    private Spinner spinnerCrop;
    private EditText etvTitle, etvLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_update_knowledge);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        spinnerCrop = findViewById(R.id.spinnerCrop);
        etvTitle = findViewById(R.id.etvTitle);
        etvLink = findViewById(R.id.etvLink);
        RecyclerView rvCrops = findViewById(R.id.rvCrops);
        ImageButton btnPaste = findViewById(R.id.btnPaste);
        ImageButton btnAddCrop = findViewById(R.id.btnAddCrop);
        Button btnUpdateKnowledge = findViewById(R.id.btnUpdateKnowledge);

        // Get knowledge info
        knowledge = getIntent().getParcelableExtra("knowledge");
        crops = knowledge.getCrops();

        // Get crops names
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        List<Crop> cropList = agriBerries.getGlobalCropList();
        List<String> cropNameList = new ArrayList<>();
        for (Crop crop : cropList) if (crop.getDeleted() == null) cropNameList.add(crop.getName());

        // Create spinner adapter
        SpinnerCropAdapter spinnerCropAdapter = new SpinnerCropAdapter(UpdateKnowledgeAdmin.this, cropNameList);
        spinnerCrop.setAdapter(spinnerCropAdapter);

        // Initialize views
        etvTitle.setText(knowledge.getTitle());
        etvLink.setText(knowledge.getLink());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cropAdapter = new TextAdapter(crops);
        rvCrops.setLayoutManager(linearLayoutManager);
        rvCrops.setAdapter(cropAdapter);

        // Initialize toolbar
        setSupportActionBar(toolbar);
        setTitle("");
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

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
        btnUpdateKnowledge.setOnClickListener(v -> updateKnowledge());
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

    private void updateKnowledge() {
        // Get attributes to verify that they are not empty
        String title = etvTitle.getText().toString().trim();
        String link = etvLink.getText().toString().trim();

        if (!title.isEmpty() && !link.isEmpty() && crops.size() > 0) {
            // Change link to a valid one
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                // Transform link to a valid one
                String previousLink = link;
                link = "http://" + previousLink;
            }

            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("knowledge").document(knowledge.getId());

            // Update knowledge info
            documentReference.update("title", title, "link", link, "crops", crops);
            Toast.makeText(UpdateKnowledgeAdmin.this, getResources().getString(R.string.knowledgeUpdated), Toast.LENGTH_SHORT).show();
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

    private void showDeleteKnowledgeDialog() {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

        // Link XML to Java
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Initialize views
        if (knowledge.getDeleted() == null) {
            // To delete
            tvMessage.setText(getResources().getString(R.string.askDeleteKnowledge));
            btnPositive.setText(getResources().getString(R.string.delete));
        } else {
            // To restore
            tvMessage.setText(getResources().getString(R.string.askRestoreKnowledge));
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
            DocumentReference documentReference = db.collection("knowledge").document(knowledge.getId());

            if (knowledge.getDeleted() == null) {
                // Delete knowledge
                documentReference.update("deleted", Formatting.addSevenDaysAndFormat(new Date()));
                knowledge.setDeleted(Formatting.addSevenDaysAndFormat(new Date()));
                deleteRestoreItem.setTitle(getResources().getString(R.string.restore));
                Toast.makeText(UpdateKnowledgeAdmin.this, getResources().getString(R.string.knowledgeDeleted), Toast.LENGTH_SHORT).show();
            } else {
                // Restore knowledge
                documentReference.update("deleted", null);
                knowledge.setDeleted(null);
                deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
                Toast.makeText(UpdateKnowledgeAdmin.this, getResources().getString(R.string.knowledgeRestored), Toast.LENGTH_SHORT).show();
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
            showDeleteKnowledgeDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        // Change text
        deleteRestoreItem = menu.findItem(R.id.deleteRestore);
        if (knowledge.getDeleted() == null)
            deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
        else
            deleteRestoreItem.setTitle(getResources().getString(R.string.restore));

        return true;
    }

}
