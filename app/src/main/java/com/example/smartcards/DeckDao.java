package com.example.smartcards;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeckDao {
    @Insert
    long insert(Deck deck);

    @Query("SELECT * FROM decks")
    List<Deck> getAllDecks();

    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    Deck getDeckById(int deckId); // New method to fetch Deck by ID

    @Query("SELECT * FROM decks WHERE folderName = :folderName")
    List<Deck> getDecksByFolderName(String folderName);



    @Delete
    void delete(Deck deck);
}
