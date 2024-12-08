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

    public DeckAdapter(List<String> deckList) {
        this.deckList = deckList;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deck_item, parent, false); // Use the new deck_item.xml
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

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView deckNameTextView;

        DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            deckNameTextView = itemView.findViewById(R.id.deck_name_text_view);
        }
    }
}
