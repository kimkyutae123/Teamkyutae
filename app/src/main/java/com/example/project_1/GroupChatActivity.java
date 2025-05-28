package com.example.project_1;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.SharedPreferences;

public class GroupChatActivity extends AppCompatActivity
{
    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private NavigationView navigationView;
    private ListView memberListView;
    private RecyclerView chatRecyclerView;
    private EditText editMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        initializeViews();
        setupDrawer();
        setupMemberList();
        setupChat();
    }

    private void initializeViews()
    {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        navigationView = findViewById(R.id.navigationView);
        memberListView = findViewById(R.id.memberListView);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupDrawer()
    {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupMemberList()
    {
        SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        Set<String> members = prefs.getStringSet("group_members", new HashSet<>());
        String myName = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_name", "");
        
        List<String> memberList = new ArrayList<>();
        memberList.add(myName + " (나)");
        
        for (String member : members)
        {
            if (!member.equals(myName))
            {
                memberList.add(member);
            }
        }
        
        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberList);
        memberListView.setAdapter(memberAdapter);
    }

    private void setupChat()
    {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        editMessage.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND)
            {
                sendMessage();
                return true;
            }
            return false;
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage()
    {
        String message = editMessage.getText().toString().trim();
        if (!message.isEmpty())
        {
            // (임시) 서버 연동 필요
            chatMessages.add(new ChatMessage(message, true));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            editMessage.setText("");
        }
    }
}
