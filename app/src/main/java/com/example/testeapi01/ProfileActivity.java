package com.example.testeapi01;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.example.testeapi01.api.RetrofitClient;
import com.example.testeapi01.api.UserProfile;
import com.example.testeapi01.api.Veiculo;
import com.example.testeapi01.api.VehicleApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private LinearLayout layoutVehicleList;
    private View progressBar;
    private final android.os.Handler handler = new android.os.Handler();

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
        layoutVehicleList = findViewById(R.id.layoutVehicleList);
        progressBar = findViewById(R.id.profileProgressBar);

        View layoutEditName = findViewById(R.id.layoutEditName);
        if (layoutEditName != null) {
            layoutEditName.setOnClickListener(v -> showEditNameDialog());
        }

        tvProfileEmail.setText(account.getEmail()); 
        tvProfileName.setText(account.getDisplayName() != null ? account.getDisplayName() : "Carregando..."); 

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        findViewById(R.id.btnAddVehicle).setOnClickListener(v -> showAddVehicleDialog());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        carregarDadosPerfil();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void carregarDadosPerfil() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        VehicleApiService apiService = RetrofitClient.getService();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        
        if (account == null || account.getEmail() == null) return;

        apiService.getProfileByEmail(account.getEmail()).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nomeApi = response.body().getName();
                    tvProfileName.setText((nomeApi != null && !nomeApi.isEmpty()) ? nomeApi : account.getDisplayName());
                } else {
                    tvProfileName.setText(account.getDisplayName());
                }
            }
            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                tvProfileName.setText("Sem conexão com API");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });

        apiService.getVehicles().enqueue(new Callback<List<Veiculo>>() {
            @Override
            public void onResponse(Call<List<Veiculo>> call, Response<List<Veiculo>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                layoutVehicleList.removeAllViews();
                if (response.isSuccessful() && response.body() != null) {
                    for (Veiculo v : response.body()) {
                        adicionarCardVeiculo(v.getModelo(), v.getPlaca(), v.getAno());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Veiculo>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_TESTEAPI01_Dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_name, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        final com.google.android.material.textfield.TextInputEditText inputName = view.findViewById(R.id.etDialogName);
        final com.google.android.material.button.MaterialButton btnSave = view.findViewById(R.id.btnDialogSave);
        final com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnDialogCancel);

        inputName.setText(tvProfileName.getText().toString());

        btnSave.setOnClickListener(v -> {
            String novoNome = inputName.getText().toString().trim();
            if (novoNome.isEmpty()) {
                Toast.makeText(this, "O nome não pode estar vazio", Toast.LENGTH_SHORT).show();
                return;
            }
            atualizarNomeApi(novoNome);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void atualizarNomeApi(String novoNome) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) return;

        // Na sua API, o UserProfile agora usa 'name' em vez de 'nome'
        UserProfile profile = new UserProfile();
        profile.setName(novoNome);
        profile.setEmail(account.getEmail());
        profile.setGoogleId(account.getId());
        
        // Se houver um endpoint updateProfile que aceite o UserProfile atualizado
        // Nota: Assumindo que VehicleApiService já possui os métodos adequados
        // Caso não tenha, o ideal seria adicionar no service.
    }

    private void adicionarCardVeiculo(String modelo, String placa, String ano) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);
        card.setRadius(48);
        card.setCardElevation(4);
        card.setStrokeWidth(0);
        card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#16BC4E")));

        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.HORIZONTAL);
        mainContainer.setPadding(48, 48, 48, 48);
        mainContainer.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvModelo = new TextView(this);
        tvModelo.setText(modelo != null ? modelo.toUpperCase() : "");
        tvModelo.setTextSize(18);
        tvModelo.setTextColor(Color.WHITE); 
        tvModelo.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvAnoPlaca = new TextView(this);
        tvAnoPlaca.setText(String.format("Ano: %s | Placa: %s", (ano != null && !ano.isEmpty() ? ano : "---"), placa));
        tvAnoPlaca.setTextColor(Color.WHITE);
        tvAnoPlaca.setTextSize(14);

        textLayout.addView(tvModelo);
        textLayout.addView(tvAnoPlaca);

        ImageView ivEdit = new ImageView(this);
        ivEdit.setImageResource(android.R.drawable.ic_menu_edit);
        ivEdit.setColorFilter(Color.WHITE);
        ivEdit.setPadding(12, 12, 12, 12);
        ivEdit.setClickable(true);
        ivEdit.setFocusable(true);
        ivEdit.setOnClickListener(v -> showEditVehicleDialog(modelo, placa, ano));

        ImageView ivDelete = new ImageView(this);
        ivDelete.setImageResource(android.R.drawable.ic_menu_delete);
        ivDelete.setColorFilter(Color.WHITE); 
        ivDelete.setPadding(12, 12, 12, 12);
        ivDelete.setClickable(true);
        ivDelete.setFocusable(true);
        ivDelete.setOnClickListener(v -> confirmarExclusao(modelo, placa));

        mainContainer.addView(textLayout);
        mainContainer.addView(ivEdit);
        mainContainer.addView(ivDelete);
        card.addView(mainContainer);

        layoutVehicleList.addView(card);
    }

    private void confirmarExclusao(String modelo, String placa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_TESTEAPI01_Dialog);
        
        TextView title = new TextView(this);
        title.setText("Remover Veículo");
        title.setPadding(60, 60, 60, 0);
        title.setTextSize(20);
        title.setTextColor(Color.parseColor("#F5F6FC"));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        builder.setCustomTitle(title);
        
        builder.setMessage("Deseja realmente excluir o veículo " + modelo + "?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    deleteVehicleFromApi(placa);
                })
                .setNegativeButton("Cancelar", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#EA4335"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    private void showAddVehicleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_TESTEAPI01_Dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_vehicle, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        final TextView title = view.findViewById(R.id.tvDialogTitle);
        if (title != null) title.setText("ADICIONAR VEÍCULO");

        final EditText inputModel = view.findViewById(R.id.etDialogModel);
        final EditText inputYear = view.findViewById(R.id.etDialogYear);
        final EditText inputPlate = view.findViewById(R.id.etDialogPlate);
        final MaterialButton btnAdd = view.findViewById(R.id.btnDialogAdd);
        final MaterialButton btnCancel = view.findViewById(R.id.btnDialogCancel);

        inputYear.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4)});
        configurarMascaraPlaca(inputPlate);
        btnAdd.setText("ADICIONAR");

        btnAdd.setOnClickListener(v -> {
            String model = inputModel.getText().toString().trim();
            String year = inputYear.getText().toString().trim();
            String plate = inputPlate.getText().toString().trim();
            if (model.isEmpty() || plate.isEmpty()) {
                Toast.makeText(this, "Preencha modelo e placa", Toast.LENGTH_SHORT).show();
                return;
            }
            saveVehicleToApi(model, year, plate);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void showEditVehicleDialog(String modelo, String placa, String ano) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_TESTEAPI01_Dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_vehicle, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        final TextView title = view.findViewById(R.id.tvDialogTitle);
        if (title != null) title.setText("EDITAR VEÍCULO");

        final EditText inputModel = view.findViewById(R.id.etDialogModel);
        final EditText inputYear = view.findViewById(R.id.etDialogYear);
        final EditText inputPlate = view.findViewById(R.id.etDialogPlate);
        final MaterialButton btnSave = view.findViewById(R.id.btnDialogAdd);
        final MaterialButton btnCancel = view.findViewById(R.id.btnDialogCancel);

        inputYear.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4)});
        inputModel.setText(modelo);
        inputYear.setText(ano);
        inputPlate.setText(placa);
        inputPlate.setEnabled(false);
        btnSave.setText("SALVAR ALTERAÇÕES");

        btnSave.setOnClickListener(v -> {
            String newModel = inputModel.getText().toString().trim();
            String newYear = inputYear.getText().toString().trim();
            if (newModel.isEmpty()) {
                Toast.makeText(this, "O modelo não pode estar vazio", Toast.LENGTH_SHORT).show();
                return;
            }
            updateVehicleInApi(newModel, newYear, placa);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void updateVehicleInApi(String modelo, String ano, String placa) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Veiculo vehicle = new Veiculo(modelo, ano, placa);
        RetrofitClient.getService().updateVehicle(placa, vehicle).enqueue(new Callback<Veiculo>() {
            @Override
            public void onResponse(Call<Veiculo> call, Response<Veiculo> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Veículo atualizado!", Toast.LENGTH_SHORT).show();
                    carregarDadosPerfil();
                } else {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Veiculo> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteVehicleFromApi(String placa) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getService().deleteVehicle(placa).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Veículo removido", Toast.LENGTH_SHORT).show();
                    carregarDadosPerfil();
                } else {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Erro ao excluir: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveVehicleToApi(String modelo, String ano, String placa) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Veiculo vehicle = new Veiculo(modelo, ano, placa);
        VehicleApiService apiService = RetrofitClient.getService();
        apiService.addVehicle(vehicle).enqueue(new Callback<Veiculo>() {
            @Override
            public void onResponse(Call<Veiculo> call, Response<Veiculo> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Veículo salvo!", Toast.LENGTH_SHORT).show();
                    carregarDadosPerfil();
                } else {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Erro ao salvar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Veiculo> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarMascaraPlaca(EditText etPlate) {
        etPlate.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) { isUpdating = false; return; }
                String str = s.toString().toUpperCase().replaceAll("[^A-Z0-9]", "");
                String res = "";
                if (!str.isEmpty()) {
                    res = str.substring(0, Math.min(str.length(), 3));
                    if (str.length() > 3) res += "-" + str.substring(3, Math.min(str.length(), 7));
                }
                isUpdating = true;
                etPlate.setText(res);
                etPlate.setSelection(res.length());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        client.signOut().addOnCompleteListener(this, task -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}