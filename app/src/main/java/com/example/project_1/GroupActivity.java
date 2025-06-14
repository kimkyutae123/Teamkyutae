package com.example.project_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);  // activity_group.xml 사용

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
        SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        boolean hasGroup = prefs.getBoolean("has_group", false);
        String groupName = prefs.getString("group_name", "");
        boolean isLeader = prefs.getBoolean("is_leader", false);
        updateUI(hasGroup, groupName, isLeader);
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
                    SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
                    boolean isLeader = prefs.getBoolean("is_leader", false);
                    
                    if (isLeader)
                    {
                        Toast.makeText(this, "방장은 그룹을 나갈 수 없습니다. 그룹을 삭제해주세요.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("has_group", false);
                        editor.remove("group_name");
                        editor.apply();
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
                    SharedPreferences prefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("has_group", false);
                    editor.remove("group_name");
                    editor.remove("is_leader");
                    editor.apply();
                    updateUI(false, "", false);
                    Toast.makeText(this, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
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
        }
    }
}