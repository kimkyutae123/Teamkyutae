package com.example.project_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Friendinvite extends AppCompatActivity
{
    private EditText friendIdInput;
    private Button sendInviteButton;
    private ListView invitedFriendList;
    private ArrayList<String> invitedFriends;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendinvite);

        // UI 요소 연결
        friendIdInput = findViewById(R.id.friendIdInput);
        sendInviteButton = findViewById(R.id.sendInviteButton);
        invitedFriendList = findViewById(R.id.invitedFriendList);

        // 초대된 친구 목록을 저장할 리스트
        invitedFriends = new ArrayList<>();

        // 어댑터 설정
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, invitedFriends);
        invitedFriendList.setAdapter(adapter);

        // 초대 버튼 클릭 시 친구를 목록에 추가
        sendInviteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String friendId = friendIdInput.getText().toString();

                // 친구 ID가 비어있지 않은지 확인
                if (!friendId.isEmpty())
                {
                    // 친구가 이미 초대 목록에 있으면, 추가하지 않음
                    if (invitedFriends.contains(friendId))
                    {
                        Toast.makeText(Friendinvite.this, "이미 초대한 친구입니다!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        // 친구를 초대 목록에 추가
                        invitedFriends.add(friendId);
                        adapter.notifyDataSetChanged();  // 리스트 갱신
                        Toast.makeText(Friendinvite.this, "친구 초대 완료!", Toast.LENGTH_SHORT).show();  // 초대 완료 메시지
                    }

                    // 입력창 초기화
                    friendIdInput.setText("");
                }
                else
                {
                    // 친구 ID가 비어있을 경우 알림
                    Toast.makeText(Friendinvite.this, "친구 ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
