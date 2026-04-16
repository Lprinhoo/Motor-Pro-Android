package com.example.testeapi01.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface VehicleApiService {
    // Veículos
    @POST("api/veiculos")
    Call<Veiculo> addVehicle(@Body Veiculo vehicle);

    @GET("api/veiculos")
    Call<List<Veiculo>> getVehicles();

    @PUT("api/veiculos/{placa}")
    Call<Veiculo> updateVehicle(@Path("placa") String placa, @Body Veiculo vehicle);

    @DELETE("api/veiculos/{placa}")
    Call<okhttp3.ResponseBody> deleteVehicle(@Path("placa") String placa);

    // Perfis
    @POST("api/perfis")
    Call<Void> updateProfile(@Body UserProfile profile);

    @GET("api/perfis/{uid}")
    Call<UserProfile> getProfile(@Path("uid") String uid);

    @GET("api/perfis/email/{email}")
    Call<UserProfile> getProfileByEmail(@Path("email") String email);

    @POST("api/auth/google")
    Call<UserProfile> loginComGoogle(@Body String idToken);

    // Agendamentos
    @POST("api/agendamentos")
    Call<Void> addAppointment(@Body Appointment appointment);

    @GET("api/agendamentos")
    Call<List<Appointment>> getAppointments();

    @DELETE("api/agendamentos/{id}")
    Call<okhttp3.ResponseBody> cancelAppointment(@Path("id") long id);

    // Oficinas
    @GET("api/oficinas")
    Call<List<Oficina>> getOficinas();
}