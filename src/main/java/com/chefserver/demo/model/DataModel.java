package com.chefserver.demo.model;

public class DataModel {
    public String empresa;
    String fecha;
    String hora;
    public String nombre;
    public long celular;
    public String correo;
    public String cargo;
    public String tipomenu;
    public boolean lunes = false;
    public boolean martes = false;
    public boolean miercoles = false;
    public boolean jueves = false;
    public boolean viernes = false;
    public String entrega;
    String horaentrega;
    public String direccion;
    public String observaciones;

    //constructor
    public DataModel(String empresa, String fecha, String hora, String nombre, long celular, String correo, String cargo, String tipomenu, String day,
                     String entrega, String horaentrega, String direccion, String observaciones){
        this.empresa = empresa;
        this.fecha = fecha;
        this.hora = hora;
        this.nombre = nombre;
        this.celular = celular;
        this.correo = correo;
        this.cargo = cargo;
        this.tipomenu = tipomenu;

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
        this.entrega = entrega;
        this.horaentrega = horaentrega;
        this.direccion = direccion;
        this.observaciones = observaciones;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
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

    public String getTipomenu() {
        return tipomenu;
    }

    public void setTipomenu(String tipomenu) {
        this.tipomenu = tipomenu;
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

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
    }

    public String getHoraentrega() {
        return horaentrega;
    }

    public void setHoraentrega(String horaentrega) {
        this.horaentrega = horaentrega;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
