package com.example.project_1;  // 패키지 경로는 실제 프로젝트에 맞게 수정

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GroupActivity extends AppCompatActivity {

    private Button inviteFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);  // activity_group.xml 파일을 사용

        // 버튼 연결
        inviteFriendButton = findViewById(R.id.inviteFriendButton);

        // 친구 초대 버튼 클릭 리스너 설정
        inviteFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 친구 초대 화면으로 이동
                Intent intent = new Intent(GroupActivity.this, Friendinvite.class);  // FriendInvite는 친구 초대 Activity 클래스
                startActivity(intent);
            }
        });
    }
}
