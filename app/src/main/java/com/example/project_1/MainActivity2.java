package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStartShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartShare = findViewById(R.id.btn_start_share);

        btnStartShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConsentDialog();
            }
        });

        Button btnGroup = findViewById(R.id.btn_group);

        btnGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showConsentDialog() {
        // 커스텀 다이얼로그 뷰 불러오기
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_consent, null);
        CheckBox checkBoxConsent = dialogView.findViewById(R.id.checkbox_consent);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("위치 공유 동의")
                .setView(dialogView)
                .setPositiveButton("동의", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false); // 기본은 비활성화

            checkBoxConsent.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                positiveButton.setEnabled(isChecked);
            });

            positiveButton.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "위치 공유에 동의하였습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // 동의 상태 저장은 나중에!
            });
        });

        dialog.show();
    }
}
