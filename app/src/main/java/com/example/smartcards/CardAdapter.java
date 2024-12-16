package com.example.smartcards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private final List<Flashcard> flashcardList;
    private final OnFlashcardActionListener actionListener;

    public interface OnFlashcardActionListener {
        void onDeleteFlashcard(Flashcard flashcard);
        void onEditFlashcard(Flashcard flashcard, String newQuestion, String newAnswer);
    }

    public CardAdapter(List<Flashcard> flashcardList, OnFlashcardActionListener actionListener) {
        this.flashcardList = flashcardList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Flashcard card = flashcardList.get(position);
        holder.frontTextView.setText(card.question);
        holder.backTextView.setText(card.answer);

        // Handle Edit button click
        holder.editButton.setOnClickListener(v -> {
            showEditDialog(holder.itemView.getContext(), card);
        });

        // Handle Delete button click
        holder.deleteButton.setOnClickListener(v -> {
            actionListener.onDeleteFlashcard(card);
        });
    }

    private void showEditDialog(Context context, Flashcard card) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_card, null);
        EditText questionInput = dialogView.findViewById(R.id.edit_question);
        EditText answerInput = dialogView.findViewById(R.id.edit_answer);

        questionInput.setText(card.question);
        answerInput.setText(card.answer);

        new AlertDialog.Builder(context)
                .setTitle("Edit Flashcard")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newQuestion = questionInput.getText().toString().trim();
                    String newAnswer = answerInput.getText().toString().trim();

                    if (!newQuestion.isEmpty() && !newAnswer.isEmpty()) {
                        actionListener.onEditFlashcard(card, newQuestion, newAnswer);
                    } else {
                        Toast.makeText(context, "Question and Answer cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return flashcardList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView frontTextView;
        TextView backTextView;
        Button editButton;
        Button deleteButton;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            frontTextView = itemView.findViewById(R.id.card_front_text);
            backTextView = itemView.findViewById(R.id.card_back_text);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
