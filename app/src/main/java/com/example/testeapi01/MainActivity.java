package com.example.testeapi01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.testeapi01.api.Oficina;
import com.example.testeapi01.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Oficina> listaOficinas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificação de login via SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (!prefs.contains("user_email")) {
            irParaLogin();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.cardProfile).setOnClickListener(v -> 
            startActivity(new Intent(this, ProfileActivity.class)));
            
        findViewById(R.id.btnServices).setOnClickListener(v -> 
            startActivity(new Intent(this, ServicesActivity.class)));

        findViewById(R.id.btnTest).setOnClickListener(v -> 
            startActivity(new Intent(this, TestConnectionActivity.class)));

        carregarOficinas();
    }

    private void carregarOficinas() {
        RetrofitClient.getService().getOficinas().enqueue(new Callback<List<Oficina>>() {
            @Override
            public void onResponse(Call<List<Oficina>> call, Response<List<Oficina>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaOficinas = response.body();
                    adicionarMarcadoresOficinas();
                }
            }

            @Override
            public void onFailure(Call<List<Oficina>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erro ao carregar oficinas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adicionarMarcadoresOficinas() {
        if (mMap == null) return;
        for (Oficina oficina : listaOficinas) {
            LatLng pos = new LatLng(oficina.getLatitude(), oficina.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(oficina.getNome())
                    .snippet(oficina.getEndereco())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng bauru = new LatLng(-22.3145, -49.0587);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bauru, 13f));
        adicionarMarcadoresOficinas();
    }

    private void irParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}