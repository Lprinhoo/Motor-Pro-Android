package com.example.testeapi01;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.example.testeapi01.api.RetrofitClient;
import com.example.testeapi01.api.UserProfile;
import com.example.testeapi01.api.VehicleApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", null);
        String name = prefs.getString("user_name", "Usuário");

        if (email == null) {
            finish();
            return;
        }

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        progressBar = findViewById(R.id.profileProgressBar);

        tvProfileEmail.setText(email);
        tvProfileName.setText(name);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        carregarPerfilCompleto(email);
    }

    private void carregarPerfilCompleto(String email) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        VehicleApiService apiService = RetrofitClient.getService();
        apiService.getProfileByEmail(email).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    tvProfileName.setText(response.body().getName());
                    // Atualiza SharedPreferences se o nome mudou na API
                    getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit()
                            .putString("user_name", response.body().getName())
                            .apply();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void logout() {
        // Limpa SharedPreferences
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply();

        // Limpa Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        client.signOut().addOnCompleteListener(this, task -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}