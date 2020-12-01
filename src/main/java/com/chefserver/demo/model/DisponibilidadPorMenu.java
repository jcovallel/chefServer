package com.chefserver.demo.model;

import org.springframework.data.annotation.Id;

public class DisponibilidadPorMenu {
    @Id
    public String id;
    public String empresa;
    public String menu;
    public Integer lunesref;
    public Integer martesref;
    public Integer miercolesref;
    public Integer juevesref;
    public Integer viernesref;
    public Integer sabadoref;
    public Integer domingoref;
    public Integer lunes;
    public Integer martes;
    public Integer miercoles;
    public Integer jueves;
    public Integer viernes;
    public Integer sabado;
    public Integer domingo;

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
        return lunes;
    }

    public Integer getLunesref() {
        return lunesref;
    }

    public void setLunesref(Integer lunesref) {
        this.lunesref = lunesref;
    }

    public Integer getMartesref() {
        return martesref;
    }

    public void setMartesref(Integer martesref) {
        this.martesref = martesref;
    }

    public Integer getMiercolesref() {
        return miercolesref;
    }

    public void setMiercolesref(Integer miercolesref) {
        this.miercolesref = miercolesref;
    }

    public Integer getJuevesref() {
        return juevesref;
    }

    public void setJuevesref(Integer juevesref) {
        this.juevesref = juevesref;
    }

    public Integer getViernesref() {
        return viernesref;
    }

    public void setViernesref(Integer viernesref) {
        this.viernesref = viernesref;
    }

    public Integer getSabadoref() {
        return sabadoref;
    }

    public void setSabadoref(Integer sabadoref) {
        this.sabadoref = sabadoref;
    }

    public Integer getDomingoref() {
        return domingoref;
    }

    public void setDomingoref(Integer domingoref) {
        this.domingoref = domingoref;
    }

    public void setLunes(Integer lunes) {
        this.lunes = lunes;
    }

    public Integer getMartes() {
        return martes;
    }

    public void setMartes(Integer martes) {
        this.martes = martes;
    }

    public Integer getMiercoles() {
        return miercoles;
    }

    public void setMiercoles(Integer miercoles) {
        this.miercoles = miercoles;
    }

    public Integer getJueves() {
        return jueves;
    }

    public void setJueves(Integer jueves) {
        this.jueves = jueves;
    }

    public Integer getViernes() {
        return viernes;
    }

    public void setViernes(Integer viernes) {
        this.viernes = viernes;
    }

    public Integer getSabado() {
        return sabado;
    }

    public void setSabado(Integer sabado) {
        this.sabado = sabado;
    }

    public Integer getDomingo() {
        return domingo;
    }

    public void setDomingo(Integer domingo) {
        this.domingo = domingo;
    }
}
