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
import java.util.Arrays;
import java.util.List;

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

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        navigationView = findViewById(R.id.navigationView);
        memberListView = findViewById(R.id.memberListView);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);

        // 햄버거 버튼 클릭 시 Drawer 열기
        btnMenu.setOnClickListener(v ->
        {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // 그룹원 리스트 예시
        List<String> members = Arrays.asList("홍길동", "김영희", "이철수");
        ArrayAdapter<String> memberAdapter = new ArrayAdapter<> (this, android.R.layout.simple_list_item_1, members);
        memberListView.setAdapter(memberAdapter);

        // 채팅 RecyclerView 설정
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // 전송 버튼
        btnSend.setOnClickListener(v ->
        {
            String message = editMessage.getText().toString().trim();
            if (!message.isEmpty())
            {
                chatMessages.add(new ChatMessage(message, true)); // true = 내가 보낸 메시지
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                editMessage.setText("");
            }
        });
    }
}
