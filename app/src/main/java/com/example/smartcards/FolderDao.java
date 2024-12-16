package com.example.smartcards;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FolderDao {
    @Insert
    void insert(Folder folder);

    @Query("SELECT * FROM folders")
    List<Folder> getAllFolders();

    @Delete
    void delete(Folder folder);
}
