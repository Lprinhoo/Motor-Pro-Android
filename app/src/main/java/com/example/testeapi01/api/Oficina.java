package com.example.testeapi01.api;

import java.util.List;

public class Oficina {
    private Long id;
    private String nome;
    private String endereco;
    private Double latitude;
    private Double longitude;
    private List<String> mecanicos;
    private List<String> servicos;
    private List<String> horarios;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public List<String> getMecanicos() { return mecanicos; }
    public void setMecanicos(List<String> mecanicos) { this.mecanicos = mecanicos; }
    public List<String> getServicos() { return servicos; }
    public void setServicos(List<String> servicos) { this.servicos = servicos; }
    public List<String> getHorarios() { return horarios; }
    public void setHorarios(List<String> horarios) { this.horarios = horarios; }

    @Override
    public String toString() {
        return nome; // Para exibir o nome no Spinner/Lista
    }
}