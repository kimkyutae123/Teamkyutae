package com.example.project_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_1.api.RetrofitClient;
import com.example.project_1.model.Member;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Random;

public class MyPageActivity extends AppCompatActivity
{
    private EditText nameEditText;
    private Button saveButton, editButton, updateButton;
    private TextView userNameTextView, userIdTextView;
    private SharedPreferences sharedPreferences;

    private String userId;
    private boolean isNameSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        nameEditText = findViewById(R.id.nameEditText);
        saveButton = findViewById(R.id.saveButton);
        editButton = findViewById(R.id.editButton);
        updateButton = findViewById(R.id.updateButton);
        userNameTextView = findViewById(R.id.userNameTextView);
        userIdTextView = findViewById(R.id.userIdTextView);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // 사용자 이름과 고유번호 불러오기
        String userName = sharedPreferences.getString("user_name", null);
        userId = sharedPreferences.getString("user_id", null);

        if (userName != null)
        {
            // 이름이 이미 등록되어 있음
            isNameSet = true;
            userNameTextView.setText("이름 : " + userName);
            userIdTextView.setText("아이디 : " + userId);
        }
        else
        {
            // 이름이 등록되지 않은 경우, 고유 번호 생성
            userId = generateUserId();
            sharedPreferences.edit().putString("user_id", userId).apply();
        }

        if (!isNameSet)
        {
            // 이름이 등록되지 않은 경우
            // 입력 칸, 저장 버튼만 보임
            nameEditText.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            userNameTextView.setVisibility(View.GONE);
            userIdTextView.setVisibility(View.GONE);
        }
        else
        {
            // 이름이 등록된 경우
            // 이름, 아이디, 수정 버튼만 보임
            nameEditText.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
            userNameTextView.setVisibility(View.VISIBLE);
            userIdTextView.setVisibility(View.VISIBLE);
        }

        // 저장 버튼 클릭 이벤트
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // Member 객체 생성
            Member member = new Member();
            member.setName(name);
            
            // API 호출
            RetrofitClient.getInstance()
                .getMemberApi()
                .createMember(member)
                .enqueue(new Callback<Member>() {
                    @Override
                    public void onResponse(Call<Member> call, Response<Member> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("API_CALL", "성공: " + response.body().toString());
                            // 성공 시 SharedPreferences에도 저장
                            sharedPreferences.edit()
                                .putString("user_name", name)
                                .apply();
                            userNameTextView.setText("이름 : " + name);
                            Toast.makeText(MyPageActivity.this, "이름이 저장되었습니다", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("API_CALL", "실패: " + response.code() + " " + response.message());
                            Toast.makeText(MyPageActivity.this, "저장 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Member> call, Throwable t) {
                        Log.e("API_CALL", "에러: " + t.getMessage());
                        Toast.makeText(MyPageActivity.this, 
                            "서버 연결 실패: " + t.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        });

        // 이름 수정 버튼 클릭 시:
        // 입력칸, 수정 확인 버튼만 보임
        editButton.setOnClickListener(v ->
        {
            nameEditText.setText(sharedPreferences.getString("user_name", ""));
            nameEditText.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        });

        // 이름 수정 확인 버튼
        updateButton.setOnClickListener(v ->
        {
            String newName = nameEditText.getText().toString().trim();
            if (!newName.isEmpty())
            {
                sharedPreferences.edit().putString("user_name", newName).apply();
                userNameTextView.setText("이름 : " + newName);

                Toast.makeText(MyPageActivity.this, "수정 완료", Toast.LENGTH_SHORT).show();

                nameEditText.setVisibility(View.GONE);
                updateButton.setVisibility(View.GONE);
                editButton.setVisibility(View.VISIBLE);
            }
            else
            {
                Toast.makeText(MyPageActivity.this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 6자리 숫자 고유번호 생성
    private String generateUserId()
    {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
