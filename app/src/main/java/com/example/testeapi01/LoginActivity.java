package com.example.testeapi01;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String WEB_CLIENT_ID = "214787575143-1n2bs6ub9aafhjk3d2q7ke69jdhl6tlq.apps.googleusercontent.com";

    private GoogleSignInClient mGoogleSignInClient;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Se já está logado, vai direto para o mapa
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (prefs.contains("user_email")) {
            irParaMapa();
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> signInComGoogle());
    }

    private void signInComGoogle() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
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
                Toast.makeText(this, "Erro Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enviarTokenParaApi(String idToken) {
        Map<String, String> body = new HashMap<>();
        body.put("idToken", idToken);

        RetrofitClient.getService().googleLogin(body).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    salvarUsuarioLocal(response.body());
                    irParaMapa();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Erro na API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvarUsuarioLocal(UserProfile user) {
        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
        editor.putString("user_email", user.getEmail());
        editor.putString("user_name", user.getName());
        editor.putString("user_photo", user.getPhotoUrl());
        editor.putLong("user_id", user.getId() != null ? user.getId() : 0);
        editor.apply();
    }

    private void irParaMapa() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}