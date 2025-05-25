package com.example.project_1;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ChatApi {

    @POST("send")
    Call<String> sendMessage(@Body Message message);

    @GET("messages")
    Call<List<Message>> getMessages();
}
