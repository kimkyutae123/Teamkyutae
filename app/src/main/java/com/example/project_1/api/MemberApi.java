package com.example.project_1.api;

import com.example.project_1.model.Member;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface MemberApi {
    @GET("api/members")
    Call<List<Member>> getAllMembers();

    @GET("api/members/{id}")
    Call<Member> getMember(@Path("id") Integer id);

    @POST("api/members")
    Call<Member> createMember(@Body Member member);

    @DELETE("api/members/{id}")
    Call<Void> deleteMember(@Path("id") Integer id);

    @PUT("api/members/{id}/agree")
    Call<Member> updateAgreeStatus(@Path("id") Integer id, @Body Member member);
} 