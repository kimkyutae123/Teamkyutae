package com.example.project_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// 검색 추천 어댑터
public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder>
{
    private List<SearchSuggestion> suggestions;
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener
    {
        void onSuggestionClick(SearchSuggestion suggestion);
    }

    public SearchSuggestionAdapter(List<SearchSuggestion> suggestions, OnSuggestionClickListener listener)
    {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        SearchSuggestion suggestion = suggestions.get(position);
        holder.textName.setText(suggestion.getName());
        holder.textAddress.setText(suggestion.getAddress());
        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(suggestion));
    }

    @Override
    public int getItemCount()
    {
        return suggestions.size();
    }

    public void updateSuggestions(List<SearchSuggestion> newSuggestions)
    {
        this.suggestions = newSuggestions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textName;
        TextView textAddress;

        ViewHolder(View itemView)
        {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
        }
    }
} 