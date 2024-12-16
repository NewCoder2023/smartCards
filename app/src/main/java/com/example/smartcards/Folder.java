package com.example.smartcards;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "folders")
public class Folder {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public Folder(String name) {
        this.name = name;
    }
}