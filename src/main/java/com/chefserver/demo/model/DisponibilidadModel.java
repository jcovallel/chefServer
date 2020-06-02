package com.chefserver.demo.model;

import org.springframework.data.annotation.Id;

public class DisponibilidadModel {
    @Id
    public String empresaid;
    public String empresa;
    public Integer Lunes;
    public Integer Martes;
    public Integer Miercoles;
    public Integer Jueves;
    public Integer Viernes;

    public String getEmpresaid() {
        return empresaid;
    }

    public void setEmpresaid(String empresaid) {
        this.empresaid = empresaid;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public Integer getLunes() {
        return Lunes;
    }

    public void setLunes(Integer lunes) {
        this.Lunes = lunes;
    }

    public Integer getMartes() {
        return Martes;
    }

    public void setMartes(Integer martes) {
        this.Martes = martes;
    }

    public Integer getMiercoles() {
        return Miercoles;
    }

    public void setMiercoles(Integer miercoles) {
        this.Miercoles = miercoles;
    }

    public Integer getJueves() {
        return Jueves;
    }

    public void setJueves(Integer jueves) {
        this.Jueves = jueves;
    }

    public Integer getViernes() {
        return Viernes;
    }

    public void setViernes(Integer viernes) {
        this.Viernes = viernes;
    }
}
