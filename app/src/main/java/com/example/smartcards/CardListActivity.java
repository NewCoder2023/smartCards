package com.example.smartcards;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcards.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CardListActivity extends AppCompatActivity {
    private RecyclerView cardRecyclerView;
    private List<Flashcard> flashcardList;
    private CardAdapter cardAdapter;

    private FlashcardDao flashcardDao;
    private int deckId; // Deck ID for filtering flashcards

    private final ActivityResultLauncher<Intent> manualCardLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String question = result.getData().getStringExtra("NEW_CARD_FRONT");
                    String answer = result.getData().getStringExtra("NEW_CARD_BACK");

                    // Replace 'currentDeckId' with the variable holding the deck ID
                    addFlashcardToDatabase(question, answer, deckId);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        // Get the Deck ID from the intent
        deckId = getIntent().getIntExtra("DECK_ID", -1);

        if (deckId == -1) {
            Toast.makeText(this, "Invalid deck selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if the deck exists in the database
        new Thread(() -> {
            Deck deck = FlashcardDatabase.getDatabase(this).deckDao().getDeckById(deckId);
            if (deck == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Deck no longer exists", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                runOnUiThread(this::initializeUI); // Proceed with setting up UI
            }
        }).start();


        // Proceed with initialization
        String deckName = getIntent().getStringExtra("DECK_NAME");

        // Bottom Navigation
        BottomNavigationView bottomNavigationViewMain = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationViewMain);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (deckName != null && !deckName.isEmpty()) {
                getSupportActionBar().setTitle(deckName);
            } else {
                getSupportActionBar().setTitle("Your Cards");
            }
        }

        // Initialize database
        flashcardDao = FlashcardDatabase.getDatabase(this).flashcardDao();

        // Setup RecyclerView
        setupCardRecyclerView();

        // Add card FAB
        FloatingActionButton addCardFab = findViewById(R.id.add_card_fab);
        addCardFab.setOnClickListener(v -> showCardCreationOptions());
    }

    private boolean isDeckIdValid(int deckId) {
        final boolean[] isValid = {false};
        new Thread(() -> {
            Deck deck = FlashcardDatabase.getDatabase(this).deckDao().getDeckById(deckId);
            isValid[0] = (deck != null);
        }).start();
        return isValid[0];
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupCardRecyclerView() {
        cardRecyclerView = findViewById(R.id.card_recycler_view);
        flashcardList = new ArrayList<>();

        // Load flashcards from the database
        loadFlashcards();

        cardAdapter = new CardAdapter(flashcardList, new CardAdapter.OnFlashcardActionListener() {
            @Override
            public void onDeleteFlashcard(Flashcard flashcard) {
                // Show confirmation dialog before deleting
                new android.app.AlertDialog.Builder(CardListActivity.this)
                        .setTitle("Delete Flashcard")
                        .setMessage("Are you sure you want to delete this flashcard?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // If user confirms, delete the flashcard
                            new Thread(() -> {
                                flashcardDao.delete(flashcard);
                                runOnUiThread(() -> {
                                    flashcardList.remove(flashcard);
                                    cardAdapter.notifyDataSetChanged();
                                    Toast.makeText(CardListActivity.this, "Flashcard deleted", Toast.LENGTH_SHORT).show();
                                });
                            }).start();
                        })
                        .setNegativeButton("No", null) // If user cancels, do nothing
                        .show();
            }


            @Override
            public void onEditFlashcard(Flashcard flashcard, String newQuestion, String newAnswer) {
                flashcard.question = newQuestion;
                flashcard.answer = newAnswer;

                // Update flashcard in the database
                new Thread(() -> {
                    flashcardDao.update(flashcard);
                    runOnUiThread(() -> {
                        cardAdapter.notifyDataSetChanged();
                        Toast.makeText(CardListActivity.this, "Flashcard updated", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        });

        cardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardRecyclerView.setAdapter(cardAdapter);
    }


    private void loadFlashcards() {
        new Thread(() -> {
            List<Flashcard> cards = flashcardDao.getFlashcardsByDeck(deckId); // Fetch flashcards for this deck
            runOnUiThread(() -> {
                flashcardList.clear();
                flashcardList.addAll(cards);
                cardAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void addFlashcardToDatabase(String question, String answer, int deckId) {
        new Thread(() -> {
            Deck deck = FlashcardDatabase.getDatabase(this).deckDao().getDeckById(deckId);
            if (deck == null) {
                runOnUiThread(() -> Toast.makeText(this, "Error: Invalid deck", Toast.LENGTH_SHORT).show());
                return;
            }

            Flashcard flashcard = new Flashcard();
            flashcard.question = question;
            flashcard.answer = answer;
            flashcard.deck_id = deckId;

            FlashcardDatabase.getDatabase(this).flashcardDao().insert(flashcard);

            runOnUiThread(() -> {
                flashcardList.add(flashcard);
                cardAdapter.notifyItemInserted(flashcardList.size() - 1);
                cardRecyclerView.scrollToPosition(flashcardList.size() - 1);
                Toast.makeText(this, "Flashcard added", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }


    private void showCardCreationOptions() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Create Flashcard")
                .setItems(new CharSequence[]{"Create Manually", "Generate with AI"}, (dialog, which) -> {
                    if (which == 0) {
                        openManualCardCreation();
                    } else if (which == 1) {
                        openAICardGeneration();
                    }
                })
                .show();
    }

    private void openManualCardCreation() {
        Intent intent = new Intent(this, ManualCardCreationActivity.class);
        manualCardLauncher.launch(intent);
    }

    private void openAICardGeneration() {
        // Placeholder for AI card generation

    }

    private void initializeUI() {
        String deckName = getIntent().getStringExtra("DECK_NAME");

        BottomNavigationView bottomNavigationViewMain = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationViewMain);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (deckName != null && !deckName.isEmpty()) {
                getSupportActionBar().setTitle(deckName);
            } else {
                getSupportActionBar().setTitle("Your Cards");
            }
        }

        flashcardDao = FlashcardDatabase.getDatabase(this).flashcardDao();
        setupCardRecyclerView();

        FloatingActionButton addCardFab = findViewById(R.id.add_card_fab);
        addCardFab.setOnClickListener(v -> showCardCreationOptions());
    }

}