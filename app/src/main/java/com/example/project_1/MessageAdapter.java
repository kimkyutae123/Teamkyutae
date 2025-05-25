package com.example.project_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderText, contentText;

        public ViewHolder(View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.tvSender);
            contentText = itemView.findViewById(R.id.tvContent);
        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.senderText.setText(message.getSender());
        holder.contentText.setText(message.getContent());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
