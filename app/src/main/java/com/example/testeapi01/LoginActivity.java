package com.example.testeapi01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.example.testeapi01.api.RetrofitClient;
import com.example.testeapi01.api.UserProfile;
import com.example.testeapi01.api.Veiculo;
import com.example.testeapi01.api.VehicleApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    
    private View cardAuth, cardUserProfile, progressBar;
    private EditText etEmail, etPassword, etUserName, etModel, etPlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        String webClientId = "860892024221-smqnn7tgfmm09c00h2ph14330out8p6k.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        cardAuth = findViewById(R.id.cardAuth);
        cardUserProfile = findViewById(R.id.cardUserProfile);
        progressBar = findViewById(R.id.progressBar); 
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUserName = findViewById(R.id.etUserName);
        etModel = findViewById(R.id.etModel);
        etPlate = findViewById(R.id.etPlate);

        findViewById(R.id.btnLogin).setOnClickListener(v -> loginComEmail());
        findViewById(R.id.btnRegisterUser).setOnClickListener(v -> criarContaComEmail());
        findViewById(R.id.btnGoogle).setOnClickListener(v -> signInComGoogle());
        findViewById(R.id.btnFinishProfile).setOnClickListener(v -> salvarDadosPerfil());

        if (mAuth.getCurrentUser() != null) {
            verificarPerfilNaApi(mAuth.getCurrentUser());
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
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Erro Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        verificarPerfilNaApi(mAuth.getCurrentUser());
                    } else {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Erro Firebase Auth", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarPerfilNaApi(FirebaseUser user) {
        if (user == null || user.getEmail() == null) return;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        // Mudando para buscar por e-mail, que é o identificador mais estável na sua API
        RetrofitClient.getService().getProfileByEmail(user.getEmail()).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    irParaMapa();
                } else {
                    // Se não encontrar pelo e-mail, aí sim pede para criar o perfil
                    cardAuth.setVisibility(View.GONE);
                    cardUserProfile.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Erro de conexão. Verifique se o servidor está rodando.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginComEmail() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        verificarPerfilNaApi(mAuth.getCurrentUser());
                    } else {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void criarContaComEmail() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getService().getProfileByEmail(email).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Este e-mail já está cadastrado no sistema.", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(LoginActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    cardAuth.setVisibility(View.GONE);
                                    cardUserProfile.setVisibility(View.VISIBLE);
                                } else {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                cardAuth.setVisibility(View.GONE);
                                cardUserProfile.setVisibility(View.VISIBLE);
                            } else {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void salvarDadosPerfil() {
        String name = etUserName.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String plate = etPlate.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();
        
        if (name.isEmpty() || user == null) {
            Toast.makeText(this, "Nome é obrigatório", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Criando o perfil com o nome digitado pelo usuário
        UserProfile profile = new UserProfile(user.getUid(), name, user.getEmail());
        VehicleApiService apiService = RetrofitClient.getService();
        
        apiService.updateProfile(profile).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Se houver veículo, salva ele também antes de ir para o mapa
                    if (!model.isEmpty() && !plate.isEmpty()) {
                        Veiculo vehicle = new Veiculo(model, "", plate);
                        apiService.addVehicle(vehicle).enqueue(new Callback<Veiculo>() {
                            @Override
                            public void onResponse(Call<Veiculo> call, Response<Veiculo> response) {
                                irParaMapa();
                            }
                            @Override
                            public void onFailure(Call<Veiculo> call, Throwable t) {
                                // Mesmo se o veículo falhar, o perfil foi salvo, então prossegue
                                irParaMapa();
                            }
                        });
                    } else {
                        irParaMapa();
                    }
                } else {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Erro ao salvar perfil: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
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
