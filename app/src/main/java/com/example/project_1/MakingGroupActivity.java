package com.example.project_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MakingGroupActivity extends AppCompatActivity
{
    private EditText groupNameEditText;
    private Button addMemberButton;
    private Button saveGroupButton;
    private ListView memberListView;

    private ArrayList<String> memberList;
    private android.widget.ArrayAdapter<String> memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makinggroup);  // 레이아웃 연결

        // 뷰 연결
        groupNameEditText = findViewById(R.id.groupNameEditText);
        addMemberButton = findViewById(R.id.addMemberButton);
        saveGroupButton = findViewById(R.id.saveGroupButton);
        memberListView = findViewById(R.id.memberListView);

        // 멤버 리스트 초기화
        memberList = new ArrayList<>();
        memberAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberList);
        memberListView.setAdapter(memberAdapter);

        // 멤버 추가 버튼 이벤트
        addMemberButton.setOnClickListener(v ->
        {
            // 예시: 고정 이름 추가 (실제 구현에선 사용자 입력 받도록 변경 가능)
            memberList.add("새 멤버 " + (memberList.size() + 1));
            memberAdapter.notifyDataSetChanged();
        });

        // 그룹 저장 버튼 이벤트
        saveGroupButton.setOnClickListener(v ->
        {
            String groupName = groupNameEditText.getText().toString().trim();
            if (groupName.isEmpty())
            {
                Toast.makeText(this, "그룹 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // 실제 저장 로직은 추후 추가
                Toast.makeText(this, "그룹 '" + groupName + "' 저장됨", Toast.LENGTH_SHORT).show();
                finish();  // 현재 액티비티 종료
            }
        });
    }
}