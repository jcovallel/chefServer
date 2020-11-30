package com.chefserver.demo.model;

import org.springframework.data.annotation.Id;

public class DiasDisponiblesPorSitio {
    @Id
    String id;
    String empresa;
    Boolean lunes;
    Boolean martes;
    Boolean miercoles;
    Boolean jueves;
    Boolean viernes;
    Boolean sabado;
    Boolean domingo;

    public String getid() {
        return id;
    }

    public void setid(String avid) {
        id = avid;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public Boolean getLunes() {
        return lunes;
    }

    public void setLunes(Boolean lunes) {
        this.lunes = lunes;
    }

    public Boolean getMartes() {
        return martes;
    }

    public void setMartes(Boolean martes) {
        this.martes = martes;
    }

    public Boolean getMiercoles() {
        return miercoles;
    }

    public void setMiercoles(Boolean miercoles) {
        this.miercoles = miercoles;
    }

    public Boolean getJueves() {
        return jueves;
    }

    public void setJueves(Boolean jueves) {
        this.jueves = jueves;
    }

    public Boolean getViernes() {
        return viernes;
    }

    public void setViernes(Boolean viernes) {
        this.viernes = viernes;
    }

    public Boolean getSabado() { return sabado; }

    public void setSabado(Boolean sabado) { this.sabado = sabado; }

    public Boolean getDomingo() { return domingo; }

    public void setDomingo(Boolean domingo) { this.domingo = domingo; }
}
