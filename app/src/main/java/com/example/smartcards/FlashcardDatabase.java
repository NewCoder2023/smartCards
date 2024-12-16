package com.example.smartcards;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Flashcard.class, Deck.class}, version = 2)
public abstract class FlashcardDatabase extends RoomDatabase {

    public abstract FlashcardDao flashcardDao();
    public abstract DeckDao deckDao();

    private static volatile FlashcardDatabase INSTANCE;

    public static FlashcardDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FlashcardDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FlashcardDatabase.class, "flashcard_database")
                            .fallbackToDestructiveMigration() // Drop and recreate database
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
