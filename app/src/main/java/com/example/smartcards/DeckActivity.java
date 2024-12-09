package com.example.smartcards;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
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

        // Add swipe-to-delete functionality
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private Drawable deleteIcon;
            private ColorDrawable background;

            {
                deleteIcon = ContextCompat.getDrawable(DeckActivity.this, android.R.drawable.ic_delete);
                background = new ColorDrawable(Color.GRAY);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String deletedDeck = deckList.get(position);

                // Remove from adapter
                deckAdapter.removeItem(position);

                // Remove from SharedPreferences
                removeDeck(deletedDeck);

                // Show undo Snackbar
                Snackbar.make(deckRecyclerView, "Deck deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            // Restore the deleted deck
                            deckList.add(position, deletedDeck);
                            deckAdapter.notifyItemInserted(position);
                            saveDeck(deletedDeck);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

                if (dX > 0) {
                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            (int) dX, itemView.getBottom());
                    deleteIcon.setBounds(
                            itemView.getLeft() + iconMargin,
                            itemView.getTop() + iconMargin,
                            itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth(),
                            itemView.getBottom() - iconMargin
                    );
                } else {
                    background.setBounds(itemView.getRight() + (int) dX,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteIcon.setBounds(
                            itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(),
                            itemView.getTop() + iconMargin,
                            itemView.getRight() - iconMargin,
                            itemView.getBottom() - iconMargin
                    );
                }

                background.draw(c);
                deleteIcon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(deckRecyclerView);
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

        // Save
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
