package com.example.smartcards;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

public class ManualCardCreationActivity extends AppCompatActivity {

    private TextInputEditText frontEditText;
    private TextInputEditText backEditText;
    private Button saveCardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_card_creation);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Flashcard");
        }

        // Initialize views
        frontEditText = findViewById(R.id.front_edit_text);
        backEditText = findViewById(R.id.back_edit_text);
        saveCardButton = findViewById(R.id.save_card_button);

        // Save card on button click
        saveCardButton.setOnClickListener(v -> saveCard());
    }

    private void saveCard() {
        String frontText = frontEditText.getText().toString().trim();
        String backText = backEditText.getText().toString().trim();

        if (frontText.isEmpty() || backText.isEmpty()) {
            Toast.makeText(this, "Both sides of the card must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the card data back
        Intent resultIntent = new Intent();
        resultIntent.putExtra("NEW_CARD_FRONT", frontText);
        resultIntent.putExtra("NEW_CARD_BACK", backText);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Handle back button in the toolbar
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
