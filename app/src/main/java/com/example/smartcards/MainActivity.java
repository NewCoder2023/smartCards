package com.example.smartcards;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcards.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private RecyclerView folderRecyclerView;
    private FolderAdapter folderAdapter;
    private List<String> folderList;

    private AppDatabase db;
    private FolderDao folderDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Room database
        db = AppDatabase.getDatabase(this);
        folderDao = db.folderDao();

        // Initialize folderList
        folderList = new ArrayList<>();

        // Greeting TextView setup
        setupGreetingText();

        // Bottom Navigation
        BottomNavigationView bottomNavigationViewMain = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationViewMain);

        // Setup RecyclerView
        setupFolderRecyclerView();

        // Load existing folders
        loadFolders();

        // Add button logic
        Button addButton = findViewById(R.id.plus_button);
        addButton.setOnClickListener(v -> showAddFolderDialog());
    }

    private void setupGreetingText() {
        TextView greetingText = findViewById(R.id.greeting_text);

        if (greetingText != null) {
            int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            String greeting = (hour < 12) ? "Good Morning!" : (hour < 18) ? "Good Afternoon!" : "Good Evening!";
            greetingText.setText(greeting);
        }
    }

    private void setupFolderRecyclerView() {
        folderRecyclerView = findViewById(R.id.folder_recycler_view);
        folderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        folderAdapter = new FolderAdapter(folderList, this::deleteFolder, position -> {
            String folderName = folderList.get(position);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Choose Action")
                    .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                        if (which == 0) {
                            // Edit folder
                            showEditFolderDialog(folderName, position);
                        } else if (which == 1) {
                            // Delete folder
                            deleteFolder(position);
                        }
                    })
                    .show();
        });

        folderRecyclerView.setAdapter(folderAdapter);
    }

    private void showEditFolderDialog(String oldName, int position) {
        EditText input = new EditText(this);
        input.setText(oldName);
        input.setSelection(oldName.length());

        new AlertDialog.Builder(this)
                .setTitle("Edit Folder Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        showEmptyNameAlert();
                    } else if (folderList.contains(newName)) {
                        showDuplicateNameAlert();
                    } else {
                        updateFolderName(oldName, newName, position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFolderName(String oldName, String newName, int position) {
        new Thread(() -> {
            Folder oldFolder = folderDao.getAllFolders().stream()
                    .filter(f -> f.name.equals(oldName))
                    .findFirst()
                    .orElse(null);
            if (oldFolder != null) {
                oldFolder.name = newName;
                Folder newFolder = new Folder(newName);
                folderDao.insert(newFolder); // Save new folder
                folderDao.delete(oldFolder); // Delete old folder
            }

            runOnUiThread(() -> {
                folderList.set(position, newName);
                folderAdapter.notifyItemChanged(position);
            });
        }).start();
    }

    private void showAddFolderDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter folder name");
        input.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("New Folder")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String folderName = input.getText().toString().trim();
                    if (folderName.isEmpty()) {
                        showEmptyNameAlert();
                    } else if (folderList.contains(folderName)) {
                        showDuplicateNameAlert();
                    } else {
                        addFolder(folderName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEmptyNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Invalid Name")
                .setMessage("Folder name cannot be empty. Please enter a valid name.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDuplicateNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Duplicate Name")
                .setMessage("A folder with this name already exists. Please choose another name.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadFolders() {
        new Thread(() -> {
            List<Folder> folders = folderDao.getAllFolders();
            runOnUiThread(() -> {
                folderList.clear();
                for (Folder folder : folders) {
                    folderList.add(folder.name);
                }
                folderAdapter.notifyDataSetChanged();
            });
        }).start();
    }


    private void addFolder(String folderName) {
        Folder folder = new Folder(folderName);
        new Thread(() -> {
            folderDao.insert(folder);
            runOnUiThread(() -> {
                folderList.add(folderName);
                folderAdapter.notifyItemInserted(folderList.size() - 1);
            });
        }).start();
    }

    private void deleteFolder(int position) {
        String folderName = folderList.get(position);
        new Thread(() -> {
            // Fetch all decks in the folder
            List<Deck> decksToDelete = FlashcardDatabase.getDatabase(this).deckDao().getDecksByFolderName(folderName);

            // Delete all decks in the folder
            for (Deck deck : decksToDelete) {
                FlashcardDatabase.getDatabase(this).deckDao().delete(deck);
            }

            // Delete the folder
            Folder folder = folderDao.getAllFolders().stream()
                    .filter(f -> f.name.equals(folderName))
                    .findFirst()
                    .orElse(null);
            if (folder != null) {
                folderDao.delete(folder);
            }

            runOnUiThread(() -> {
                folderList.remove(position);
                folderAdapter.notifyItemRemoved(position);
                folderAdapter.notifyItemRangeChanged(position, folderList.size());
            });
        }).start();
    }



    private void deleteFolderFromDatabase(String folderName) {
        new Thread(() -> {
            Folder folder = folderDao.getAllFolders().stream()
                    .filter(f -> f.name.equals(folderName))
                    .findFirst()
                    .orElse(null);
            if (folder != null) {
                List<Deck> associatedDecks = FlashcardDatabase.getDatabase(this).deckDao().getAllDecks().stream()
                        .filter(deck -> folderName.equals(deck.folderName))
                        .collect(Collectors.toList());
                for (Deck deck : associatedDecks) {
                    FlashcardDatabase.getDatabase(this).deckDao().delete(deck);
                }
                folderDao.delete(folder);
            }
        }).start();
    }
}