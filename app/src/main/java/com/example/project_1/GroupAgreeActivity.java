package com.example.project_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupAgreeActivity extends AppCompatActivity
{
    private ListView agreeListView;
    private Button agreeButton;
    private ArrayList<String> memberStatusList;
    private ArrayAdapter<String> adapter;
    private boolean isAgreed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupagree);

        // UI 요소 초기화
        agreeListView = findViewById(R.id.agreeListView);
        agreeButton = findViewById(R.id.agreeButton);
        
        // SharedPreferences에서 동의 상태와 멤버 목록 확인
        SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        isAgreed = prefs.getBoolean("location_agreed", false);
        String myName = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_name", "");

        // 멤버 상태 목록 초기화
        memberStatusList = new ArrayList<>();
        Set<String> members = prefs.getStringSet("group_members", new HashSet<>());
        
        // 임시 데이터 (실제로는 서버에서 받아와야 함)
        memberStatusList.add(myName + (isAgreed ? " (동의함)" : " (동의안함)"));
        for (String member : members) {
            if (!member.equals(myName)) {
                // 각 멤버의 동의 상태 (임시로 랜덤하게 설정)
                boolean memberAgreed = Math.random() < 0.5;
                memberStatusList.add(member + (memberAgreed ? " (동의함)" : " (동의안함)"));
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberStatusList);
        agreeListView.setAdapter(adapter);

        // 동의/철회 버튼 클릭 이벤트
        agreeButton.setOnClickListener(v -> {
            isAgreed = !isAgreed;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("location_agreed", isAgreed);
            editor.apply();

            // 내 상태 업데이트
            updateMyStatus(myName);
            Toast.makeText(this, isAgreed ? "위치 공유에 동의하셨습니다." : "위치 공유 동의를 철회하셨습니다.", Toast.LENGTH_SHORT).show();
        });

        // 버튼 텍스트 초기화
        updateButtonText();
    }

    private void updateMyStatus(String myName) {
        for (int i = 0; i < memberStatusList.size(); i++) {
            if (memberStatusList.get(i).startsWith(myName)) {
                memberStatusList.set(i, myName + (isAgreed ? " (동의함)" : " (동의안함)"));
                adapter.notifyDataSetChanged();
                break;
            }
        }
        updateButtonText();
    }

    private void updateButtonText() {
        agreeButton.setText(isAgreed ? "동의 철회" : "동의하기");
    }
}