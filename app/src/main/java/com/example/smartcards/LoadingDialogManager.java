package com.example.smartcards;

import android.app.ProgressDialog;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class LoadingDialogManager {
    private ProgressDialog progressDialog;
    private final Context context;

    public LoadingDialogManager(Context context) {
        this.context = context;
    }

    public void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Generating flashcards...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    public void hideLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

