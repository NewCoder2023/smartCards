package com.example.smartcards;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AICardGenerationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_card_generation);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AI Card Generation");
        }

        // Initialize UI elements
        EditText topicEditText = findViewById(R.id.topic_edit_text);
        Button generateButton = findViewById(R.id.generate_cards_button);

        // Handle the button click
        generateButton.setOnClickListener(v -> {
            String topic = topicEditText.getText().toString().trim();
            if (topic.isEmpty()) {
                Toast.makeText(this, "Please enter a topic to generate cards.", Toast.LENGTH_SHORT).show();
            } else {
                // Placeholder action for generating cards
                Toast.makeText(this, "AI card generation is not yet implemented.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
