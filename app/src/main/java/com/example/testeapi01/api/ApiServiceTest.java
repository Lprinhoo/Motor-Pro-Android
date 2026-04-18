package com.example.testeapi01.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiServiceTest {
    @POST("mensagem")
    Call<Void> enviarMensagem(@Body Map<String, String> body);
}