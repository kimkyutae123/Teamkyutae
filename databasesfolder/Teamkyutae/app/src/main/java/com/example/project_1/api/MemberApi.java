package com.example.project_1.api;

import com.example.project_1.model.Member;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MemberApi {
    @POST("api/member/register")
    Call<Member> registerMember(@Body Member member);

    @GET("api/member/{userId}")
    Call<Member> getMember(@Path("userId") Integer userId);
} 