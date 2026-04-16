package com.example.testeapi01;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ServiceReminderWorker extends Worker {

    public ServiceReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Pega os dados enviados no agendamento
        String servico = getInputData().getString("servico");
        String horario = getInputData().getString("horario");

        // Dispara a notificação
        NotificationHelper.showNotification(getApplicationContext(),
                "Lembrete de Serviço!",
                "Seu agendamento de " + servico + " é às " + horario + ". Estamos te esperando!");

        return Result.success();
    }
}
