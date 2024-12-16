package com.example.smartcards;

import android.graphics.Canvas;
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

        deckAdapter = new DeckAdapter(deckList, this::deleteDeck);
        deckRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deckRecyclerView.setAdapter(deckAdapter);

        // Add swipe-to-delete functionality
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) { // Allow only LEFT swipe
            private final Drawable deleteIcon = ContextCompat.getDrawable(DeckActivity.this, android.R.drawable.ic_delete);
            private final ColorDrawable background = new ColorDrawable(getResources().getColor(android.R.color.darker_gray));

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // No dragging functionality needed
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Deck deletedDeck = deckList.get(position);

                // Show confirmation dialog before deletion
                new AlertDialog.Builder(DeckActivity.this)
                        .setTitle("Delete Deck")
                        .setMessage("Are you sure you want to delete this deck?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Remove from database
                            deleteDeckFromDatabase(deletedDeck);

                            // Remove from adapter
                            deckList.remove(position);
                            deckAdapter.notifyItemRemoved(position);

                            // Optional: Show a toast message
                            Toast.makeText(DeckActivity.this, "Deck deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Rebind the item to prevent visual deletion
                            deckAdapter.notifyItemChanged(position);
                        })
                        .show();
            }


            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

                // Set background for left swipe
                if (dX < 0) {
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // Set delete icon position
                    int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = itemView.getBottom() - iconMargin;

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }

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
            List<Deck> decks = deckDao.getDecksByFolderName(folderName); // Fetch decks for this folder
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
        Deck deletedDeck = deckList.get(position);
        deckList.remove(position);
        deckAdapter.notifyItemRemoved(position);
        deleteDeckFromDatabase(deletedDeck);
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
}
