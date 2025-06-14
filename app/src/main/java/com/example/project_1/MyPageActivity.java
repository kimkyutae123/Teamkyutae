package com.example.project_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MyPageActivity extends AppCompatActivity
{
    private EditText nameEditText;
    private Button nameEditButton;
    private Button nameSaveButton;
    private TextView nameTextView;
    private TextView userIdTextView;
    private Button logoutButton;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        initializeViews();
    }

    private void initializeViews()
    {
        nameEditText = findViewById(R.id.nameEditText);
        nameEditButton = findViewById(R.id.nameEditButton);
        nameSaveButton = findViewById(R.id.nameSaveButton);
        nameTextView = findViewById(R.id.nameTextView);
        userIdTextView = findViewById(R.id.userIdTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // 사용자 정보 가져오기
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        String userName = prefs.getString("userName", null);

        // userId가 없는 경우 새로 생성
        if (userId == null) {
            userId = generateUserId();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userId", userId);
            editor.apply();
        }

        // 최초 등록인 경우
        if (userName == null || userName.isEmpty()) {
            nameEditText.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.GONE);
            nameEditButton.setVisibility(View.GONE);
            nameSaveButton.setVisibility(View.VISIBLE);
            userIdTextView.setVisibility(View.GONE);  // 고유번호 숨기기
            logoutButton.setVisibility(View.GONE);    // 로그아웃 버튼 숨기기
        } else {
            nameEditText.setVisibility(View.GONE);
            nameTextView.setVisibility(View.VISIBLE);
            nameEditButton.setVisibility(View.VISIBLE);
            nameSaveButton.setVisibility(View.GONE);
            nameTextView.setText(userName);
            userIdTextView.setVisibility(View.VISIBLE);
            userIdTextView.setText("고유번호: " + userId);
            logoutButton.setVisibility(View.VISIBLE);  // 로그아웃 버튼 표시
        }

        // 이름 저장 버튼 클릭 리스너
        nameSaveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            if (!newName.isEmpty()) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("userName", newName);
                if (editor.commit()) {
                    nameTextView.setText(newName);
                    nameEditText.setVisibility(View.GONE);
                    nameTextView.setVisibility(View.VISIBLE);
                    nameEditButton.setVisibility(View.VISIBLE);
                    nameSaveButton.setVisibility(View.GONE);
                    userIdTextView.setVisibility(View.VISIBLE);
                    userIdTextView.setText("고유번호: " + userId);
                    logoutButton.setVisibility(View.VISIBLE);  // 로그아웃 버튼 표시
                    Toast.makeText(this, "이름이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "이름 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 이름 수정 버튼 클릭 리스너
        nameEditButton.setOnClickListener(v -> {
            nameEditText.setText(nameTextView.getText());
            nameEditText.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.GONE);
            nameEditButton.setVisibility(View.GONE);
            nameSaveButton.setVisibility(View.VISIBLE);
        });

        // 로그아웃 버튼 클릭 리스너
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String generateUserId() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // 6자리 숫자 생성
    }
}
