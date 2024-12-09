package com.example.smartcards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {
    private List<String> deckList;
    private OnDeckDeleteListener deleteListener;

    public interface OnDeckDeleteListener {
        void onDeckDelete(int position);
    }

    public DeckAdapter(List<String> deckList, OnDeckDeleteListener deleteListener) {
        this.deckList = deckList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deck_item, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        holder.deckNameTextView.setText(deckList.get(position));
    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }

    public void removeItem(int position) {
        deckList.remove(position);
        notifyItemRemoved(position);
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView deckNameTextView;

        DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            deckNameTextView = itemView.findViewById(R.id.deck_name_text_view);
        }
    }
}