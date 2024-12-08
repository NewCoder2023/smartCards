package com.example.smartcards;

import android.app.AlertDialog;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SmartCardsPrefs";
    private static final String KEY_FOLDERS = "Folders";

    private RecyclerView folderRecyclerView;
    private FolderAdapter folderAdapter;
    private List<String> folderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Greeting TextView setup (same as before)
        setupGreetingText();

        // Bottom Navigation
        BottomNavigationView bottomNavigationViewMain = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNavigationViewMain);

        // Setup RecyclerView
        setupFolderRecyclerView();

        // Add button logic
        Button addButton = findViewById(R.id.plus_button);
        addButton.setOnClickListener(v -> showAddFolderDialog());
    }

    private void setupGreetingText() {
        // Find the TextView for the greeting
        TextView greetingText = findViewById(R.id.greeting_text);

        // Check if the TextView exists in the layout
        if (greetingText != null) {
            // Get the current hour of the day
            int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

            // Determine the greeting based on the time of day
            String greeting;
            if (hour < 12) {
                greeting = "Good Morning!";
            } else if (hour < 18) {
                greeting = "Good Afternoon!";
            } else {
                greeting = "Good Evening!";
            }

            // Set the greeting text
            greetingText.setText(greeting);
        }
    }


    private void setupFolderRecyclerView() {
        folderRecyclerView = findViewById(R.id.folder_recycler_view);
        folderList = new ArrayList<>();

        // Load saved folders
        loadFolders();

        folderAdapter = new FolderAdapter(folderList, this::deleteFolder);
        folderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        folderRecyclerView.setAdapter(folderAdapter);

        // Add swipe-to-delete functionality
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private Drawable deleteIcon;
            private ColorDrawable background;

            {
                deleteIcon = ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_delete);
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
                String deletedFolder = folderList.get(position);

                // Remove from adapter
                folderAdapter.removeItem(position);

                // Remove from SharedPreferences
                removeFolder(deletedFolder);

                // Show undo Snackbar
                Snackbar.make(folderRecyclerView, "Folder deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            // Restore the deleted folder
                            folderList.add(position, deletedFolder);
                            folderAdapter.notifyItemInserted(position);
                            saveFolder(deletedFolder);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState,
                                    boolean isCurrentlyActive) {
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
        }).attachToRecyclerView(folderRecyclerView);
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
                    } else if (folderList.contains(folderName)) { // Check if the name already exists
                        showDuplicateNameAlert(); // Notify user about duplicate name
                    } else {
                        addFolder(folderName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDuplicateNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Duplicate Name")
                .setMessage("A folder with this name already exists. Please choose another name.")
                .setPositiveButton("OK", null)
                .show();
    }


    private void addFolder(String folderName) {
        // Add to list and adapter
        folderList.add(folderName);
        folderAdapter.notifyItemInserted(folderList.size() - 1);

        // Save to SharedPreferences
        saveFolder(folderName);
    }

    private void deleteFolder(int position) {
        String deletedFolder = folderList.get(position);
        folderList.remove(position);
        folderAdapter.notifyItemRemoved(position);
        removeFolder(deletedFolder);
    }

    private void showEmptyNameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Invalid Name")
                .setMessage("Folder name cannot be empty. Please enter a valid name.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void saveFolder(String folderName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> folderSet = sharedPreferences.getStringSet(KEY_FOLDERS, new HashSet<>());
        Set<String> updatedFolderSet = new HashSet<>(folderSet);
        updatedFolderSet.add(folderName);

        sharedPreferences.edit().putStringSet(KEY_FOLDERS, updatedFolderSet).apply();
    }

    private void removeFolder(String folderName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> folderSet = sharedPreferences.getStringSet(KEY_FOLDERS, new HashSet<>());
        Set<String> updatedFolderSet = new HashSet<>(folderSet);
        updatedFolderSet.remove(folderName);

        sharedPreferences.edit().putStringSet(KEY_FOLDERS, updatedFolderSet).apply();
    }

    private void loadFolders() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> folderSet = sharedPreferences.getStringSet(KEY_FOLDERS, new HashSet<>());

        folderList.clear();
        folderList.addAll(folderSet);
    }
}