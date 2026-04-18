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
    @POST("api/auth/google")
    Call<UserProfile> googleLogin(@Body Map<String, String> body);

    @POST("api/auth/email")
    Call<UserProfile> emailLogin(@Body Map<String, String> body);

    @POST("api/veiculos")
    Call<Veiculo> addVehicle(@Body Veiculo vehicle);

    @GET("api/veiculos")
    Call<List<Veiculo>> getVehicles();

    @PUT("api/veiculos/{placa}")
    Call<Veiculo> updateVehicle(@Path("placa") String placa, @Body Veiculo vehicle);

    @DELETE("api/veiculos/{placa}")
    Call<ResponseBody> deleteVehicle(@Path("placa") String placa);

    @GET("api/oficinas")
    Call<List<Oficina>> getOficinas();

    @GET("api/perfis/email/{email}")
    Call<UserProfile> getProfileByEmail(@Path("email") String email);

    @POST("api/perfis/update")
    Call<Void> updateProfile(@Body UserProfile profile);

    @GET("api/agendamentos")
    Call<List<Appointment>> getAppointments();

    @POST("api/agendamentos")
    Call<Void> addAppointment(@Body Appointment appointment);
}