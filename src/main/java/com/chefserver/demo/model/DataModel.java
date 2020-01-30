package com.chefserver.demo.model;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class DataModel {
    public LocalDate fecha;
    public LocalTime hora;
    public String nombre;
    public long celular;
    public String correo;
    public String cargo;
    public boolean lunes = false;
    public boolean martes = false;
    public boolean miercoles = false;
    public boolean jueves = false;
    public boolean viernes = false;
    public String observaciones;

    //constructor
    public DataModel(String fecha, String hora, String nombre, long celular, String correo, String cargo, String day, String observaciones){
        this.fecha = new LocalDate(fecha);
        this.hora = LocalTime.parse(hora);
        this.nombre = nombre;
        this.celular = celular;
        this.correo = correo;
        this.cargo = cargo;
        switch (day){
            case "lunes":{
                this.lunes = true;
            }
            break;
            case "martes":{
                this.martes = true;
            }
            break;
            case "miercoles":{
                this.miercoles = true;
            }
            break;
            case "jueves":{
                this.jueves = true;
            }
            break;
            case "viernes":{
                this.viernes = true;
            }
            break;
        }
        this.observaciones = observaciones;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getCelular() {
        return celular;
    }

    public void setCelular(long celular) {
        this.celular = celular;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public boolean isLunes() {
        return lunes;
    }

    public void setLunes(boolean lunes) {
        this.lunes = lunes;
    }

    public boolean isMartes() {
        return martes;
    }

    public void setMartes(boolean martes) {
        this.martes = martes;
    }

    public boolean isMiercoles() {
        return miercoles;
    }

    public void setMiercoles(boolean miercoles) {
        this.miercoles = miercoles;
    }

    public boolean isJueves() {
        return jueves;
    }

    public void setJueves(boolean jueves) {
        this.jueves = jueves;
    }

    public boolean isViernes() {
        return viernes;
    }

    public void setViernes(boolean viernes) {
        this.viernes = viernes;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
