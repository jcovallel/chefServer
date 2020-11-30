package com.chefserver.demo.model;

import org.springframework.data.annotation.Id;

public class DisponibilidadPorMenu {
    @Id
    public String id;
    public String empresa;
    public String menu;
    public Integer Lunesref;
    public Integer Martesref;
    public Integer Miercolesref;
    public Integer Juevesref;
    public Integer Viernesref;
    public Integer Sabadoref;
    public Integer Domingoref;
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

    public Integer getLunesref() {
        return Lunesref;
    }

    public void setLunesref(Integer lunesref) {
        Lunesref = lunesref;
    }

    public Integer getMartesref() {
        return Martesref;
    }

    public void setMartesref(Integer martesref) {
        Martesref = martesref;
    }

    public Integer getMiercolesref() {
        return Miercolesref;
    }

    public void setMiercolesref(Integer miercolesref) {
        Miercolesref = miercolesref;
    }

    public Integer getJuevesref() {
        return Juevesref;
    }

    public void setJuevesref(Integer juevesref) {
        Juevesref = juevesref;
    }

    public Integer getViernesref() {
        return Viernesref;
    }

    public void setViernesref(Integer viernesref) {
        Viernesref = viernesref;
    }

    public Integer getSabadoref() {
        return Sabadoref;
    }

    public void setSabadoref(Integer sabadoref) {
        Sabadoref = sabadoref;
    }

    public Integer getDomingoref() {
        return Domingoref;
    }

    public void setDomingoref(Integer domingoref) {
        Domingoref = domingoref;
    }

    public void setLunes(Integer lunes) {
        Lunes = lunes;
    }

    public Integer getMartes() {
        return Martes;
    }

    public void setMartes(Integer martes) {
        Martes = martes;
    }

    public Integer getMiercoles() {
        return Miercoles;
    }

    public void setMiercoles(Integer miercoles) {
        Miercoles = miercoles;
    }

    public Integer getJueves() {
        return Jueves;
    }

    public void setJueves(Integer jueves) {
        Jueves = jueves;
    }

    public Integer getViernes() {
        return Viernes;
    }

    public void setViernes(Integer viernes) {
        Viernes = viernes;
    }

    public Integer getSabado() {
        return Sabado;
    }

    public void setSabado(Integer sabado) {
        Sabado = sabado;
    }

    public Integer getDomingo() {
        return Domingo;
    }

    public void setDomingo(Integer domingo) {
        Domingo = domingo;
    }
}
