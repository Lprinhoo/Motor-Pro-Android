package com.example.testeapi01;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logoSplash);

        // Uma pequena animação de fade-in pra ficar bonito
        Animation fadeIn = new android.view.animation.AlphaAnimation(0, 1);
        fadeIn.setDuration(1500);
        logo.startAnimation(fadeIn);

        // Espera 2,5 segundos e decide para onde ir
        new Handler().postDelayed(() -> {
            Class<?> destination;
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                destination = MainActivity.class; // Já logado, vai para o Mapa
            } else {
                destination = LoginActivity.class; // Não logado, vai para o Login
            }
            
            Intent intent = new Intent(SplashActivity.this, destination);
            startActivity(intent);
            finish();
        }, 2500);
    }
}