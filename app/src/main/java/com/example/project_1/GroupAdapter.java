package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<GroupMember> memberList;

    public GroupAdapter(List<GroupMember> memberList) {
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupMember member = memberList.get(position);
        holder.nameText.setText(member.getName());
        holder.statusText.setText(member.hasConsented() ? "동의함" : "동의 안 함");
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, statusText;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textName);
            statusText = itemView.findViewById(R.id.textStatus);
        }
    }
}
