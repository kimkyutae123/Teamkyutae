package com.example.project_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int VIEW_TYPE_LEFT = 0;
    private static final int VIEW_TYPE_RIGHT = 1;

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList)
    {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position)
    {
        return messageList.get(position).isMine() ? VIEW_TYPE_RIGHT : VIEW_TYPE_LEFT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;

        if (viewType == VIEW_TYPE_RIGHT)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
            return new RightViewHolder(view);
        }
        else
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
            return new LeftViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ChatMessage msg = messageList.get(position);

        if (holder instanceof RightViewHolder)
        {
            ((RightViewHolder) holder).textMessage.setText(msg.getMessage());
        }
        else
        {
            ((LeftViewHolder) holder).textMessage.setText(msg.getMessage());
        }
    }

    @Override
    public int getItemCount()
    {
        return messageList.size();
    }

    static class LeftViewHolder extends RecyclerView.ViewHolder
    {
        TextView textMessage;

        public LeftViewHolder(View itemView)
        {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
        }
    }

    static class RightViewHolder extends RecyclerView.ViewHolder
    {
        TextView textMessage;

        public RightViewHolder(View itemView)
        {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
        }
    }
}