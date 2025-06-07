package com.example.project_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MemberInvite extends AppCompatActivity
{
    private EditText memberIdInput;
    private Button sendInviteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memberinvite);

        memberIdInput = findViewById(R.id.memberIdInput);
        sendInviteButton = findViewById(R.id.sendInviteButton);

        sendInviteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String memberId = memberIdInput.getText().toString();

                if (!memberId.isEmpty())
                {
                    // (임시) 서버 연동 필요
                    Toast.makeText(MemberInvite.this, "멤버 초대 완료!", Toast.LENGTH_SHORT).show();
                    memberIdInput.setText("");
                }
                else
                {
                    Toast.makeText(MemberInvite.this, "멤버 ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
} 