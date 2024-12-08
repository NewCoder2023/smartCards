package com.example.smartcards;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.smartcards.utils.NavigationUtils;

import java.util.ArrayList;
import java.util.List;

public class DeckActivity extends AppCompatActivity {
    private RecyclerView deckRecyclerView;
    private List<String> deckList;
    private DeckAdapter deckAdapter;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        // Get the folder name from the Intent
        folderName = getIntent().getStringExtra("FOLDER_NAME");

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            getSupportActionBar().setTitle(folderName); // Set the title to the folder name
        }

        // Setup RecyclerView
        deckRecyclerView = findViewById(R.id.deck_recycler_view);
        deckList = new ArrayList<>();
        deckAdapter = new DeckAdapter(deckList);
        deckRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deckRecyclerView.setAdapter(deckAdapter);

        // Add button logic
        Button addDeckButton = findViewById(R.id.add_deck_button);
        addDeckButton.setOnClickListener(v -> showAddDeckDialog());

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddDeckDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter deck name");
        input.setPadding(32, 32, 32, 32); // Add padding inside the input field

        new AlertDialog.Builder(this)
                .setTitle("New Deck")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String deckName = input.getText().toString().trim();
                    if (deckName.isEmpty()) {
                        showEmptyNameAlert(); // Show alert for empty name
                    } else if (deckList.contains(deckName)) {
                        showDuplicateNameAlert(); // Show alert for duplicate name
                    } else {
                        deckList.add(deckName);
                        deckAdapter.notifyItemInserted(deckList.size() - 1);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Alert for empty deck name
    private void showEmptyNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Invalid Name")
                .setMessage("Deck name cannot be empty. Please enter a valid name.")
                .setPositiveButton("OK", null)
                .show();
    }

    // Alert for duplicate deck name
    private void showDuplicateNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Duplicate Name")
                .setMessage("A deck with this name already exists. Please choose another name.")
                .setPositiveButton("OK", null)
                .show();
    }

}