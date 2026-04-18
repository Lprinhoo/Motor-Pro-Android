package com.example.testeapi01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.example.testeapi01.api.RetrofitClient;
import com.example.testeapi01.api.UserProfile;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Client ID fornecido
        String webClientId = "860892024221-smqnn7tgfmm09c00h2ph14330out8p6k.apps.googleusercontent.com";
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> signInComGoogle());

        // Se já está logado no Google, tenta validar com a API ou vai pro mapa
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            irParaMapa();
        }
    }

    private void signInComGoogle() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                enviarTokenParaApi(account.getIdToken());
            } catch (ApiException e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Erro Google Login: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enviarTokenParaApi(String idToken) {
        if (idToken == null) return;

        Map<String, String> body = new HashMap<>();
        body.put("idToken", idToken);

        RetrofitClient.getService().googleLogin(body).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                // Independente do que venha no UserProfile, o sucesso na autenticação leva ao mapa
                if (response.isSuccessful()) {
                    irParaMapa();
                } else {
                    Toast.makeText(LoginActivity.this, "API recusou o token: " + response.code(), Toast.LENGTH_SHORT).show();
                    mGoogleSignInClient.signOut();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Servidor Offline ou Erro de Rede", Toast.LENGTH_SHORT).show();
                mGoogleSignInClient.signOut();
            }
        });
    }

    private void irParaMapa() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}