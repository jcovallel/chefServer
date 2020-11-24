package com.chefserver.demo.model;

import org.springframework.data.annotation.Id;

public class DisponibilidadPorMenu {
    @Id
    public String id;
    public String empresa;
    public String menu;
    public Integer Lunes;
    public Integer Martes;
    public Integer Miercoles;
    public Integer Jueves;
    public Integer Viernes;
    public Integer Sabado;
    public Integer Domingo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getMenu() { return menu; }

    public void setMenu(String menu) { this.menu = menu; }

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

    public Integer getSabado() { return Sabado; }

    public void setSabado(Integer sabado) { Sabado = sabado; }

    public Integer getDomingo() { return Domingo; }

    public void setDomingo(Integer domingo) { Domingo = domingo; }
}
