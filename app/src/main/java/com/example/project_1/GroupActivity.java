package com.example.project_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;

import java.util.ArrayList;

public class GroupActivity extends AppCompatActivity
{
    private LinearLayout noGroupLayout;
    private LinearLayout hasGroupLayout;
    private TextView groupNameText;
    private Button createGroupButton;
    private Button inviteMemberButton;
    private Button groupAgreeButton;
    private Button groupChatButton;
    private Button leaveGroupButton;
    private Button deleteGroupButton;
    private boolean isLeader = false;
    private String currentGroupId = "";
    private ArrayList<String> groupMembersList = new ArrayList<>();
    private TextView groupMembersTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // 키보드가 자동으로 나타나지 않도록 설정
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_group);

        // 사용자 정보 확인
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = userPrefs.getString("userName", "");
        String userId = userPrefs.getString("userId", "");

        if (userName.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "마이페이지에서 아이디와 이름을 먼저 생성해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        checkGroupStatus();
        setupButtonListeners();
    }

    private void initializeViews()
    {
        noGroupLayout = findViewById(R.id.noGroupLayout);
        hasGroupLayout = findViewById(R.id.hasGroupLayout);
        groupNameText = findViewById(R.id.groupNameText);
        createGroupButton = findViewById(R.id.createGroupButton);
        inviteMemberButton = findViewById(R.id.inviteMemberButton);
        groupAgreeButton = findViewById(R.id.groupAgreeButton);
        groupChatButton = findViewById(R.id.groupChatButton);
        leaveGroupButton = findViewById(R.id.leaveGroupButton);
        deleteGroupButton = findViewById(R.id.deleteGroupButton);
        groupMembersTextView = findViewById(R.id.groupMembersTextView);
    }

    private void checkGroupStatus()
    {
        SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        boolean hasGroup = groupPrefs.getBoolean("has_group", false);
        
        if (hasGroup) {
            String groupName = groupPrefs.getString("group_name", "");
            isLeader = groupPrefs.getBoolean("is_leader", false);
            currentGroupId = groupPrefs.getString("group_id", "");
            updateUI(true, groupName, isLeader);
            loadGroupMembers();
        } else {
            currentGroupId = "";
            isLeader = false;
            updateUI(false, "", false);
        }
    }

    private void loadGroupMembers() {
        SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        groupMembersList.clear();
        
        // 초대된 멤버들 중 동의한 멤버들을 로드
        String[] testUserNumbers = {"123456", "234567", "345678"};
        String[] testUserNames = {"김철수", "이영희", "박지성"};
        
        for (int i = 0; i < testUserNumbers.length; i++) {
            if (prefs.getBoolean("invited_" + testUserNumbers[i], false) && 
                prefs.getBoolean("agreed_" + testUserNumbers[i], false)) {
                String memberInfo = testUserNames[i] + " (" + testUserNumbers[i] + ")";
                groupMembersList.add(memberInfo);
            }
        }
        
        updateGroupMembersList();
    }

    private void setupButtonListeners()
    {
        createGroupButton.setOnClickListener(v -> startActivity(new Intent(this, MakingGroupActivity.class)));
        inviteMemberButton.setOnClickListener(v -> showInviteDialog());
        groupAgreeButton.setOnClickListener(v -> startActivity(new Intent(this, GroupAgreeActivity.class)));
        groupChatButton.setOnClickListener(v -> startActivity(new Intent(this, GroupChatActivity.class)));
        setupLeaveButton();
        setupDeleteButton();
    }

    private void showInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("멤버 초대");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("고유번호 6자리를 입력하세요");
        builder.setView(input);

        builder.setPositiveButton("초대", (dialog, which) -> {
            String userNumber = input.getText().toString();
            if (userNumber.length() == 6) {
                // 테스트 사용자 목록에서 해당 번호 찾기
                String[] testUserNumbers = {"123456", "234567", "345678"};
                String[] testUserNames = {"김철수", "이영희", "박지성"};
                
                for (int i = 0; i < testUserNumbers.length; i++) {
                    if (testUserNumbers[i].equals(userNumber)) {
                        // 초대 및 수락 상태 저장
                        SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = groupPrefs.edit();
                        editor.putBoolean("invited_" + userNumber, true);
                        editor.putBoolean("agreed_" + userNumber, true);
                        editor.apply();

                        // 그룹원 목록에 즉시 추가
                        String memberInfo = testUserNames[i] + " (" + userNumber + ")";
                        groupMembersList.add(memberInfo);
                        updateGroupMembersList();
                        
                        Toast.makeText(this, testUserNames[i] + "님이 초대를 수락했습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Toast.makeText(this, "존재하지 않는 사용자입니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "올바른 고유번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setupLeaveButton()
    {
        leaveGroupButton.setOnClickListener(v ->
        {
            new AlertDialog.Builder(this)
                .setTitle("그룹 나가기")
                .setMessage("정말로 그룹을 나가시겠습니까?")
                .setPositiveButton("예", (dialog, which) ->
                {
                    if (isLeader)
                    {
                        Toast.makeText(this, "방장은 그룹을 나갈 수 없습니다. 그룹을 삭제해주세요.", Toast.LENGTH_SHORT).show();
                    }
                    else if (!currentGroupId.isEmpty())
                    {
                        // 그룹 나가기 처리
                        SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = groupPrefs.edit();
                        editor.clear();
                        editor.apply();
                        
                        currentGroupId = "";
                        updateUI(false, "", false);
                        Toast.makeText(this, "그룹을 나갔습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
        });
    }

    private void setupDeleteButton()
    {
        deleteGroupButton.setOnClickListener(v ->
        {
            new AlertDialog.Builder(this)
                .setTitle("그룹 삭제")
                .setMessage("정말로 그룹을 삭제하시겠습니까?")
                .setPositiveButton("예", (dialog, which) ->
                {
                    if (isLeader)
                    {
                        // 그룹 삭제 처리
                        SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = groupPrefs.edit();
                        editor.clear();
                        editor.apply();
                        
                        currentGroupId = "";
                        isLeader = false;
                        updateUI(false, "", false);
                        Toast.makeText(this, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(this, "방장만 그룹을 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkGroupStatus();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void updateUI(boolean hasGroup, String groupName, boolean isLeader)
    {
        if (hasGroup)
        {
            noGroupLayout.setVisibility(View.GONE);
            hasGroupLayout.setVisibility(View.VISIBLE);
            groupNameText.setText(groupName);
            deleteGroupButton.setVisibility(isLeader ? View.VISIBLE : View.GONE);
        }
        else
        {
            noGroupLayout.setVisibility(View.VISIBLE);
            hasGroupLayout.setVisibility(View.GONE);
            TextView noGroupTextView = findViewById(R.id.noGroupText);
            if (noGroupTextView != null) {
                noGroupTextView.setText("그룹 없음");
            }
        }
    }

    private void updateGroupMembersList() {
        StringBuilder membersText = new StringBuilder();
        for (String member : groupMembersList) {
            membersText.append(member).append("\n");
        }
        groupMembersTextView.setText(membersText.toString());
    }
}