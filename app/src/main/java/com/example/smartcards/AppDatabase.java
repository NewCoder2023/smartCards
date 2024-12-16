package com.example.smartcards;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Folder.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FolderDao folderDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smartCards_database")
                            .fallbackToDestructiveMigration() // Drop and recreate database
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
