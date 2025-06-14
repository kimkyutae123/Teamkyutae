package com.example.project_1.api;

import com.example.project_1.entity.Group;
import com.example.project_1.entity.GroupChat;
import com.example.project_1.entity.Member;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("members")
    Call<List<Member>> getAllMembers();

    @POST("members")
    Call<Member> createMember(@Body Member member);

    @GET("groups")
    Call<List<Group>> getAllGroups();

    @POST("groups")
    Call<Group> createGroup(@Body Group group);

    @GET("groups/{groupId}/chats")
    Call<List<GroupChat>> getGroupChats(@Path("groupId") Long groupId);

    @POST("groups/{groupId}/chats")
    Call<GroupChat> sendMessage(@Path("groupId") Long groupId, @Body GroupChat chat);
} 