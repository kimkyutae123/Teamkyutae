package com.example.project_1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class GroupAgreeActivity extends AppCompatActivity
{
    private Button btnStartShare;
    private Button btnAgreeList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupagree);

        btnStartShare = findViewById(R.id.btn_start_share);
        btnAgreeList = findViewById(R.id.btn_agree_list);

        btnStartShare.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showConsentDialog();
            }
        });

        btnAgreeList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GroupAgreeActivity.this, GroupAgreeList.class);
                startActivity(intent);
            }
        });
    }

    private void showConsentDialog()
    {
        // 커스텀 다이얼로그 뷰 불러오기
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_agree, null);
        CheckBox checkBoxConsent = dialogView.findViewById(R.id.checkbox_agree);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("위치 공유 동의")
                .setView(dialogView)
                .setPositiveButton("동의", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(dialogInterface ->
        {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false); // 기본은 비활성화

            checkBoxConsent.setOnCheckedChangeListener((compoundButton, isChecked) ->
            {
                positiveButton.setEnabled(isChecked);
            });

            positiveButton.setOnClickListener(v ->
            {
                Toast.makeText(GroupAgreeActivity.this, "위치 공유에 동의하였습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // 동의 상태 저장은 나중에!
            });
        });

        dialog.show();
    }
}