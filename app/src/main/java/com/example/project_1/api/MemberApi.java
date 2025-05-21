package com.example.project_1.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MemberApi {
    @POST("members")
    Call<Member> addMember(@Body Member member);
}