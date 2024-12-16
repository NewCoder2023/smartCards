package com.example.smartcards;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "decks")
public class Deck {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String deckName;

    public String folderName;


    public Deck(String deckName, String folderName) {
        this.deckName = deckName;
        this.folderName = folderName;
    }

    // Default constructor for Room
    public Deck() {}
}

