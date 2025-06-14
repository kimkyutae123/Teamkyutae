package com.example.project_1;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private long currentGroupId = -1;
    private boolean isLeader = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // 키보드가 자동으로 나타나지 않도록 설정
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_group);

        dbHelper = new DatabaseHelper(this);
        currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getInt("user_id", -1);

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
    }

    private void checkGroupStatus()
    {
        // 사용자가 속한 그룹 찾기
        Cursor groupsCursor = dbHelper.getAllGroups();
        if (groupsCursor != null) {
            while (groupsCursor.moveToNext()) {
                long groupId = groupsCursor.getLong(groupsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROUP_ID));
                int leaderId = groupsCursor.getInt(groupsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROUP_LEADER_ID));
                
                // 해당 그룹의 멤버인지 확인
                Cursor membersCursor = dbHelper.getGroupMembers(groupId);
                if (membersCursor != null) {
                    while (membersCursor.moveToNext()) {
                        int memberId = membersCursor.getInt(membersCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MEMBER_USER_ID));
                        if (memberId == currentUserId) {
                            currentGroupId = groupId;
                            isLeader = (leaderId == currentUserId);
                            String groupName = groupsCursor.getString(groupsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROUP_NAME));
                            updateUI(true, groupName, isLeader);
                            membersCursor.close();
                            groupsCursor.close();
                            return;
                        }
                    }
                    membersCursor.close();
                }
            }
            groupsCursor.close();
        }
        
        // 그룹이 없는 경우
        currentGroupId = -1;
        isLeader = false;
        updateUI(false, "", false);
    }

    private void setupButtonListeners()
    {
        createGroupButton.setOnClickListener(v -> startActivity(new Intent(this, MakingGroupActivity.class)));
        inviteMemberButton.setOnClickListener(v -> startActivity(new Intent(this, MemberInvite.class)));
        groupAgreeButton.setOnClickListener(v -> startActivity(new Intent(this, GroupAgreeActivity.class)));
        groupChatButton.setOnClickListener(v -> startActivity(new Intent(this, GroupChatActivity.class)));
        setupLeaveButton();
        setupDeleteButton();
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
                    else if (currentGroupId != -1)
                    {
                        // 멤버 삭제 로직 구현 필요
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
                    if (currentGroupId != -1 && isLeader)
                    {
                        // 그룹 삭제 로직 구현 필요
                        currentGroupId = -1;
                        isLeader = false;
                        updateUI(false, "", false);
                        Toast.makeText(this, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
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
        if (dbHelper != null) {
            dbHelper.close();
        }
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
}