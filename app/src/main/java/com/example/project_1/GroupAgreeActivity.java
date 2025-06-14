package com.example.project_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class GroupAgreeActivity extends AppCompatActivity
{
    private ListView agreeListView;
    private Button agreeButton;
    private ArrayAdapter<String> adapter;
    private List<String> groupMembersList = new ArrayList<>();
    private String myName;
    private String myNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupagree);

        agreeListView = findViewById(R.id.agreeListView);
        agreeButton = findViewById(R.id.agreeButton);

        // 사용자 정보 로드
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        myName = userPrefs.getString("user_name", "나");
        myNumber = userPrefs.getString("user_number", "");

        // 위치 공유 상태 확인
        SharedPreferences locationPrefs = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        boolean isLocationShared = locationPrefs.getBoolean("location_shared_" + myNumber, false);
        
        // 버튼 텍스트 초기화
        agreeButton.setText(isLocationShared ? "동의 철회" : "동의하기");

        // 동의 버튼 클릭 리스너 설정
        agreeButton.setOnClickListener(v -> {
            boolean newLocationState = !locationPrefs.getBoolean("location_shared_" + myNumber, false);
            // 위치 공유 상태 변경
            locationPrefs.edit().putBoolean("location_shared_" + myNumber, newLocationState).apply();
            // 버튼 텍스트 업데이트
            agreeButton.setText(newLocationState ? "동의 철회" : "동의하기");
            // 멤버 목록 업데이트
            loadGroupMembers();
            setupMemberList();
        });

        loadGroupMembers();
        setupMemberList();
    }

    private void loadGroupMembers() {
        SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        SharedPreferences locationPrefs = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        groupMembersList.clear();
        
        // 현재 사용자(나) 추가
        boolean isLocationShared = locationPrefs.getBoolean("location_shared_" + myNumber, false);
        String myInfo = myName + " (나)";
        if (isLocationShared) {
            myInfo += " (동의함)";
        }
        groupMembersList.add(myInfo);
        
        // 초대된 멤버들 중 동의한 멤버들을 로드
        String[] testUserNumbers = {"123456", "234567", "345678"};
        String[] testUserNames = {"김철수", "이영희", "박지성"};
        
        for (int i = 0; i < testUserNumbers.length; i++) {
            if (prefs.getBoolean("invited_" + testUserNumbers[i], false) && 
                prefs.getBoolean("agreed_" + testUserNumbers[i], false)) {
                // 테스트 사용자는 자동으로 동의함으로 표시
                String memberInfo = testUserNames[i] + " (" + testUserNumbers[i] + ") (동의함)";
                groupMembersList.add(memberInfo);
            }
        }
    }

    private void setupMemberList() {
        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupMembersList);
        agreeListView.setAdapter(memberAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 위치 공유 상태 확인
        SharedPreferences locationPrefs = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        boolean isLocationShared = locationPrefs.getBoolean("location_shared_" + myNumber, false);
        
        // 버튼 텍스트 업데이트
        agreeButton.setText(isLocationShared ? "동의 철회" : "동의하기");
        
        // 멤버 목록 업데이트
        loadGroupMembers();
        setupMemberList();
    }
}