package com.example.smartcards;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardDao {

    @Insert
    long insert(Flashcard flashcard);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);


    @Query("SELECT * FROM flashcards WHERE deck_id = :deckId")
    List<Flashcard> getFlashcardsByDeck(int deckId);
}
