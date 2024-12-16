package com.example.smartcards;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {
    private final List<Deck> deckList;
    private final OnDeckDeleteListener deleteListener;

    public interface OnDeckDeleteListener {
        void onDeckDelete(int position);
    }

    public DeckAdapter(List<Deck> deckList, OnDeckDeleteListener deleteListener) {
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
        Deck deck = deckList.get(position);
        holder.deckNameTextView.setText(deck.deckName);

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, CardListActivity.class);
            intent.putExtra("DECK_ID", deck.id);
            intent.putExtra("DECK_NAME", deck.deckName);
            context.startActivity(intent);
        });

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
