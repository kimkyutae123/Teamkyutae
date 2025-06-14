package com.example.project_1;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.content.Intent;
import android.util.Log;

public class MakingGroupActivity extends AppCompatActivity
{
    private static final String TAG = "MakingGroupActivity";
    private EditText groupNameEditText;
    private Button addMemberButton;
    private Button createGroupButton;
    private ListView memberListView;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private String currentGroupName;

    private ArrayList<String> memberList;
    private ArrayAdapter<String> memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makinggroup);

        dbHelper = new DatabaseHelper(this);
        
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userIdStr = prefs.getString("userId", null);
        String userName = prefs.getString("userName", null);

        // 아이디와 이름이 설정되지 않은 경우
        if (userIdStr == null || userName == null) {
            Toast.makeText(this, "먼저 마이페이지에서 아이디와 이름을 설정해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = Integer.parseInt(userIdStr);

        // 뷰 연결
        groupNameEditText = findViewById(R.id.groupNameEditText);
        groupNameEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        groupNameEditText.setFilters(new android.text.InputFilter[] { new android.text.InputFilter.LengthFilter(20) });
        addMemberButton = findViewById(R.id.addMemberButton);
        createGroupButton = findViewById(R.id.createGroupButton);
        memberListView = findViewById(R.id.memberListView);

        // 멤버 리스트 초기화
        memberList = new ArrayList<>();
        // 현재 사용자를 멤버 리스트에 추가
        memberList.add(userName + " (방장)");

        memberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberList);
        memberListView.setAdapter(memberAdapter);

        // 멤버 추가 버튼 이벤트
        addMemberButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("멤버 추가")
                .setMessage("추가할 멤버의 6자리 ID를 입력하세요")
                .setView(new EditText(this))
                .setPositiveButton("추가", (dialog, which) -> {
                    EditText input = ((AlertDialog) dialog).findViewById(android.R.id.text1);
                    String targetUserIdStr = input.getText().toString();
                    
                    if (targetUserIdStr.length() == 6) {
                        int targetUserId = Integer.parseInt(targetUserIdStr);
                        
                        // 로컬 DB에 사용자 정보가 있는 경우
                        Cursor memberCursor = dbHelper.getUser(targetUserId);
                        if (memberCursor != null && memberCursor.moveToFirst()) {
                            String memberName = memberCursor.getString(memberCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
                            String memberWithStatus = memberName + " (초대보냄)";
                            
                            if (!memberList.contains(memberWithStatus) && 
                                targetUserId != currentUserId) {
                                memberList.add(memberWithStatus);
                                memberAdapter.notifyDataSetChanged();
                                Toast.makeText(this, "멤버가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "이미 추가된 멤버입니다.", Toast.LENGTH_SHORT).show();
                            }
                            memberCursor.close();
                        } else {
                            Toast.makeText(this, "존재하지 않는 사용자입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "6자리 숫자를 입력하세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
        });

        // 그룹 생성 버튼 이벤트
        createGroupButton.setOnClickListener(v -> {
            String groupName = groupNameEditText.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(this, "그룹 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 그룹 생성
            long groupId = dbHelper.createGroup(groupName, "", currentUserId);
            if (groupId != -1) {
                Toast.makeText(this, "그룹이 생성되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "그룹 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}