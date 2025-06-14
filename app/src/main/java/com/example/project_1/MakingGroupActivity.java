package com.example.project_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MakingGroupActivity extends AppCompatActivity
{
    private EditText groupNameEditText;
    private Button addMemberButton;
    private Button createGroupButton;
    private ListView memberListView;

    private ArrayList<String> memberList;
    private ArrayAdapter<String> memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makinggroup);  // 레이아웃 연결

        // 뷰 연결
        groupNameEditText = findViewById(R.id.groupNameEditText);
        addMemberButton = findViewById(R.id.addMemberButton);
        createGroupButton = findViewById(R.id.createGroupButton);
        memberListView = findViewById(R.id.memberListView);

        // 멤버 리스트 초기화
        memberList = new ArrayList<>();
        // 현재 사용자를 멤버 리스트에 추가
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String myName = prefs.getString("user_name", "") + " (나, 방장)";
        memberList.add(myName);

        memberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberList);
        memberListView.setAdapter(memberAdapter);

        // 멤버 추가 버튼 이벤트
        addMemberButton.setOnClickListener(v ->
        {
            EditText input = new EditText(this);
            input.setHint("추가할 멤버 아이디");
            
            new AlertDialog.Builder(this)
                .setTitle("멤버 추가")
                .setView(input)
                .setPositiveButton("추가", (dialog, which) ->
                {
                    String memberId = input.getText().toString().trim();
                    if (!memberId.isEmpty())
                    {
                        String memberWithStatus = memberId + " (초대보냄)";
                        if (!memberList.contains(memberWithStatus) && !memberId.equals(myName))
                        {
                            memberList.add(memberWithStatus);
                            memberAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "멤버가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(this, "이미 추가된 멤버입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "멤버 아이디를 입력하세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
        });

        // 그룹 생성 버튼 이벤트
        createGroupButton.setOnClickListener(v ->
        {
            String groupName = groupNameEditText.getText().toString().trim();
            if (groupName.isEmpty())
            {
                Toast.makeText(this, "그룹 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (memberList.size() <= 1)
            {
                Toast.makeText(this, "최소 한 명의 멤버를 추가하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 그룹 생성 처리
            SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = groupPrefs.edit();
            editor.putBoolean("has_group", true);
            editor.putString("group_name", groupName);
            editor.putBoolean("is_leader", true);  // 그룹 생성자를 방장으로 설정
            editor.apply();

            Toast.makeText(this, "그룹이 생성되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}