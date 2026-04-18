package com.example.testeapi01;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.testeapi01.api.RetrofitClientTest;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_connection);

        EditText etMensagem = findViewById(R.id.etMensagem);
        Button btnEnviar = findViewById(R.id.btnEnviar);

        btnEnviar.setOnClickListener(v -> {
            String texto = etMensagem.getText().toString();
            if (texto.isEmpty()) {
                Toast.makeText(this, "Digite algo", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> body = new HashMap<>();
            body.put("conteudo", texto);

            RetrofitClientTest.getService().enviarMensagem(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(TestConnectionActivity.this, "Enviado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestConnectionActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(TestConnectionActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}