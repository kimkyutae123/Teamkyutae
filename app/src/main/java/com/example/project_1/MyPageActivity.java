package com.example.project_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.project_1.api.Member;
import com.example.project_1.api.MemberApi;




public class MyPageActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText;
    private Button saveButton, editButton, updateButton;
    private TextView userNameTextView, userIdTextView, userAgeTextView;

    private MemberApi memberApi;
    private String userId;  // 고유 아이디 필요하면 서버에서 받아서 쓰는 방식 추천

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);  // 네 XML 파일명 맞춰서 변경

        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        saveButton = findViewById(R.id.saveButton);
        editButton = findViewById(R.id.editButton);
        updateButton = findViewById(R.id.updateButton);
        userNameTextView = findViewById(R.id.userNameTextView);
        userIdTextView = findViewById(R.id.userIdTextView);
        userAgeTextView = findViewById(R.id.userAgeTextView);

        // Retrofit 객체 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        memberApi = retrofit.create(MemberApi.class);

        // 저장 버튼 클릭 시 서버에 회원 정보 전송
        saveButton.setVisibility(View.VISIBLE);  // XML에서는 기본적으로 GONE 이라 강제로 보이게 함
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = nameEditText.getText().toString().trim();
                String userAge = ageEditText.getText().toString().trim();

                if (userName.isEmpty() || userAge.isEmpty()) {
                    Toast.makeText(MyPageActivity.this, "이름과 나이를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                Member member = new Member(userName, userAge);
                Call<Member> call = memberApi.addMember(member);

                call.enqueue(new Callback<Member>() {
                    @Override
                    public void onResponse(Call<Member> call, Response<Member> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Member savedMember = response.body();

                            userNameTextView.setText("이름 : " + savedMember.getUserName());
                            userAgeTextView.setText("나이 : " + savedMember.getUserAge());
                            // userIdTextView는 서버에서 id 받아오면 세팅

                            userNameTextView.setVisibility(View.VISIBLE);
                            userAgeTextView.setVisibility(View.VISIBLE);
                            userIdTextView.setVisibility(View.VISIBLE);

                            nameEditText.setVisibility(View.GONE);
                            ageEditText.setVisibility(View.GONE);
                            saveButton.setVisibility(View.GONE);

                            Toast.makeText(MyPageActivity.this, "서버에 저장 성공", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyPageActivity.this, "서버 저장 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Member> call, Throwable t) {
                        Toast.makeText(MyPageActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // (선택) 수정 버튼, 업데이트 버튼 등도 필요하면 위 로직 비슷하게 추가 가능
    }
}
