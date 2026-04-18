package com.example.testeapi01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            finish();
            return;
        }

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        progressBar = findViewById(R.id.profileProgressBar);

        tvProfileEmail.setText(account.getEmail()); 
        tvProfileName.setText(account.getDisplayName() != null ? account.getDisplayName() : "Carregando..."); 

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        // Por enquanto, vamos apenas validar se o perfil existe na API
        validarPerfilNaApi();
    }

    private void validarPerfilNaApi() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null || account.getEmail() == null) return;

        VehicleApiService apiService = RetrofitClient.getService();
        apiService.getProfileByEmail(account.getEmail()).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    tvProfileName.setText(response.body().getName());
                } else {
                    Toast.makeText(ProfileActivity.this, "Perfil não encontrado na API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Erro de conexão com a API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        client.signOut().addOnCompleteListener(this, task -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}