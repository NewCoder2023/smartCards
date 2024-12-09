package com.example.smartcards;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeckActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SmartCardsPrefs";
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(folderName);
        }

        // Setup RecyclerView
        setupDeckRecyclerView();

        // Add button logic
        Button addDeckButton = findViewById(R.id.add_deck_button);
        addDeckButton.setOnClickListener(v -> showAddDeckDialog());

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationView);
    }

    private void setupDeckRecyclerView() {
        deckRecyclerView = findViewById(R.id.deck_recycler_view);
        deckList = new ArrayList<>();

        // Load saved decks for this folder
        loadDecks();

        deckAdapter = new DeckAdapter(deckList, this::deleteDeck);
        deckRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deckRecyclerView.setAdapter(deckAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddDeckDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter deck name");
        input.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("New Deck")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String deckName = input.getText().toString().trim();
                    if (deckName.isEmpty()) {
                        showEmptyNameAlert();
                    } else if (deckList.contains(deckName)) {
                        showDuplicateNameAlert();
                    } else {
                        addDeck(deckName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addDeck(String deckName) {
        // Add to list and adapter
        deckList.add(deckName);
        deckAdapter.notifyItemInserted(deckList.size() - 1);

        // Save to SharedPreferences
        saveDeck(deckName);
    }

    private void deleteDeck(int position) {
        String deletedDeck = deckList.get(position);
        deckList.remove(position);
        deckAdapter.notifyItemRemoved(position);
        removeDeck(deletedDeck);
    }

    private void showEmptyNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Invalid Name")
                .setMessage("Deck name cannot be empty. Please enter a valid name.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDuplicateNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Duplicate Name")
                .setMessage("A deck with this name already exists. Please choose another name.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void saveDeck(String deckName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "Decks_" + folderName;
        Set<String> deckSet = sharedPreferences.getStringSet(key, new HashSet<>());
        Set<String> updatedDeckSet = new HashSet<>(deckSet);
        updatedDeckSet.add(deckName);

        sharedPreferences.edit().putStringSet(key, updatedDeckSet).apply();
    }

    private void removeDeck(String deckName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "Decks_" + folderName;
        Set<String> deckSet = sharedPreferences.getStringSet(key, new HashSet<>());
        Set<String> updatedDeckSet = new HashSet<>(deckSet);
        updatedDeckSet.remove(deckName);

        sharedPreferences.edit().putStringSet(key, updatedDeckSet).apply();
    }

    private void loadDecks() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "Decks_" + folderName;
        Set<String> deckSet = sharedPreferences.getStringSet(key, new HashSet<>());

        deckList.clear();
        deckList.addAll(deckSet);
    }
}