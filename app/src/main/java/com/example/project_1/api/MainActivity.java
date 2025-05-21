package com.example.project_1.api;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import com.example.project_1.R;
import com.example.project_1.api.Member;
import com.example.project_1.api.MemberApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private MemberApi memberApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.xxx:8080/")  // 스프링부트 서버 주소(포트 포함)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        memberApi = retrofit.create(MemberApi.class);

        Member newMember = new Member("testUser", "25");



        Call<Member> call = memberApi.addMember(newMember);
        call.enqueue(new Callback<Member>() {
            @Override
            public void onResponse(Call<Member> call, Response<Member> response) {
                if (response.isSuccessful()) {
                    Member savedMember = response.body();
                    Toast.makeText(MainActivity.this, "저장 성공! ID: " + savedMember.getUserId(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "저장 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Member> call, Throwable t) {
                Toast.makeText(MainActivity.this, "통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
