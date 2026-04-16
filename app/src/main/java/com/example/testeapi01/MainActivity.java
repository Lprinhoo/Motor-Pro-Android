package com.example.testeapi01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.example.testeapi01.api.Oficina;
import com.example.testeapi01.api.RetrofitClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
    private List<Polyline> currentPolylines = new ArrayList<>();
    private List<Marker> workshopMarkers = new ArrayList<>();
    private List<Oficina> listaOficinas = new ArrayList<>();
    private Oficina oficinaSelecionada;

    private Button btnAction, btnCancel, btnServices;
    private View cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irParaLogin();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnAction = findViewById(R.id.btnAction);
        btnCancel = findViewById(R.id.btnCancel);
        btnServices = findViewById(R.id.btnServices);
        cardProfile = findViewById(R.id.cardProfile);

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        btnAction.setOnClickListener(v -> carregarOficinasDaApi());
        btnCancel.setOnClickListener(v -> cancelarBusca());
        btnServices.setOnClickListener(v -> {
            if (oficinaSelecionada != null) {
                Intent intent = new Intent(this, ServicesActivity.class);
                intent.putExtra("OFICINA_ID", oficinaSelecionada.getId());
                intent.putExtra("OFICINA_NOME", oficinaSelecionada.getNome());
                
                // Passando as listas dinâmicas para a tela de agendamento
                if (oficinaSelecionada.getServicos() != null) {
                    intent.putStringArrayListExtra("OFICINA_SERVICOS", new java.util.ArrayList<>(oficinaSelecionada.getServicos()));
                }
                if (oficinaSelecionada.getMecanicos() != null) {
                    intent.putStringArrayListExtra("OFICINA_MECANICOS", new java.util.ArrayList<>(oficinaSelecionada.getMecanicos()));
                }
                
                startActivity(intent);
            }
        });
    }

    private void carregarOficinasDaApi() {
        RetrofitClient.getService().getOficinas().enqueue(new Callback<List<Oficina>>() {
            @Override
            public void onResponse(Call<List<Oficina>> call, Response<List<Oficina>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaOficinas = response.body();
                    mostrarOficinasNoMapa();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao carregar oficinas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Oficina>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha na conexão com API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarOficinasNoMapa() {
        if (mMap == null) return;
        cancelarBusca(); // Limpa o mapa antes de adicionar novas

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_oficina);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(b, 80, 80, false));

        for (Oficina oficina : listaOficinas) {
            if (oficina.getLatitude() == null || oficina.getLongitude() == null) continue;
            
            LatLng pos = new LatLng(oficina.getLatitude(), oficina.getLongitude());
            
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(oficina.getNome())
                    .snippet(oficina.getEndereco())
                    .icon(icon));
            
            marker.setTag(oficina); // Guarda o objeto oficina no marcador
            workshopMarkers.add(marker);
        }

        mMap.setOnMarkerClickListener(marker -> {
            oficinaSelecionada = (Oficina) marker.getTag();
            if (oficinaSelecionada != null) {
                marker.showInfoWindow();
                btnServices.setVisibility(View.VISIBLE);
                btnAction.setVisibility(View.GONE);
                btnCancel.setVisibility(View.VISIBLE);
                
                // Traça rota até esta oficina específica
                trazarRotaAte(marker.getPosition());
            }
            return true;
        });
    }

    private void trazarRotaAte(LatLng destino) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                for (Polyline p : currentPolylines) p.remove();
                currentPolylines.clear();

                Polyline poly = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(location.getLatitude(), location.getLongitude()), destino)
                        .width(12).color(Color.parseColor("#1A73E8")).geodesic(true));
                
                currentPolylines.add(poly);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destino, 14f));
            }
        });
    }

    private void cancelarBusca() {
        for (Marker m : workshopMarkers) m.remove();
        workshopMarkers.clear();
        for (Polyline p : currentPolylines) p.remove();
        currentPolylines.clear();
        
        centralizarNoUsuario();
        btnServices.setVisibility(View.GONE);
        btnAction.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.GONE);
        oficinaSelecionada = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        centralizarNoUsuario();
    }

    private void centralizarNoUsuario() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                }
            });
        }
    }

    private void irParaLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }
}
