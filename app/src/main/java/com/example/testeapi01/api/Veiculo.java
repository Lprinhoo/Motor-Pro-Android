package com.example.testeapi01.api;

public class Veiculo {
    private String modelo;
    private String ano;
    private String placa;

    public Veiculo(String modelo, String ano, String placa) {
        this.modelo = modelo;
        this.ano = ano;
        this.placa = placa;
    }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getAno() { return ano; }
    public void setAno(String ano) { this.ano = ano; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
}