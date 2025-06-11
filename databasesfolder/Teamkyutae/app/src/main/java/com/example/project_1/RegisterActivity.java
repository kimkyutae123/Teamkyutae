package com.example.project_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project_1.api.RetrofitClient;
import com.example.project_1.model.Member;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextEmail;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "이름과 이메일을 모두 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            registerMember(name, email);
        });
    }

    private void registerMember(String name, String email) {
        Member member = new Member(name, email);
        
        RetrofitClient.getInstance()
                .getMemberApi()
                .registerMember(member)
                .enqueue(new Callback<Member>() {
                    @Override
                    public void onResponse(Call<Member> call, Response<Member> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Member savedMember = response.body();
                            Toast.makeText(RegisterActivity.this, 
                                "회원 등록 성공: " + savedMember.getName(), 
                                Toast.LENGTH_SHORT).show();
                            finish(); // 등록 성공 후 화면 종료
                        } else {
                            Toast.makeText(RegisterActivity.this, 
                                "회원 등록 실패", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Member> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, 
                            "서버 연결 실패: " + t.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 