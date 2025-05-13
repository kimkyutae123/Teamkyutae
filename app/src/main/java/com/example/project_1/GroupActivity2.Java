package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<GroupMember> memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        recyclerView = findViewById(R.id.recyclerGroupMembers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 샘플 데이터 (실제 앱에서는 서버에서 받아오겠지?)
        memberList = new ArrayList<>();
        memberList.add(new GroupMember("윤현정", true));
        memberList.add(new GroupMember("홍길동", false));
        memberList.add(new GroupMember("이순신", true));

        adapter = new GroupAdapter(memberList);
        recyclerView.setAdapter(adapter);
    }
}
