package com.example.smartcards;

import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcards.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class DeckActivity extends AppCompatActivity {
    private RecyclerView deckRecyclerView;
    private List<Deck> deckList;
    private DeckAdapter deckAdapter;
    private String folderName;

    private DeckDao deckDao; // Access to the Deck database
    private int deckId = -1; // Initialize with a default invalid value


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        // Bottom Navigation
        BottomNavigationView bottomNavigationViewMain = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationViewMain);


        // Get the folder name from the Intent
        folderName = getIntent().getStringExtra("FOLDER_NAME");

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(folderName);
        }

        // Initialize database
        deckDao = FlashcardDatabase.getDatabase(this).deckDao();

        // Setup RecyclerView
        setupDeckRecyclerView();

        // Add button logic
        Button addDeckButton = findViewById(R.id.add_deck_button);
        addDeckButton.setOnClickListener(v -> showAddDeckDialog());
    }

    private void setupDeckRecyclerView() {
        deckRecyclerView = findViewById(R.id.deck_recycler_view);
        deckList = new ArrayList<>();

        // Load decks from the database
        loadDecks();

        // Initialize DeckAdapter with long-press listener
        deckAdapter = new DeckAdapter(deckList, position -> {
            Deck selectedDeck = deckList.get(position);
            new android.app.AlertDialog.Builder(DeckActivity.this)
                    .setTitle("Choose Action")
                    .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                        if (which == 0) {
                            // Edit folder
                            showEditDeckDialog(selectedDeck , position);
                        } else if (which == 1) {
                            // Delete folder
                            deleteDeck(position);
                        }
                    })
                    .show();
        });

        deckRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deckRecyclerView.setAdapter(deckAdapter);
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
                    } else if (deckExists(deckName)) {
                        showDuplicateNameAlert();
                    } else {
                        addDeckToDatabase(deckName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadDecks() {
        new Thread(() -> {
            List<Deck> decks = deckDao.getDecksByFolderName(folderName);
            runOnUiThread(() -> {
                deckList.clear();
                deckList.addAll(decks);
                deckAdapter.notifyDataSetChanged();
            });
        }).start();
    }



    private void addDeckToDatabase(String deckName) {
        Deck deck = new Deck(deckName, folderName); // Include folderName

        new Thread(() -> {
            long newDeckId = deckDao.insert(deck); // Insert deck and get its ID
            deck.id = (int) newDeckId; // Set the ID in the deck object

            runOnUiThread(() -> {
                deckList.add(deck);
                deckAdapter.notifyItemInserted(deckList.size() - 1);

                // Set the current deck ID to the newly created deck
                deckId = deck.id;
            });
        }).start();
    }


    private void deleteDeckFromDatabase(Deck deck) {
        new Thread(() -> {
            deckDao.delete(deck);

            runOnUiThread(() -> {
                deckList.remove(deck);
                deckAdapter.notifyDataSetChanged();
            });
        }).start();
    }


    private void deleteDeck(int position) {
        Deck deck = deckList.get(position);
        new Thread(() -> {
            deckDao.delete(deck); // Delete from the database
            runOnUiThread(() -> {
                deckList.remove(position); // Remove from the list
                deckAdapter.notifyItemRemoved(position); // Notify adapter
                deckAdapter.notifyItemRangeChanged(position, deckList.size());
            });
        }).start();
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

    private boolean deckExists(String deckName) {
        for (Deck deck : deckList) {
            if (deck.deckName.equals(deckName)) {
                return true;
            }
        }
        return false;
    }

    private void updateDeckName(Deck deck, String newName, int position) {
        new Thread(() -> {
            deck.deckName = newName;
            deckDao.update(deck);

            runOnUiThread(() -> {
                deckList.set(position, deck);
                deckAdapter.notifyItemChanged(position);
            });
        }).start();
    }


    private void showEditDeckDialog(Deck deck, int position) {
        EditText input = new EditText(this);
        input.setText(deck.deckName);
        input.setSelection(deck.deckName.length());

        new AlertDialog.Builder(this)
                .setTitle("Edit Deck Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        showEmptyNameAlert();
                    } else if (deckExists(newName)) {
                        showDuplicateNameAlert();
                    } else {
                        updateDeckName(deck, newName, position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
