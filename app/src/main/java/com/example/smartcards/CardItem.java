package com.example.smartcards;

import android.net.Uri;

public class CardItem {
    private Uri fileUri;
    private String fileType;
    private String frontText;
    private String backText;

    public CardItem(Uri fileUri, String fileType) {
        this.fileUri = fileUri;
        this.fileType = fileType;
    }

    public Uri getFileUri() { return fileUri; }
    public String getFileType() { return fileType; }
    public String getFrontText() { return frontText; }
    public void setFrontText(String frontText) { this.frontText = frontText; }
    public String getBackText() { return backText; }
    public void setBackText(String backText) { this.backText = backText; }
}