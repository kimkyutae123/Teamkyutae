package com.example.project_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GroupActivity extends AppCompatActivity
{
    private Button inviteFriendButton;
    private Button createGroupButton;
    private Button groupAgreeButton;
    private Button groupChatButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);  // activity_group.xml 사용

        // 버튼 연결
        inviteFriendButton = findViewById(R.id.inviteFriendButton);
        createGroupButton = findViewById(R.id.createGroupButton);
        groupAgreeButton = findViewById(R.id.groupAgreeButton);
        groupChatButton = findViewById(R.id.groupChatButton);


        // 친구 초대 버튼 클릭 리스너
        inviteFriendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GroupActivity.this, Friendinvite.class);
                startActivity(intent);
            }
        });

        // 그룹 생성 버튼 클릭 리스너
        createGroupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GroupActivity.this, MakingGroupActivity.class);
                startActivity(intent);
            }
        });

        // 그룹원 동의 버튼 클릭 리스너
        groupAgreeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GroupActivity.this, GroupAgreeActivity.class);
                startActivity(intent);
            }
        });

        // 그룹 채팅 버튼 클릭 리스너
        groupChatButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GroupActivity.this, GroupChatActivity.class);
                startActivity(intent);
            }
        });
    }
}