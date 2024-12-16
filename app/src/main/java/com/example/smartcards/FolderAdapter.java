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

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private List<String> folderList;
    private OnFolderDeleteListener deleteListener;

    public interface OnFolderDeleteListener {
        void onFolderDelete(int position);
    }

    public interface OnFolderLongClickListener {
        void onFolderLongClick(int position);
    }
    private OnFolderLongClickListener longClickListener;

    public FolderAdapter(List<String> folderList, OnFolderDeleteListener deleteListener, OnFolderLongClickListener longClickListener) {
        this.folderList = folderList;
        this.deleteListener = deleteListener;
        this.longClickListener = longClickListener;
    }


    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_item, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        if (position >= folderList.size()) {
            return; // Prevent invalid access
        }

        holder.folderNameTextView.setText(folderList.get(position));

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, DeckActivity.class);
            intent.putExtra("FOLDER_NAME", folderList.get(position));
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onFolderLongClick(position);
            }
            return true;
        });
    }



    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public void removeItem(int position) {
        folderList.remove(position);
        notifyItemRemoved(position);
    }

    public String getItem(int position) {
        return folderList.get(position);
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderNameTextView;

        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folder_name_text_view);
           }
    }
}