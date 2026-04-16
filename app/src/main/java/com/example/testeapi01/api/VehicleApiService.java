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
    @POST("veiculos")
    Call<Veiculo> addVehicle(@Body Veiculo vehicle);

    @GET("veiculos")
    Call<List<Veiculo>> getVehicles();

    @PUT("veiculos/{placa}")
    Call<Veiculo> updateVehicle(@Path("placa") String placa, @Body Veiculo vehicle);

    @DELETE("veiculos/{placa}")
    Call<okhttp3.ResponseBody> deleteVehicle(@Path("placa") String placa);

    // Perfis
    @POST("perfis")
    Call<Void> updateProfile(@Body UserProfile profile);

    @GET("perfis/{uid}")
    Call<UserProfile> getProfile(@Path("uid") String uid);

    @GET("perfis/email/{email}")
    Call<UserProfile> getProfileByEmail(@Path("email") String email);

    @POST("auth/google")
    Call<UserProfile> loginComGoogle(@Body String idToken);

    // Agendamentos
    @POST("agendamentos")
    Call<Void> addAppointment(@Body Appointment appointment);

    @GET("agendamentos")
    Call<List<Appointment>> getAppointments();

    @DELETE("agendamentos/{id}")
    Call<okhttp3.ResponseBody> cancelAppointment(@Path("id") long id);

    // Oficinas
    @GET("oficinas")
    Call<List<Oficina>> getOficinas();
}