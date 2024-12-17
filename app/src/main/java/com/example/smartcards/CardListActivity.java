package com.example.smartcards;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcards.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CardListActivity extends AppCompatActivity {
    private RecyclerView cardRecyclerView;
    private List<Flashcard> flashcardList;
    private CardAdapter cardAdapter;
    private boolean isPDFPermissionRequested = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> manualCardLauncher;

    private FlashcardDao flashcardDao;
    private int deckId; // Deck ID for filtering flashcards
    private Uri currentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        // Initialize the launchers
        setupLaunchers();

        // Get deck ID and initialize UI
        deckId = getIntent().getIntExtra("DECK_ID", -1);
        if (deckId == -1) {
            Toast.makeText(this, "Invalid deck selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            Deck deck = FlashcardDatabase.getDatabase(this).deckDao().getDeckById(deckId);
            runOnUiThread(() -> {
                if (deck == null) {
                    Toast.makeText(this, "Deck no longer exists", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    initializeUI();
                }
            });
        }).start();
    }

    private void setupLaunchers() {
        // Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGalleryImage(result.getData().getData());
                    }
                });

        // PDF Picker Launcher
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handlePDFFile(result.getData().getData());
                    }
                });

        // Camera Launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        handleCameraImage();
                    }
                });

        // Manual Card Creation Launcher
        manualCardLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String question = result.getData().getStringExtra("NEW_CARD_FRONT");
                        String answer = result.getData().getStringExtra("NEW_CARD_BACK");
                        addFlashcardToDatabase(question, answer);
                    }
                });
    }

    private void initializeUI() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String deckName = getIntent().getStringExtra("DECK_NAME");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show the back button
            getSupportActionBar().setTitle(deckName != null ? deckName : "Your Cards");
        }

        flashcardDao = FlashcardDatabase.getDatabase(this).flashcardDao();
        setupCardRecyclerView();

        FloatingActionButton addCardFab = findViewById(R.id.add_card_fab);
        addCardFab.setOnClickListener(v -> showCardCreationOptions());
    }


    private void setupCardRecyclerView() {
        cardRecyclerView = findViewById(R.id.card_recycler_view);
        flashcardList = new ArrayList<>();
        cardAdapter = new CardAdapter(flashcardList, new CardAdapter.OnFlashcardActionListener() {
            @Override
            public void onDeleteFlashcard(Flashcard flashcard) {
                deleteFlashcard(flashcard); // Call the deletion logic
            }

            @Override
            public void onEditFlashcard(Flashcard flashcard, String newQuestion, String newAnswer) {
                editFlashcard(flashcard, newQuestion, newAnswer);
            }
        });
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardRecyclerView.setAdapter(cardAdapter);
        loadFlashcards();
    }

    private void editFlashcard(Flashcard flashcard, String newQuestion, String newAnswer) {
        new Thread(() -> {
            // Update flashcard details
            flashcard.question = newQuestion;
            flashcard.answer = newAnswer;
            flashcardDao.update(flashcard); // Update the database

            // Update the UI on the main thread
            runOnUiThread(() -> {
                int position = flashcardList.indexOf(flashcard);
                if (position != -1) {
                    flashcardList.set(position, flashcard);
                    cardAdapter.notifyItemChanged(position);
                    Toast.makeText(this, "Flashcard updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error updating flashcard", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }


    private void loadFlashcards() {
        new Thread(() -> {
            List<Flashcard> cards = flashcardDao.getFlashcardsByDeck(deckId);
            runOnUiThread(() -> {
                flashcardList.clear();
                flashcardList.addAll(cards);
                cardAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void deleteFlashcard(Flashcard flashcard) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Flashcard")
                .setMessage("Are you sure you want to delete this flashcard?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        // Delete the flashcard from the database
                        flashcardDao.delete(flashcard);

                        // Update the UI on the main thread
                        runOnUiThread(() -> {
                            int position = flashcardList.indexOf(flashcard);
                            if (position != -1) {
                                flashcardList.remove(position);
                                cardAdapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Flashcard deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error deleting flashcard", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }




    private void showCardCreationOptions() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Create Flashcard")
                .setItems(new CharSequence[]{"Create Manually", "Generate with AI"}, (dialog, which) -> {
                    if (which == 0) {
                        openManualCardCreation();
                    } else {
                        showAIGenerationOptions();
                    }
                }).show();
    }

    private void openManualCardCreation() {
        Intent intent = new Intent(this, ManualCardCreationActivity.class);
        manualCardLauncher.launch(intent);
    }

    private void showAIGenerationOptions() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Generate with AI")
                .setItems(new CharSequence[]{"Take a Picture", "Upload Image", "Upload PDF"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openCamera();
                            break;
                        case 1:
                            openImagePicker();
                            break;
                        case 2:
                            openPDFPicker();
                            break;
                    }
                }).show();
    }

    private void openCamera() {
        File photoFile = createImageFile();
        if (photoFile != null) {
            currentPhotoUri = FileProvider.getUriForFile(this, "com.example.smartcards.fileprovider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            cameraLauncher.launch(intent);
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        } catch (IOException e) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(intent);
    }

    private void openPDFPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(intent);
    }

    private void handleCameraImage() {
        processImageForAI(currentPhotoUri);
    }

    private void handleGalleryImage(Uri imageUri) {
        processImageForAI(imageUri);
    }

    private void handlePDFFile(Uri pdfUri) {
        processPDFForAI(pdfUri);
    }

    private void processImageForAI(Uri fileUri) {
        Toast.makeText(this, "Processing Image: " + fileUri, Toast.LENGTH_SHORT).show();
    }

    private void processPDFForAI(Uri fileUri) {
        Toast.makeText(this, "Processing PDF: " + fileUri, Toast.LENGTH_SHORT).show();
    }

    private void addFlashcardToDatabase(String question, String answer) {
        new Thread(() -> {
            Flashcard flashcard = new Flashcard();
            flashcard.question = question;
            flashcard.answer = answer;
            flashcard.deck_id = deckId;

            flashcardDao.insert(flashcard);
            runOnUiThread(() -> {
                flashcardList.add(flashcard);
                cardAdapter.notifyItemInserted(flashcardList.size() - 1);
                cardRecyclerView.scrollToPosition(flashcardList.size() - 1);
                Toast.makeText(this, "Flashcard added", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
