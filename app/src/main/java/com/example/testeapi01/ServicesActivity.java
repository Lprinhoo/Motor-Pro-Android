package com.example.testeapi01;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import com.example.testeapi01.api.Appointment;
import com.example.testeapi01.api.RetrofitClient;
import com.example.testeapi01.api.VehicleApiService;

import java.util.Calendar;
import java.util.List;
import android.content.res.ColorStateList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class ServicesActivity extends AppCompatActivity {

    private TextView textViewDetailTitle, textViewHoursTitle;
    private View horizontalScrollHours, cardInfo;
    private MaterialButton btnBook;
    private ChipGroup chipGroupMechanics;
    private LinearLayout containerServicos;

    private String selectedService = "", selectedMechanic = "", selectedTime = "", userId;
    private Long oficinaId;
    private List<String> listaServicos = new ArrayList<>();
    private List<String> listaMecanicos = new ArrayList<>();
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        oficinaId = getIntent().getLongExtra("OFICINA_ID", -1L);
        listaServicos = getIntent().getStringArrayListExtra("OFICINA_SERVICOS");
        listaMecanicos = getIntent().getStringArrayListExtra("OFICINA_MECANICOS");

        if (listaServicos == null) listaServicos = new ArrayList<>();
        if (listaMecanicos == null) listaMecanicos = new ArrayList<>();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        String nomeOficina = getIntent().getStringExtra("OFICINA_NOME");
        if (toolbar != null) {
            if (nomeOficina != null) toolbar.setTitle(nomeOficina);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        textViewDetailTitle = findViewById(R.id.textViewDetailTitle);
        textViewHoursTitle = findViewById(R.id.textViewHoursTitle);
        horizontalScrollHours = findViewById(R.id.horizontalScrollHours);
        btnBook = findViewById(R.id.btnBook);
        cardInfo = findViewById(R.id.cardInfo);
        chipGroupMechanics = findViewById(R.id.chipGroupMechanics);
        containerServicos = findViewById(R.id.containerServicos);

        setupDynamicServices();
        setupDynamicMechanics();
        setupTimeButtons();

        if (btnBook != null) btnBook.setOnClickListener(v -> realizarAgendamento());
        
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupDynamicServices() {
        if (containerServicos == null) return;
        containerServicos.removeAllViews();
        
        LayoutInflater inflater = LayoutInflater.from(this);
        
        for (String servico : listaServicos) {
            View cardView = inflater.inflate(R.layout.item_servico_card, containerServicos, false);
            TextView textServico = cardView.findViewById(R.id.textServicoNome);
            ImageView iconServico = cardView.findViewById(R.id.iconServico);
            
            textServico.setText(servico);
            // Poderíamos definir ícones diferentes baseados no nome do serviço aqui
            
            cardView.setOnClickListener(v -> selecionarServico(servico));
            containerServicos.addView(cardView);
        }
    }

    private void setupDynamicMechanics() {
        if (chipGroupMechanics == null) return;
        chipGroupMechanics.removeAllViews();
        
        for (String mecanico : listaMecanicos) {
            Chip chip = new Chip(this);
            chip.setText(mecanico);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#333333")));
            chip.setTextColor(Color.WHITE);
            // Ao definir como checkable, o Android já gerencia o ícone de check
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setCheckedIconTint(ColorStateList.valueOf(Color.GREEN));
            
            chipGroupMechanics.addView(chip);
        }

        chipGroupMechanics.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                selectedMechanic = chip.getText().toString();
                textViewHoursTitle.setVisibility(View.VISIBLE);
                horizontalScrollHours.setVisibility(View.VISIBLE);
                selectedTime = "";
                btnBook.setVisibility(View.GONE);
                resetTimeButtons();
                atualizarDisponibilidadeHorarios();
            }
        });
    }

    private void resetTimeButtons() {
        int[] ids = {R.id.btnTime08, R.id.btnTime09, R.id.btnTime10, R.id.btnTime11, R.id.btnTime14, R.id.btnTime15, R.id.btnTime16, R.id.btnTime17};
        for (int id : ids) {
            MaterialButton btn = findViewById(id);
            if (btn != null) btn.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void selecionarServico(String servico) {
        selectedService = servico;
        selectedMechanic = "";
        selectedTime = "";
        textViewDetailTitle.setText(servico);
        cardInfo.setVisibility(View.VISIBLE);
        chipGroupMechanics.clearCheck();
        textViewHoursTitle.setVisibility(View.GONE);
        horizontalScrollHours.setVisibility(View.GONE);
        btnBook.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void setupTimeButtons() {
        int[] ids = {R.id.btnTime08, R.id.btnTime09, R.id.btnTime10, R.id.btnTime11, R.id.btnTime14, R.id.btnTime15, R.id.btnTime16, R.id.btnTime17};
        for (int id : ids) {
            MaterialButton btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    selectedTime = btn.getText().toString().split(" ")[0];
                    btnBook.setVisibility(View.VISIBLE);
                    btnBook.setText("CONFIRMAR PARA AS " + selectedTime);
                    for (int otherId : ids) {
                        MaterialButton b = findViewById(otherId);
                        if (b != null) b.setBackgroundColor(Color.TRANSPARENT);
                    }
                    btn.setBackgroundColor(Color.LTGRAY);
                });
            }
        }
    }

    private void atualizarDisponibilidadeHorarios() {
        if (selectedService.isEmpty() || selectedMechanic.isEmpty()) return;
        
        RetrofitClient.getService().getAppointments().enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Appointment> appointments = response.body();
                    configurarBotoesComAgendamentos(appointments);
                } else {
                    configurarBotoesComAgendamentos(new java.util.ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                configurarBotoesComAgendamentos(new java.util.ArrayList<>());
            }
        });
    }

    private void configurarBotoesComAgendamentos(List<Appointment> appointments) {
        int[] ids = {R.id.btnTime08, R.id.btnTime09, R.id.btnTime10, R.id.btnTime11, R.id.btnTime14, R.id.btnTime15, R.id.btnTime16, R.id.btnTime17};

        if (horizontalScrollHours != null) horizontalScrollHours.setVisibility(View.VISIBLE);
        if (textViewHoursTitle != null) textViewHoursTitle.setVisibility(View.VISIBLE);

        for (int id : ids) {
            MaterialButton btn = findViewById(id);
            if (btn == null) continue;

            String time = btn.getText().toString().split(" ")[0];
            boolean isBooked = false;

            if (appointments != null) {
                for (Appointment appt : appointments) {
                    if (appt.getMecanico() != null && appt.getMecanico().equalsIgnoreCase(selectedMechanic) && 
                        appt.getHorario() != null && appt.getHorario().contains(time)) {
                        isBooked = true;
                        break;
                    }
                }
            }

            btn.setVisibility(View.VISIBLE);
            
            if (isBooked) {
                btn.setEnabled(false);
                btn.setText(time + " (OCUPADO)");
                btn.setTextColor(Color.GRAY);
                btn.setStrokeColor(ColorStateList.valueOf(Color.GRAY));
                btn.setAlpha(0.6f);
            } else {
                // MODO TESTE: Tudo liberado (independente de ser sábado ou horário passado)
                btn.setEnabled(true);
                btn.setAlpha(1.0f);
                btn.setText(time);
                btn.setTextColor(Color.parseColor("#16BC4E"));
                btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#16BC4E")));
                btn.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private void realizarAgendamento() {
        if (isProcessing || selectedService.isEmpty() || selectedMechanic.isEmpty() || selectedTime.isEmpty()) return;
        isProcessing = true;
        
        RetrofitClient.getService().getAppointments().enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Appointment appt : response.body()) {
                        if (appt.getMecanico() != null && appt.getMecanico().equalsIgnoreCase(selectedMechanic) && 
                            appt.getHorario() != null && appt.getHorario().contains(selectedTime)) {
                            Toast.makeText(ServicesActivity.this, "Este horário acabou de ser ocupado!", Toast.LENGTH_LONG).show();
                            isProcessing = false;
                            atualizarDisponibilidadeHorarios();
                            return;
                        }
                    }
                }
                saveAppointmentToApi(selectedService, selectedMechanic, selectedTime);
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                saveAppointmentToApi(selectedService, selectedMechanic, selectedTime);
            }
        });
    }

    private void saveAppointmentToApi(String servico, String mecanico, String horario) {
        Appointment appointment = new Appointment(userId, servico, mecanico, horario, String.valueOf(oficinaId));
        VehicleApiService apiService = RetrofitClient.getService();

        apiService.addAppointment(appointment).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isProcessing = false;
                if (response.isSuccessful()) {
                    Toast.makeText(ServicesActivity.this, "Agendado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ServicesActivity.this, "Erro ao agendar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isProcessing = false;
                Toast.makeText(ServicesActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}