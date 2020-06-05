package com.chefserver.demo.model;

import org.springframework.data.annotation.Id;

public class AvailaibleDays {
    @Id
    public String id;
    public boolean Lunes;
    public boolean Martes;
    public boolean Miercoles;
    public boolean Jueves;
    public boolean Viernes;

    public String getid() {
        return id;
    }

    public void setid(String avid) {
        id = avid;
    }

    public boolean getLunes() {
        return Lunes;
    }

    public void setLunes(boolean lunes) {
        this.Lunes = lunes;
    }

    public boolean getMartes() {
        return Martes;
    }

    public void setMartes(boolean martes) {
        this.Martes = martes;
    }

    public boolean getMiercoles() {
        return Miercoles;
    }

    public void setMiercoles(boolean miercoles) {
        this.Miercoles = miercoles;
    }

    public boolean getJueves() {
        return Jueves;
    }

    public void setJueves(boolean jueves) {
        this.Jueves = jueves;
    }

    public boolean getViernes() {
        return Viernes;
    }

    public void setViernes(boolean viernes) {
        this.Viernes = viernes;
    }
}
