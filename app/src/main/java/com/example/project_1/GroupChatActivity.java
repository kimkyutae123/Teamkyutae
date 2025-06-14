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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private List<String> groupMembersList = new ArrayList<>();
    private String currentGroupId = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        // 그룹 ID 불러오기 (없으면 default)
        SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        currentGroupId = groupPrefs.getString("group_id", "default");

        initializeViews();
        setupDrawer();
        setupMemberList();
        setupChat();
        loadChatMessages();
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
        loadGroupMembers();
        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupMembersList);
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
            chatMessages.add(new ChatMessage(message, true));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            editMessage.setText("");
            saveChatMessages();
        }
    }

    private void saveChatMessages() {
        SharedPreferences prefs = getSharedPreferences("GroupChatPrefs", MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        for (ChatMessage msg : chatMessages) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("message", msg.getMessage());
                obj.put("isMine", msg.isMine());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }
        prefs.edit().putString("GroupChat_" + currentGroupId, jsonArray.toString()).apply();
    }

    private void loadChatMessages() {
        SharedPreferences prefs = getSharedPreferences("GroupChatPrefs", MODE_PRIVATE);
        String json = prefs.getString("GroupChat_" + currentGroupId, null);
        chatMessages.clear();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String message = obj.getString("message");
                    boolean isMine = obj.getBoolean("isMine");
                    chatMessages.add(new ChatMessage(message, isMine));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (chatAdapter != null) chatAdapter.notifyDataSetChanged();
    }

    // 그룹 삭제/나가기 시 아래 메서드로 채팅 내역 삭제
    public static void clearChatHistory(android.content.Context context, String groupId) {
        SharedPreferences prefs = context.getSharedPreferences("GroupChatPrefs", MODE_PRIVATE);
        prefs.edit().remove("GroupChat_" + groupId).apply();
    }

    private void loadGroupMembers() {
        SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        groupMembersList.clear();
        
        // 현재 사용자(나) 추가
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String myName = userPrefs.getString("user_name", "나");
        String myNumber = userPrefs.getString("user_number", "");
        String myInfo = myName + " (나)";
        groupMembersList.add(myInfo);
        
        // 초대된 멤버들 중 동의한 멤버들을 로드
        String[] testUserNumbers = {"123456", "234567", "345678"};
        String[] testUserNames = {"김철수", "이영희", "박지성"};
        
        for (int i = 0; i < testUserNumbers.length; i++) {
            if (prefs.getBoolean("invited_" + testUserNumbers[i], false) && 
                prefs.getBoolean("agreed_" + testUserNumbers[i], false)) {
                String memberInfo = testUserNames[i] + " (" + testUserNumbers[i] + ")";
                groupMembersList.add(memberInfo);
            }
        }
        
        updateGroupMembersList();
    }

    private void updateGroupMembersList() {
        // Implementation of updateGroupMembersList method
    }
}
