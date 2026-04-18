package com.example.testeapi01;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String webClientId = "214787575143-1n2bs6ub9aafhjk3d2q7ke69jdhl6tlq.apps.googleusercontent.com";
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        progressBar = findViewById(R.id.progressBar);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> signInComGoogle());
        findViewById(R.id.btnLogin).setOnClickListener(v -> loginComEmail());
    }

    private void signInComGoogle() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void loginComEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        RetrofitClient.getService().emailLogin(body).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    salvarPerfilESeguir(response.body());
                } else {
                    Toast.makeText(LoginActivity.this, "Erro no login: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
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
        if (idToken == null) return;

        Map<String, String> body = new HashMap<>();
        body.put("idToken", idToken);

        RetrofitClient.getService().googleLogin(body).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    salvarPerfilESeguir(response.body());
                } else {
                    Toast.makeText(LoginActivity.this, "Erro API: " + response.code(), Toast.LENGTH_SHORT).show();
                    mGoogleSignInClient.signOut();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                mGoogleSignInClient.signOut();
            }
        });
    }

    private void salvarPerfilESeguir(UserProfile profile) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("user_id", profile.getId());
        editor.putString("user_name", profile.getName());
        editor.putString("user_email", profile.getEmail());
        editor.putString("user_photo", profile.getPhotoUrl());
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}