package com.example.project_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.sql.DriverManager;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class Friendinvite extends AppCompatActivity {
    private EditText friendIdInput;
    private Button sendInviteButton;
    private ListView invitedFriendList;
    private ArrayList<String> invitedFriends;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        sendInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendId = friendIdInput.getText().toString();

                if (!friendId.isEmpty()) {
                    if (invitedFriends.contains(friendId)) {
                        Toast.makeText(Friendinvite.this, "이미 초대한 친구입니다!", Toast.LENGTH_SHORT).show();
                    } else {
                        invitedFriends.add(friendId);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(Friendinvite.this, "친구 초대 완료!", Toast.LENGTH_SHORT).show();

                        // ✅ DB에 저장하기 (새 스레드에서 실행)
                        new Thread(() -> {
                            try {
                                // JDBC 드라이버 로드
                                Class.forName("com.mysql.cj.jdbc.Driver"); // 또는 com.mysql.cj.jdbc.Driver
                                // 연결 설정
                                Connection conn = DriverManager.getConnection(
                                        "jdbc:mysql://192.168.111.1:3306/FriendInvite?useSSL=false",
                                        "root", "1234"
                                );

                                // INSERT 쿼리 실행
                                String sql = "INSERT INTO FriendInvite (id, friendinventory) VALUES (?, ?)";
                                PreparedStatement stmt = conn.prepareStatement(sql);
                                stmt.setString(1, "<사용자ID>"); // 예: 로그인된 사용자 ID
                                stmt.setString(2, friendId);    // 초대한 친구 ID
                                stmt.executeUpdate();

                                conn.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }

                    friendIdInput.setText("");
                } else {
                    Toast.makeText(Friendinvite.this, "친구 ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}