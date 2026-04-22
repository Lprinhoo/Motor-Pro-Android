package com.example.testeapi01.api;

import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface VehicleApiService {
    
    // Autenticação
    @POST("api/auth/google")
    Call<UserProfile> googleLogin(@Body Map<String, String> body);

    @POST("api/auth/email")
    Call<UserProfile> emailLogin(@Body Map<String, String> body);

    // Veículos (Usando userId dinâmico)
    @GET("api/vehicles/{userId}")
    Call<List<Veiculo>> getVehicles(@Path("userId") Long userId);

    @POST("api/vehicles/{userId}")
    Call<Veiculo> addVehicle(@Path("userId") Long userId, @Body Veiculo vehicle);

    @DELETE("api/vehicles/{placa}")
    Call<ResponseBody> deleteVehicle(@Path("placa") String placa);

    // Oficinas
    @GET("api/oficinas")
    Call<List<Oficina>> getOficinas();

    // Perfil
    @GET("api/perfis/email/{email}")
    Call<UserProfile> getProfileByEmail(@Path("email") String email);

    @POST("api/perfis/update")
    Call<Void> updateProfile(@Body UserProfile profile);

    // Agendamentos
    @GET("api/appointments/user/{userId}")
    Call<List<Appointment>> getAppointments(@Path("userId") Long userId);

    @POST("api/appointments/{userId}/{oficinaId}")
    Call<Void> addAppointment(
        @Path("userId") Long userId, 
        @Path("oficinaId") Long oficinaId, 
        @Body Appointment appointment
    );
}