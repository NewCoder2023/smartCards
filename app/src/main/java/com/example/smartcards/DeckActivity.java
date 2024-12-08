package com.example.smartcards;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcards.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        // Set the folder name as the title
        TextView titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText(folderName);

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

    private void showAddDeckDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter deck name");

        new AlertDialog.Builder(this)
                .setTitle("New Deck")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String deckName = input.getText().toString().trim();
                    if (!deckName.isEmpty() && !deckList.contains(deckName)) {
                        deckList.add(deckName);
                        deckAdapter.notifyItemInserted(deckList.size() - 1);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
