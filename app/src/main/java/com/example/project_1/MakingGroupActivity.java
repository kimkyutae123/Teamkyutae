package com.example.project_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MakingGroupActivity extends AppCompatActivity
{
    private EditText groupNameEditText;
    private Button addMemberButton;
    private Button createGroupButton;
    private TextView groupMembersTextView;
    private ArrayList<String> groupMembersList = new ArrayList<>();
    private ArrayList<String> memberList = new ArrayList<>();
    private ListView memberListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makinggroup);

        initializeViews();
        setupInviteButton();
        setupCreateButton();
        setupMemberList();
    }

    private void initializeViews()
    {
        groupNameEditText = findViewById(R.id.groupNameEditText);
        addMemberButton = findViewById(R.id.addMemberButton);
        createGroupButton = findViewById(R.id.createGroupButton);
        groupMembersTextView = findViewById(R.id.groupMembersTextView);
        memberListView = findViewById(R.id.memberListView);
    }

    private void setupInviteButton()
    {
        addMemberButton.setOnClickListener(v ->
        {
            // 초대할 사용자의 고유번호를 입력받는 다이얼로그 표시
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("초대할 사용자의 고유번호를 입력하세요");

            new AlertDialog.Builder(this)
                .setTitle("멤버 초대")
                .setView(input)
                .setPositiveButton("초대", (dialog, which) ->
                {
                    String userNumber = input.getText().toString();
                    if (userNumber.length() == 6)
                    {
                        // 테스트 사용자 목록에서 해당 번호 찾기
                        String[] testUserNumbers = {"123456", "234567", "345678"};
                        String[] testUserNames = {"김철수", "이영희", "박지성"};
                        
                        for (int i = 0; i < testUserNumbers.length; i++)
                        {
                            if (testUserNumbers[i].equals(userNumber))
                            {
                                // 초대 및 수락 상태 저장
                                SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("invited_" + userNumber, true);
                                editor.putBoolean("agreed_" + userNumber, true);
                                editor.apply();

                                // 그룹원 목록에 추가
                                String memberInfo = testUserNames[i] + " (" + userNumber + ")";
                                groupMembersList.add(memberInfo);
                                updateGroupMembersList();
                                
                                Toast.makeText(this, testUserNames[i] + "님이 초대를 수락했습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Toast.makeText(this, "해당 번호의 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(this, "올바른 고유번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
        });
    }

    private void setupCreateButton()
    {
        createGroupButton.setOnClickListener(v ->
        {
            createGroup();
        });
    }

    private void setupMemberList() {
        memberList.clear();  // 기존 목록 초기화

        // 현재 사용자를 리스트의 맨 위에 추가 (방장으로 표시)
        memberList.add("나 (방장)");

        // 초대된 멤버들 추가
        SharedPreferences groupPrefs = getSharedPreferences("group_prefs", MODE_PRIVATE);
        for (int i = 1; i <= 5; i++) {
            String invitedKey = "invited_" + String.format("%04d", i);
            if (groupPrefs.getBoolean(invitedKey, false)) {
                String memberInfo = "테스트 사용자 " + i + " (" + String.format("%04d", i) + ") (멤버)";
                memberList.add(memberInfo);
            }
        }

        // 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, memberList);
        memberListView.setAdapter(adapter);
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "그룹 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 사용자 정보 가져오기
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String uniqueNumber = prefs.getString("unique_number", "0000");

        // 그룹 정보 저장
        SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = groupPrefs.edit();
        
        // 모든 초대 기록 초기화
        for (int i = 1; i <= 5; i++) {
            String invitedKey = "invited_" + String.format("%04d", i);
            String agreedKey = "agreed_" + String.format("%04d", i);
            editor.remove(invitedKey);
            editor.remove(agreedKey);
        }

        // 새로운 그룹 정보 저장
        editor.putString("group_name", groupName);
        editor.putBoolean("has_group", true);
        editor.putBoolean("is_leader", true);
        
        // 현재 사용자만 그룹 멤버로 추가
        editor.putBoolean("invited_" + uniqueNumber, true);
        editor.putBoolean("agreed_" + uniqueNumber, true);

        editor.apply();

        Toast.makeText(this, "그룹이 생성되었습니다", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateGroupMembersList()
    {
        StringBuilder membersText = new StringBuilder();
        for (String member : groupMembersList)
        {
            membersText.append(member).append("\n");
        }
        groupMembersTextView.setText(membersText.toString());
    }
}