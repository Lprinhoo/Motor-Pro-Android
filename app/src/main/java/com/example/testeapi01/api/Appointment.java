package com.example.testeapi01.api;

public class Appointment {
    private Long id;
    private String userId;
    private String servico;
    private String mecanico;
    private String horario;
    private Long oficinaId;

    public Appointment(String userId, String servico, String mecanico, String horario, Long oficinaId) {
        this.userId = userId;
        this.servico = servico;
        this.mecanico = mecanico;
        this.horario = horario;
        this.oficinaId = oficinaId;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }
    public String getMecanico() { return mecanico; }
    public void setMecanico(String mecanico) { this.mecanico = mecanico; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public Long getOficinaId() { return oficinaId; }
    public void setOficinaId(Long oficinaId) { this.oficinaId = oficinaId; }
}