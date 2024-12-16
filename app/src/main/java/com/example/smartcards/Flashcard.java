package com.example.smartcards;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards",
        foreignKeys = @ForeignKey(entity = Deck.class,
                parentColumns = "id",
                childColumns = "deck_id",
                onDelete = ForeignKey.CASCADE))
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String question;
    public String answer;

    public int deck_id;
}

